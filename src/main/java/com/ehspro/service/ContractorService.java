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
    public ContractorService(ContractorRepository repository,ReferenceService references,ListQueryService queries,DtoMapper mapper) {
        this.repository=repository;this.references=references;this.queries=queries;this.mapper=mapper;
    }
    @Transactional public Long create(ContractorDto dto) {
        ReferenceService.creating(dto.id);references.organization(dto.businessUnitId);
        Contractor entity=mapper.map(dto,Contractor.class);entity.id=null;return repository.saveAndFlush(entity).id;
    }
    @Transactional public Long update(Long id,ContractorDto dto) {
        Contractor old=repository.findById(id).orElseThrow(() -> ApiException.notFound("Contractor not found"));
        references.organization(old.businessUnitId);references.organization(dto.businessUnitId);
        old.name=dto.name;old.status=dto.status;old.businessUnitId=dto.businessUnitId;return old.id;
    }
    public PageResult<ContractorDto> list(ListRequest request) { return queries.list(Contractor.class,request,"businessUnitId","name",e -> mapper.map(e,ContractorDto.class)); }
}
