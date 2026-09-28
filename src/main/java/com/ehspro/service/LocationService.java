package com.ehspro.service;

import com.ehspro.dto.*;
import com.ehspro.entity.*;
import com.ehspro.exception.ApiException;
import com.ehspro.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LocationService {
    private final LocationRepository repository;
    private final DtoMapper mapper;
    private final ListQueryService queries;
    private final ReferenceService references;
    public LocationService(LocationRepository repository, DtoMapper mapper, ListQueryService queries, ReferenceService references) {
        this.repository = repository; this.mapper = mapper; this.queries = queries; this.references = references;
    }
    @Transactional
    public Long create(LocationDto dto) {
        ReferenceService.creating(dto.id);
        return save(dto,null);
    }
    @Transactional
    public Long update(Long id,LocationDto dto) { return save(dto,id); }
    private Long save(LocationDto dto,Long updateId) {
        Location old=updateId==null ? null : repository.findById(updateId).orElseThrow(() -> ApiException.notFound("Location not found"));
        Set<Long> existingChildren=new HashSet<>();
        if(old!=null) {
            references.organization(old.organizationUnitId);
            if(!Objects.equals(old.organizationUnitId,dto.organizationUnitId)) throw ApiException.badRequest("Location cannot be moved between organizations");
            old.subLocations.forEach(child -> existingChildren.add(child.id));
        }
        references.organization(dto.organizationUnitId);
        if (dto.subLocationIds != null && !dto.subLocationIds.isEmpty()) throw ApiException.badRequest("Existing sublocation IDs cannot be attached to a new location");
        Location entity = mapper.map(dto, Location.class);
        entity.id = updateId;
        if(old!=null) { entity.createdBy=old.createdBy;entity.createdDate=old.createdDate;entity.modifiedDate=java.time.LocalDateTime.now(); }
        if (entity.supervisorIds == null) entity.supervisorIds = new ArrayList<>();
        entity.supervisorIds.forEach(references::employee);
        List<SubLocationDto> children = new ArrayList<>();
        if (dto.subLocations != null) children.addAll(dto.subLocations);
        if (dto.newSublocations != null) children.addAll(dto.newSublocations);
        Set<String> names = new HashSet<>();
        entity.subLocations = new ArrayList<>();
        for (SubLocationDto child : children) {
            if(child.id!=null&&child.id!=0&&!existingChildren.contains(child.id)) throw ApiException.badRequest("Sublocation does not belong to this location");
            if (!names.add(child.name.trim().toLowerCase(Locale.ROOT))) throw ApiException.badRequest("Duplicate sublocation name");
            SubLocation sub = mapper.map(child, SubLocation.class);
            if(sub.id!=null&&sub.id==0) sub.id=null;
            entity.subLocations.add(sub);
        }
        Location saved=repository.saveAndFlush(entity);
        if (saved.translations != null) saved.translations.forEach(t -> {
            if (t.subLocations != null) t.subLocations.forEach(s -> {
                if (s.subLocationId == null || s.subLocationId == 0) {
                    s.subLocationId = saved.subLocations.stream().filter(c -> c.name.equals(s.name)).map(c -> c.id).findFirst().orElse(null);
                }
                if (s.subLocationId != null && saved.subLocations.stream().noneMatch(c -> c.id.equals(s.subLocationId))) throw ApiException.badRequest("Translation references an unknown sublocation");
            });
        });
        return saved.id;
    }
    public PageResult<LocationDto> list(ListRequest request) {
        return queries.list(Location.class, request, "organizationUnitId", "name", this::response);
    }
    private LocationDto response(Location entity) {
        LocationDto dto = mapper.map(entity, LocationDto.class);
        dto.statusName = ReferenceService.status(entity.status);
        dto.organizationUnitName = references.organizationName(entity.organizationUnitId);
        dto.createByName = references.employeeName(entity.createdBy);
        dto.subLocationNames = entity.subLocations.stream().map(s -> s.name).collect(Collectors.joining(", "));
        dto.supervisorNames = entity.supervisorIds.stream().map(references::employeeName).collect(Collectors.joining(", "));
        dto.newSublocations = null;
        return dto;
    }
}
