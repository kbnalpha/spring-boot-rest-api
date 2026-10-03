package com.ehspro.service;
import com.ehspro.dto.*;
import com.ehspro.entity.*;
import com.ehspro.exception.ApiException;
import com.ehspro.repository.ReferenceItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;
import java.util.*;

@Service @Transactional(readOnly=true)
public class ReferenceDataService {
    private static final Set<String> KINDS=Set.of("COUNTRY","STATE","CITY","LANGUAGE","TIME_ZONE","LANDING_PAGE");
    private final ReferenceItemRepository repository;
    private final DtoMapper mapper;
    public ReferenceDataService(ReferenceItemRepository repository,DtoMapper mapper) { this.repository=repository;this.mapper=mapper; }
    public ReferenceItem require(String kind,Long id) {
        if(id==null) throw ApiException.badRequest(kind+" ID is required");
        return repository.findById(new ReferenceItemId(kind,id)).orElseThrow(() -> ApiException.badRequest("Unknown "+kind+" ID: "+id));
    }
    public String name(String kind,Long id) { return id==null ? null : repository.findById(new ReferenceItemId(kind,id)).map(r -> r.name).orElse(null); }
    public List<ReferenceItem> list(String kind,Long countryId,Long stateId) {
        validateKind(kind);
        return repository.findByKindOrderByNameAsc(kind).stream()
            .filter(r -> countryId==null || r.countryId==null || countryId.equals(r.countryId))
            .filter(r -> stateId==null || stateId.equals(r.stateId)).toList();
    }
    @Transactional
    public ReferenceItem save(String kind,Long id,ReferenceItemDto dto) {
        validateKind(kind);
        if(id==null) throw ApiException.badRequest("Reference ID is required");
        if(Set.of("STATE","CITY","TIME_ZONE").contains(kind) || dto.countryId!=null) require("COUNTRY",dto.countryId);
        if(kind.equals("CITY")) {
            if(!Objects.equals(require("STATE",dto.stateId).countryId,dto.countryId)) throw ApiException.badRequest("State does not belong to country");
        }
        if(kind.equals("TIME_ZONE")) {
            try { ZoneId.of(dto.zoneId); } catch(Exception e) { throw ApiException.badRequest("A valid IANA zoneId is required"); }
        }
        if(kind.equals("COUNTRY") && (blank(dto.code)||blank(dto.currency)||blank(dto.symbol))) throw ApiException.badRequest("Country code, currency and symbol are required");
        ReferenceItem item=mapper.map(dto,ReferenceItem.class); item.kind=kind;item.id=id;
        // Reference identifiers must not silently move between parents after being used.
        repository.findById(new ReferenceItemId(kind,id)).ifPresent(old -> {
            if(!Objects.equals(old.countryId,item.countryId)||!Objects.equals(old.stateId,item.stateId)) throw ApiException.badRequest("Existing reference parent cannot be changed");
        });
        return repository.save(item);
    }
    public void organization(OrganizationUnitDto dto) {
        ReferenceItem country=require("COUNTRY",dto.country);
        if(!Objects.equals(require("STATE",dto.state).countryId,dto.country)) throw ApiException.badRequest("State does not belong to country");
        ReferenceItem city=require("CITY",dto.city);
        if(!Objects.equals(city.countryId,dto.country)||!Objects.equals(city.stateId,dto.state)) throw ApiException.badRequest("City does not belong to selected country/state");
        ReferenceItem language=require("LANGUAGE",dto.languageId);
        if(language.countryId!=null&&!language.countryId.equals(dto.country)) throw ApiException.badRequest("Language is unavailable for this country");
        ReferenceItem zone=require("TIME_ZONE",dto.timeZoneId);
        if(!Objects.equals(zone.countryId,dto.country)) throw ApiException.badRequest("Time zone is unavailable for this country");
        dto.countryCode=country.code;dto.currency=country.currency;dto.symbol=country.symbol;dto.timeZone=zone.zoneId;
        if((dto.latitude==null)!=(dto.longitude==null)) throw ApiException.badRequest("Latitude and longitude must be supplied together");
    }
    public void employee(EmployeeDto dto) {
        ReferenceItem language=require("LANGUAGE",dto.languageID);
        if(dto.country!=null) {
            dto.countryCode=require("COUNTRY",dto.country).code;
            if(language.countryId!=null&&!language.countryId.equals(dto.country)) throw ApiException.badRequest("Language is unavailable for this country");
        } else dto.countryCode=null;
    }
    private boolean blank(String s) { return s==null||s.isBlank(); }
    private void validateKind(String kind) { if(!KINDS.contains(kind)) throw ApiException.badRequest("Unknown reference kind"); }
}
