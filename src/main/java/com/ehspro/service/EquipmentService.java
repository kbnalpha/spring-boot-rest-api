package com.ehspro.service;
import com.ehspro.dto.*;
import com.ehspro.entity.Equipment;
import com.ehspro.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class EquipmentService {
    private final EquipmentRepository repository;
    private final DtoMapper mapper;
    private final ListQueryService queries;
    private final ReferenceService references;
    public EquipmentService(EquipmentRepository repository, DtoMapper mapper, ListQueryService queries, ReferenceService references) {
        this.repository = repository; this.mapper = mapper; this.queries = queries; this.references = references;
    }
    @Transactional
    public Long create(EquipmentDto dto) {
        ReferenceService.creating(dto.id);
        references.organization(dto.organizationUnitId);
        Equipment entity = mapper.map(dto, Equipment.class);
        entity.id = null;
        return repository.saveAndFlush(entity).id;
    }
    public PageResult<EquipmentDto> list(ListRequest request) {
        return queries.list(Equipment.class, request, "organizationUnitId", "uid", this::response);
    }
    @Transactional
    public Long update(Long id, EquipmentDto dto) {
        Equipment old = repository.findById(id).orElseThrow(() -> com.ehspro.exception.ApiException.notFound("Equipment not found"));
        references.organization(old.organizationUnitId);
        references.organization(dto.organizationUnitId);
        if (!java.util.Objects.equals(old.organizationUnitId, dto.organizationUnitId)) throw com.ehspro.exception.ApiException.badRequest("Master records cannot be moved between organizations");
        Equipment entity = mapper.map(dto, Equipment.class);
        entity.id = id; entity.createdBy = old.createdBy; entity.createdDate = old.createdDate;
        entity.modifiedDate = java.time.LocalDateTime.now();
        return repository.saveAndFlush(entity).id;
    }
    private EquipmentDto response(Equipment entity) {
        EquipmentDto dto = mapper.map(entity, EquipmentDto.class);
        dto.active = ReferenceService.status(entity.status);
        dto.organizationUnitNames = references.organizationName(entity.organizationUnitId);
        return dto;
    }
}
