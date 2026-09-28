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
    public DepartmentService(DepartmentRepository repository, DtoMapper mapper, ListQueryService queries, ReferenceService references) {
        this.repository = repository; this.mapper = mapper; this.queries = queries; this.references = references;
    }
    @Transactional
    public Long create(DepartmentDto dto) {
        ReferenceService.creating(dto.id);
        references.organization(dto.businessUnitId);
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
        references.organization(old.businessUnitId);
        references.organization(dto.businessUnitId);
        if (!java.util.Objects.equals(old.businessUnitId, dto.businessUnitId)) throw com.ehspro.exception.ApiException.badRequest("Master records cannot be moved between organizations");
        Department entity = mapper.map(dto, Department.class);
        entity.id = id; entity.createdBy = old.createdBy; entity.createdDate = old.createdDate;
        entity.modifiedDate = java.time.LocalDateTime.now();
        return repository.saveAndFlush(entity).id;
    }
    private DepartmentDto response(Department entity) {
        DepartmentDto dto = mapper.map(entity, DepartmentDto.class);
        dto.statusDisplay = ReferenceService.status(entity.status);
        return dto;
    }
}
