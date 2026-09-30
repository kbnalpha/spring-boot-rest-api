package com.ehspro.service;

import com.ehspro.dto.OrganizationUnitDto;
import com.ehspro.entity.OrganizationUnit;
import com.ehspro.exception.ApiException;
import com.ehspro.repository.OrganizationUnitRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class OrganizationUnitService {
    private final OrganizationUnitRepository repository;
    private final DtoMapper mapper;
    private final com.ehspro.security.AccessService access;
    private final ReferenceDataService lookups;
    public OrganizationUnitService(OrganizationUnitRepository repository, DtoMapper mapper, com.ehspro.security.AccessService access, ReferenceDataService lookups) {
        this.repository = repository; this.mapper = mapper; this.access=access; this.lookups=lookups;
    }
    @Transactional
    public Long create(OrganizationUnitDto dto) {
        access.administrator();
        if(!access.isSuperAdmin()) {
            if(dto.parentId==null||dto.parentId<=0) throw new org.springframework.security.access.AccessDeniedException("Only Super Admin can create a root organization");
            access.organization(dto.parentId);
            dto.tenantId=access.tenant(dto.tenantId);
        }
        ReferenceService.creating(dto.id);
        lookups.organization(dto);
        if (dto.parentId != null && dto.parentId != 0) {
            OrganizationUnit parent = repository.findById(dto.parentId)
                .orElseThrow(() -> ApiException.notFound("Parent organization not found: " + dto.parentId));
            if (dto.tenantId == null) dto.tenantId = parent.tenantId;
            if (!Objects.equals(dto.tenantId, parent.tenantId)) throw ApiException.badRequest("Parent belongs to another tenant");
        } else dto.parentId = null;
        if(dto.tenantId==null) dto.tenantId=1L;
        if(dto.status==null) dto.status=1;
        OrganizationUnit entity = mapper.map(dto, OrganizationUnit.class);
        entity.id = null;
        return repository.saveAndFlush(entity).id;
    }
    public List<OrganizationUnitDto> list() {
        Map<Long, OrganizationUnitDto> nodes = new LinkedHashMap<>();
                var allowed=access.organizationIds();
        repository.findAll(Sort.by("id")).stream().filter(e -> allowed.contains(e.id)).forEach(e -> {
            OrganizationUnitDto dto=mapper.map(e,OrganizationUnitDto.class);
            dto.cityName=lookups.name("CITY",e.city);dto.stateName=lookups.name("STATE",e.state);dto.countryName=lookups.name("COUNTRY",e.country);
            dto.languageName=lookups.name("LANGUAGE",e.languageId);dto.timeZoneName=lookups.name("TIME_ZONE",e.timeZoneId);
            nodes.put(e.id,dto);
        });
        List<OrganizationUnitDto> roots = new ArrayList<>();
        for (OrganizationUnitDto node : nodes.values()) {
            Set<Long> visited = new HashSet<>();
            OrganizationUnitDto cursor = node;
            while (cursor != null) {
                if (!visited.add(cursor.id)) throw new IllegalStateException("Organization hierarchy contains a cycle");
                cursor = nodes.get(cursor.parentId);
            }
            OrganizationUnitDto parent = nodes.get(node.parentId);
            if (parent == null) roots.add(node); else parent.children.add(node);
        }
        return roots;
    }
    @Transactional
    public Long update(Long id, OrganizationUnitDto dto) {
        access.administrator();access.organization(id);
        OrganizationUnit old=repository.findById(id).orElseThrow(() -> ApiException.notFound("Organization not found"));
        if(dto.tenantId!=null&&!Objects.equals(dto.tenantId,old.tenantId)) throw ApiException.badRequest("Organization instance cannot be changed");
        dto.tenantId=old.tenantId;
        if(dto.parentId!=null&&dto.parentId==0) dto.parentId=null;
        if(!access.isSuperAdmin()) {
            access.tenant(old.tenantId);
            if(!Objects.equals(old.parentId,dto.parentId)) {
                if(dto.parentId==null) throw new org.springframework.security.access.AccessDeniedException("Only Super Admin can promote an organization to a root");
                // Scope anchors cannot be moved: that could move another account's whole tree.
                if(old.parentId==null) throw new org.springframework.security.access.AccessDeniedException("Only Super Admin can reparent a root organization");
                access.organization(dto.parentId);
            }
        }
        Set<Long> visited=new HashSet<>(Set.of(id));
        Long parentId=dto.parentId;
        while(parentId!=null) {
            if(!visited.add(parentId)) throw ApiException.badRequest("Organization hierarchy cannot contain a cycle");
            OrganizationUnit parent=repository.findById(parentId).orElseThrow(() -> ApiException.badRequest("Parent organization not found"));
            if(!Objects.equals(parent.tenantId,dto.tenantId)) throw ApiException.badRequest("Parent belongs to another instance");
            parentId=parent.parentId;
        }
        lookups.organization(dto);
        OrganizationUnit entity=mapper.map(dto,OrganizationUnit.class);entity.id=id;
        entity.createdBy=old.createdBy;entity.createdDate=old.createdDate;entity.modifiedDate=java.time.LocalDateTime.now();
        return repository.saveAndFlush(entity).id;
    }
}
