package com.ehspro.service;
import com.ehspro.dto.*;
import com.ehspro.entity.OperationActivity;
import com.ehspro.repository.OperationActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class OperationActivityService {
    private final OperationActivityRepository repository;
    private final DtoMapper mapper;
    private final ListQueryService queries;
    private final ReferenceService references;
    public OperationActivityService(OperationActivityRepository repository, DtoMapper mapper, ListQueryService queries, ReferenceService references) {
        this.repository = repository; this.mapper = mapper; this.queries = queries; this.references = references;
    }
    @Transactional
    public Long create(OperationActivityDto dto) {
        ReferenceService.creating(dto.id);
        references.organization(dto.businessUnitId);
        OperationActivity entity = mapper.map(dto, OperationActivity.class);
        entity.id = null;
        return repository.saveAndFlush(entity).id;
    }
    public PageResult<OperationActivityDto> list(ListRequest request) {
        return queries.list(OperationActivity.class, request, "businessUnitId", "activityName", this::response);
    }
    @Transactional
    public Long update(Long id, OperationActivityDto dto) {
        OperationActivity old = repository.findById(id).orElseThrow(() -> com.ehspro.exception.ApiException.notFound("OperationActivity not found"));
        references.organization(old.businessUnitId);
        references.organization(dto.businessUnitId);
        if (!java.util.Objects.equals(old.businessUnitId, dto.businessUnitId)) throw com.ehspro.exception.ApiException.badRequest("Master records cannot be moved between organizations");
        OperationActivity entity = mapper.map(dto, OperationActivity.class);
        entity.id = id; entity.createdBy = old.createdBy; entity.createdDate = old.createdDate;
        entity.modifiedDate = java.time.LocalDateTime.now();
        return repository.saveAndFlush(entity).id;
    }
    private OperationActivityDto response(OperationActivity entity) {
        OperationActivityDto dto = mapper.map(entity, OperationActivityDto.class);
        dto.businessUnitName = references.organizationName(entity.businessUnitId);
        dto.category = Long.valueOf(1).equals(entity.categoryId) ? "Routine" : null;
        return dto;
    }
}
