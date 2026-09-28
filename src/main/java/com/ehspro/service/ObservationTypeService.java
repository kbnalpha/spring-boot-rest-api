package com.ehspro.service;

import com.ehspro.dto.*;
import com.ehspro.entity.*;
import com.ehspro.exception.ApiException;
import com.ehspro.repository.ObservationTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ObservationTypeService {
    private final ObservationTypeRepository repository;
    private final DtoMapper mapper;
    private final ListQueryService queries;
    private final ReferenceService references;
    public ObservationTypeService(ObservationTypeRepository repository, DtoMapper mapper, ListQueryService queries, ReferenceService references) {
        this.repository = repository; this.mapper = mapper; this.queries = queries; this.references = references;
    }
    @Transactional
    public ObservationTypeDto create(ObservationTypeDto dto) {
        ReferenceService.creating(dto.id);
        return save(dto,null);
    }
    @Transactional
    public ObservationTypeDto update(Long id,ObservationTypeDto dto) { return save(dto,id); }
    private ObservationTypeDto save(ObservationTypeDto dto,Long updateId) {
        ObservationType old=updateId==null ? null : repository.findById(updateId).orElseThrow(() -> ApiException.notFound("Observation type not found"));
        Set<Long> existingChildren=new HashSet<>();
        if(old!=null) {
            references.organization(old.businessUnitId);
            if(!Objects.equals(old.businessUnitId,dto.businessUnitId)) throw ApiException.badRequest("Observation type cannot be moved between organizations");
            old.observationSubTypes.forEach(child -> existingChildren.add(child.id));
        }
        references.organization(dto.businessUnitId);
        ObservationType entity = mapper.map(dto, ObservationType.class);
        entity.id = updateId;
        if(old!=null) { entity.createdBy=old.createdBy;entity.createdDate=old.createdDate;entity.modifiedDate=java.time.LocalDateTime.now(); }
        if (entity.observationSubTypes == null) entity.observationSubTypes = new ArrayList<>();
        for (ObservationSubType child : entity.observationSubTypes) {
            if(child.id!=null&&child.id!=0&&!existingChildren.contains(child.id)) throw ApiException.badRequest("Subtype does not belong to this observation type");
            if(child.id!=null&&child.id==0) child.id=null;
        }
        ObservationType saved=repository.saveAndFlush(entity);
        if (saved.translations != null) saved.translations.forEach(t -> {
            if (t.observationSubTypes == null) return;
            for (int i = 0; i < t.observationSubTypes.size(); i++) {
                var s = t.observationSubTypes.get(i);
                if (s.languageId == null) s.languageId = t.languageId;
                if (s.observationSubTypeId == null || s.observationSubTypeId == 0) {
                    if (i >= saved.observationSubTypes.size()) throw ApiException.badRequest("Too many translated subtypes");
                    s.observationSubTypeId = saved.observationSubTypes.get(i).id;
                }
                if (saved.observationSubTypes.stream().noneMatch(c -> c.id.equals(s.observationSubTypeId))) throw ApiException.badRequest("Translation references an unknown subtype");
            }
        });
        return response(saved);
    }
    public PageResult<ObservationTypeDto> list(ListRequest request) {
        return queries.list(ObservationType.class, request, "businessUnitId", "typeDescription", this::response);
    }
    private ObservationTypeDto response(ObservationType entity) {
        ObservationTypeDto dto = mapper.map(entity, ObservationTypeDto.class);
        dto.observationSubTypes.forEach(s -> s.observationTypeId = entity.id);
        dto.observationSubTypeDescriptions = entity.observationSubTypes.stream().map(s -> s.subTypeDescription).collect(Collectors.joining(", "));
        dto.enableSvtDisplay = String.valueOf(Boolean.TRUE.equals(entity.enableSvt));
        dto.statusDisplay = ReferenceService.status(entity.status);
        dto.createdByName = references.employeeName(entity.createdBy);
        dto.isEditDelete = true;
        dto.observationCategoryName = entity.observationCategoryId == null ? null : switch (entity.observationCategoryId.intValue()) {
            case 1 -> "Unsafe Act";
            case 4 -> "Safe Behavior";
            default -> null;
        };
        return dto;
    }
}
