package com.ehspro.service;
import com.ehspro.dto.*;
import com.ehspro.entity.Contractor;
import com.ehspro.repository.ContractorRepository;
import com.ehspro.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly=true)
public class ContractorService {
    private final ContractorRepository repository;
    private final ReferenceService references;
    private final ListQueryService queries;
    private final DtoMapper mapper;
    private final ReferenceDataService lookups;
    public ContractorService(ContractorRepository repository,ReferenceService references,ListQueryService queries,DtoMapper mapper,ReferenceDataService lookups) {
        this.repository=repository;this.references=references;this.queries=queries;this.mapper=mapper;this.lookups=lookups;
    }
    @Transactional public Long create(ContractorDto dto) {
        ReferenceService.creating(dto.id);validate(dto);
        Contractor entity=mapper.map(dto,Contractor.class);entity.id=null;return repository.saveAndFlush(entity).id;
    }
    @Transactional public Long update(Long id,ContractorDto dto) {
        Contractor old=repository.findById(id).orElseThrow(() -> ApiException.notFound("Contractor not found"));
        references.organization(old.businessUnitId);validate(dto);
        if(!old.businessUnitId.equals(dto.businessUnitId)) throw ApiException.badRequest("Contractor cannot be moved between organizations");
        Contractor entity=mapper.map(dto,Contractor.class);entity.id=id;entity.createdBy=old.createdBy;entity.createdDate=old.createdDate;
        entity.modifiedDate=java.time.LocalDateTime.now();return repository.saveAndFlush(entity).id;
    }
    private void validate(ContractorDto dto) {
        references.organization(dto.businessUnitId);
        if(dto.contractorName!=null) {
            if(dto.name!=null&&!dto.name.equals(dto.contractorName)) throw ApiException.badRequest("name and contractorName must agree");
            dto.name=dto.contractorName;
        }
        if(dto.name==null||dto.name.isBlank()) throw ApiException.badRequest("contractorName is required");
        if(dto.countryId!=null) lookups.require("COUNTRY",dto.countryId);
        if(dto.stateId!=null&&!java.util.Objects.equals(lookups.require("STATE",dto.stateId).countryId,dto.countryId)) throw ApiException.badRequest("State does not belong to country");
        if(dto.cityId!=null) {
            var city=lookups.require("CITY",dto.cityId);
            if(!java.util.Objects.equals(city.countryId,dto.countryId)||!java.util.Objects.equals(city.stateId,dto.stateId)) throw ApiException.badRequest("City does not belong to country/state");
        }
    }
    public PageResult<ContractorDto> list(ListRequest request) {
        if("contractorName".equals(request.sorting)) request.sorting="name";
        if(request.filters!=null) request.filters.forEach(f -> {if("contractorName".equals(f.field)) f.field="name";});
        if(request.multiSortMeta!=null) request.multiSortMeta.forEach(f -> {if("contractorName".equals(f.field)) f.field="name";});
        return queries.list(Contractor.class,request,"businessUnitId","name",this::response);
    }
    private ContractorDto response(Contractor entity) {
        ContractorDto dto=mapper.map(entity,ContractorDto.class);dto.contractorName=entity.name;
        dto.country=lookups.name("COUNTRY",entity.countryId);dto.state=lookups.name("STATE",entity.stateId);dto.city=lookups.name("CITY",entity.cityId);
        dto.businessUnitName=references.organizationName(entity.businessUnitId);dto.createdByName=references.employeeName(entity.createdBy);return dto;
    }
}
