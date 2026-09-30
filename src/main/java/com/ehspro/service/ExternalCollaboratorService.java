package com.ehspro.service;
import com.ehspro.dto.*;
import com.ehspro.entity.ExternalCollaborator;
import com.ehspro.exception.ApiException;
import com.ehspro.repository.*;
import com.ehspro.security.AccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @Transactional(readOnly=true)
public class ExternalCollaboratorService {
    private final ExternalCollaboratorRepository repository;
    private final RoleRepository roles;
    private final ReferenceService references;
    private final ReferenceDataService lookups;
    private final AccessService access;
    private final ListQueryService queries;
    private final DtoMapper mapper;
    public ExternalCollaboratorService(ExternalCollaboratorRepository repository,RoleRepository roles,ReferenceService references,
            ReferenceDataService lookups,AccessService access,ListQueryService queries,DtoMapper mapper) {
        this.repository=repository;this.roles=roles;this.references=references;this.lookups=lookups;this.access=access;this.queries=queries;this.mapper=mapper;
    }
    @Transactional public Long save(ExternalCollaboratorDto dto) {
        access.require("ManageExternalCollaborators");
        if(dto.id!=null&&dto.id<0) throw ApiException.badRequest("id must be zero or positive");
        ExternalCollaborator old=null;
        if(dto.id!=null&&dto.id>0) {
            old=repository.findById(dto.id).orElseThrow(() -> ApiException.notFound("Temporary user not found"));
            references.organization(old.organizationUnitId);
            if(!old.organizationUnitId.equals(dto.organizationUnitId)) throw ApiException.badRequest("Temporary user cannot be moved between organizations");
        }
        var unit=references.organization(dto.organizationUnitId);
        var role=roles.findById(dto.roleId).orElseThrow(() -> ApiException.badRequest("Unknown role"));
        if(role.builtInAdmin) access.administrator();
        if((role.systemRole&&!role.builtInAdmin)||(!role.builtInAdmin&&!Objects.equals(role.tenantId,unit.tenantId))||!Integer.valueOf(1).equals(role.status)) throw ApiException.badRequest("Role must be active and assignable in this instance");
        if(old==null||!Objects.equals(old.roleId,dto.roleId)) access.require("ManageRoleUsers");
        if(dto.country!=null) lookups.require("COUNTRY",dto.country);
        if(dto.externalDetails!=null) {
            var details=dto.externalDetails;
            if(dto.companyName!=null&&details.companyName!=null&&!dto.companyName.equals(details.companyName)) throw ApiException.badRequest("Company names must agree");
            if(dto.designation!=null&&details.designation!=null&&!dto.designation.equals(details.designation)) throw ApiException.badRequest("Designations must agree");
            if(details.status!=null&&!details.status.equals(dto.status)) throw ApiException.badRequest("Statuses must agree");
            if(details.companyName!=null) dto.companyName=details.companyName;
            if(details.designation!=null) dto.designation=details.designation;
        }
        Set<Long> memberships=new LinkedHashSet<>();memberships.add(unit.id);
        if(dto.organizationUnitListIds!=null) memberships.addAll(dto.organizationUnitListIds);
        for(Long id:memberships) if(!Objects.equals(references.organization(id).tenantId,unit.tenantId)) throw ApiException.badRequest("Membership belongs to another instance");
        var entity=mapper.map(dto,ExternalCollaborator.class);entity.organizationUnitListIds=memberships;
        if(dto.dateOfBirth!=null) entity.age=Period.between(dto.dateOfBirth,LocalDate.now()).getYears();
        if(old==null) entity.id=null;
        else {entity.createdBy=old.createdBy;entity.createdDate=old.createdDate;entity.modifiedDate=LocalDateTime.now();}
        // hasAccess is source-system metadata, never an authentication grant.
        return repository.saveAndFlush(entity).id;
    }
    public PageResult<ExternalCollaboratorDto> list(ListRequest request) {
        return queries.list(ExternalCollaborator.class,request,"organizationUnitId","firstName",this::response);
    }
    private ExternalCollaboratorDto response(ExternalCollaborator entity) {
        var dto=mapper.map(entity,ExternalCollaboratorDto.class);
        dto.password=null;dto.organizationUnitName=references.organizationName(entity.organizationUnitId);
        dto.userRoleName=roles.findById(entity.roleId).map(r -> r.name).orElse(null);
        dto.userRoleIds=List.of(entity.roleId);dto.workerTypeName="External Collaborator";
        dto.fullName=entity.firstName+" "+entity.lastName+" ()";
        if(entity.dateOfBirth!=null) dto.age=Period.between(entity.dateOfBirth,LocalDate.now()).getYears();
        dto.externalDetails=new ExternalCollaboratorDto.ExternalDetails();dto.externalDetails.companyName=entity.companyName;
        dto.externalDetails.designation=entity.designation;dto.externalDetails.status=entity.status;
        return dto;
    }
}
