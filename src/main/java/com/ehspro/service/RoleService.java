package com.ehspro.service;
import com.ehspro.dto.RoleDto;
import com.ehspro.entity.*;
import com.ehspro.model.PermissionNode;
import com.ehspro.exception.ApiException;
import com.ehspro.repository.*;
import com.ehspro.security.AccessService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @Transactional(readOnly=true)
public class RoleService {
    private final RoleRepository repository;
    private final PermissionDefinitionRepository permissions;
    private final DtoMapper mapper;
    private final AccessService access;
    private final ReferenceDataService lookups;
    public RoleService(RoleRepository repository,PermissionDefinitionRepository permissions,DtoMapper mapper,AccessService access,ReferenceDataService lookups) {
        this.repository=repository;this.permissions=permissions;this.mapper=mapper;this.access=access;this.lookups=lookups;
    }
    @Transactional public Long create(RoleDto dto) {
        access.require("CreateRole"); ReferenceService.creating(dto.id);
        Role entity=validated(dto);entity.id=null;
        return repository.saveAndFlush(entity).id;
    }
    @Transactional public Long update(Long id,RoleDto dto) {
        access.superAdmin();
        Role old=repository.findById(id).orElseThrow(() -> ApiException.notFound("Role not found"));
        if(old.systemRole) throw ApiException.badRequest("System roles cannot be configured through the application");
        dto.tenantId=old.tenantId;
        Role entity=validated(dto);entity.id=id;entity.createdBy=old.createdBy;entity.createdDate=old.createdDate;
        return repository.saveAndFlush(entity).id;
    }
    private Role validated(RoleDto dto) {
        if(dto.systemRole || "super admin".equalsIgnoreCase(dto.name.trim())) throw ApiException.badRequest("Super Admin is a reserved system role");
        if((dto.roleOrganizationUnits!=null&&!dto.roleOrganizationUnits.isEmpty())||(dto.userRoles!=null&&!dto.userRoles.isEmpty()))
            throw ApiException.badRequest("Assign user roles and organizational scope through their separate APIs");
        lookups.require("LANDING_PAGE",dto.landingPageId);
        Set<Long> ids=new HashSet<>(dto.permissionIds==null ? Set.of() : dto.permissionIds);
        collect(dto.permissionLookupHierarchyDto,ids,0);
        collect(dto.permissions,ids,0);
        if(ids.isEmpty()) throw ApiException.badRequest("Select at least one business permission");
        for(Long id:ids) {
            PermissionDefinition p=permissions.findById(id).orElseThrow(() -> ApiException.badRequest("Unknown permission: "+id));
            if(!p.businessAction || p.superAdminOnly) throw ApiException.badRequest("Permission cannot be granted to a customer role: "+p.code);
        }
        Role entity=mapper.map(dto,Role.class); entity.tenantId=access.tenant(dto.tenantId);entity.systemRole=false;
        entity.permissionIds=ids;entity.permissions=new ArrayList<>();entity.roleOrganizationUnits=new ArrayList<>();entity.userRoles=new ArrayList<>();
        if(entity.status==null) entity.status=1;
        return entity;
    }
    private void collect(List<PermissionNode> nodes,Set<Long> ids,int depth) {
        if(depth>32) throw ApiException.badRequest("Permission hierarchy is too deep");
        if(nodes==null) return;
        for(PermissionNode node:nodes) {
            if(node==null) throw ApiException.badRequest("Permission nodes cannot be null");
            if(Boolean.TRUE.equals(node.isGranted)) {
                if(node.id==null) throw ApiException.badRequest("Permission ID is required");
                ids.add(node.id);
            }
            collect(node.children,ids,depth+1);
        }
    }
    public List<RoleDto> list() {
        return repository.findAll(Sort.by("id")).stream()
            .filter(r -> access.isSuperAdmin() || (!r.systemRole&&Objects.equals(r.tenantId,access.tenant(null))))
            .map(this::response).toList();
    }
    private RoleDto response(Role role) {
        RoleDto dto=mapper.map(role,RoleDto.class);dto.createBy=role.createdBy;
        dto.permissions=new ArrayList<>();
        for(PermissionDefinition p:permissions.findAll()) {
            if(!p.businessAction || (!role.systemRole&&!role.permissionIds.contains(p.id))) continue;
            PermissionNode node=new PermissionNode();node.id=p.id;node.name=p.code;node.displayName=p.displayName;
            node.parentId=p.parentId;node.isGranted=true;node.status=1;dto.permissions.add(node);
        }
        dto.permissionLookupHierarchyDto=dto.permissions;
        return dto;
    }
    public List<Map<String,Object>> catalog() {
        List<Map<String,Object>> result=new ArrayList<>();
        for(PermissionDefinition p:permissions.findAll(Sort.by("moduleName","id"))) {
            Map<String,Object> value=new LinkedHashMap<>();value.put("id",p.id);value.put("name",p.code);
            value.put("displayName",p.displayName);value.put("module",p.moduleName);value.put("parentId",p.parentId);
            value.put("businessAction",p.businessAction);value.put("superAdminOnly",p.superAdminOnly);
            value.put("selectable",p.businessAction&&!p.superAdminOnly);value.put("disabled",!p.businessAction||p.superAdminOnly);
            result.add(value);
        }
        return result;
    }
}