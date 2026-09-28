package com.ehspro.service;
import com.ehspro.dto.*;
import com.ehspro.entity.Designation;
import com.ehspro.repository.DesignationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class DesignationService {
    private final DesignationRepository repository;
    private final DtoMapper mapper;
    private final ListQueryService queries;
    private final ReferenceService references;
    public DesignationService(DesignationRepository repository, DtoMapper mapper, ListQueryService queries, ReferenceService references) {
        this.repository = repository; this.mapper = mapper; this.queries = queries; this.references = references;
    }
    @Transactional
    public Long create(DesignationDto dto) {
        ReferenceService.creating(dto.id);
        references.organization(dto.businessUnitId);
        Designation entity = mapper.map(dto, Designation.class);
        entity.id = null;
        return repository.saveAndFlush(entity).id;
    }
    public PageResult<DesignationDto> list(ListRequest request) {
        return queries.list(Designation.class, request, "businessUnitId", "name", this::response);
    }
    @Transactional
    public Long update(Long id, DesignationDto dto) {
        Designation old = repository.findById(id).orElseThrow(() -> com.ehspro.exception.ApiException.notFound("Designation not found"));
        references.organization(old.businessUnitId);
        references.organization(dto.businessUnitId);
        if (!java.util.Objects.equals(old.businessUnitId, dto.businessUnitId)) throw com.ehspro.exception.ApiException.badRequest("Master records cannot be moved between organizations");
        Designation entity = mapper.map(dto, Designation.class);
        entity.id = id; entity.createdBy = old.createdBy; entity.createdDate = old.createdDate;
        entity.modifiedDate = java.time.LocalDateTime.now();
        return repository.saveAndFlush(entity).id;
    }
    private DesignationDto response(Designation entity) {
        DesignationDto dto = mapper.map(entity, DesignationDto.class);
        dto.statusDisplay = ReferenceService.status(entity.status);
        return dto;
    }
}
