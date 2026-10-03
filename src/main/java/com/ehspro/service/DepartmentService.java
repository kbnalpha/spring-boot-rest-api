package com.ehspro.service;
import com.ehspro.dto.*;
import com.ehspro.entity.Department;
import com.ehspro.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class DepartmentService {
    private final DepartmentRepository repository;
    private final DtoMapper mapper;
    private final ListQueryService queries;
    private final ReferenceService references;
    private final com.ehspro.security.AccessService access;
    public DepartmentService(DepartmentRepository repository, DtoMapper mapper, ListQueryService queries,
            ReferenceService references, com.ehspro.security.AccessService access) {
        this.repository = repository; this.mapper = mapper; this.queries = queries; this.references = references; this.access=access;
    }
    @Transactional
    public Long create(DepartmentDto dto) {
        ReferenceService.creating(dto.id);
        validateBusinessUnit(dto.businessUnitId);
        Department entity = mapper.map(dto, Department.class);
        entity.id = null;
        return repository.saveAndFlush(entity).id;
    }
    public PageResult<DepartmentDto> list(ListRequest request) {
        return queries.list(Department.class, request, "businessUnitId", "name", this::response);
    }
    @Transactional
    public Long update(Long id, DepartmentDto dto) {
        Department old = repository.findById(id).orElseThrow(() -> com.ehspro.exception.ApiException.notFound("Department not found"));
        validateBusinessUnit(old.businessUnitId);
        validateBusinessUnit(dto.businessUnitId);
        if (!java.util.Objects.equals(old.businessUnitId, dto.businessUnitId)) throw com.ehspro.exception.ApiException.badRequest("Master records cannot be moved between organizations");
        Department entity = mapper.map(dto, Department.class);
        entity.id = id; entity.createdBy = old.createdBy; entity.createdDate = old.createdDate;
        entity.modifiedDate = java.time.LocalDateTime.now();
        return repository.saveAndFlush(entity).id;
    }
    private void validateBusinessUnit(Long businessUnitId) {
        if(Long.valueOf(-1).equals(businessUnitId)) access.superAdmin();
        else references.organization(businessUnitId);
    }
    private DepartmentDto response(Department entity) {
        DepartmentDto dto = mapper.map(entity, DepartmentDto.class);
        dto.statusDisplay = ReferenceService.status(entity.status);
        return dto;
    }
}
