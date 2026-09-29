package com.ehspro.service;

import com.ehspro.dto.*;
import com.ehspro.entity.Employee;
import com.ehspro.exception.ApiException;
import com.ehspro.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EmployeeService {
    private final EmployeeRepository repository;
    private final com.ehspro.security.AccessService access;
    private final ReferenceDataService lookups;
    private final ContractorRepository contractors;
    private final UserAccountRepository accounts;
    private final RoleRepository roles;
    private final DtoMapper mapper;
    private final ListQueryService queries;
    private final ReferenceService references;
    public EmployeeService(EmployeeRepository repository, RoleRepository roles, DtoMapper mapper,
            ListQueryService queries, ReferenceService references, com.ehspro.security.AccessService access, ReferenceDataService lookups, ContractorRepository contractors, UserAccountRepository accounts) {
        this.repository = repository; this.roles = roles; this.mapper = mapper;
        this.queries = queries; this.references = references; this.access=access; this.lookups=lookups; this.contractors=contractors; this.accounts=accounts;
    }
    @Transactional
    public Long create(EmployeeDto dto) {
        ReferenceService.creating(dto.id);
        return repository.saveAndFlush(validated(dto, null)).id;
    }
    @Transactional
    public Long update(Long id, EmployeeDto dto) {
        Employee old=repository.findById(id).orElseThrow(() -> ApiException.notFound("Employee not found"));
        references.organization(old.organizationUnitId);
        access.require(Integer.valueOf(2).equals(old.userType) ? "ManageContractEmployees" : "ManageEmployees");
        if(!Objects.equals(old.organizationUnitId,dto.organizationUnitId)) throw ApiException.badRequest("Employee cannot be moved between organizations");
        if(!Objects.equals(old.hasAccess,dto.hasAccess)) access.superAdmin();
        Employee entity=validated(dto, old);entity.id=id;entity.createdBy=old.createdBy;entity.createdDate=old.createdDate;
        entity.modifiedDate=java.time.LocalDateTime.now();
        return repository.saveAndFlush(entity).id;
    }
    private Employee validated(EmployeeDto dto, Employee old) {
        var unit=references.organization(dto.organizationUnitId);
        dto.tenantID=unit.tenantId;
        lookups.employee(dto);
        if(dto.userType==null) dto.userType=1;
        access.require(dto.userType==2 ? "ManageContractEmployees" : "ManageEmployees");
        if(dto.userType!=1 && dto.userType!=2) throw ApiException.badRequest("userType must be 1 (employee) or 2 (contract employee)");
        if(dto.userType==1 && (dto.department==null||dto.department<=0)) throw ApiException.badRequest("Department is required for employees");
        if(dto.userType==2) {
            if(dto.contractorId==null||dto.contractorId<=0) throw ApiException.badRequest("Contractor company is required");
            var contractor=contractors.findById(dto.contractorId).orElseThrow(() -> ApiException.badRequest("Contractor not found"));
            if(!contractor.businessUnitId.equals(dto.organizationUnitId)) throw ApiException.badRequest("Contractor belongs to another organization");
        }
                boolean rolesSupplied=dto.userRoleIds!=null&&!dto.userRoleIds.isEmpty();
        if(old==null && (Boolean.TRUE.equals(dto.hasAccess)||rolesSupplied)) access.superAdmin();
        if(old!=null && !Objects.equals(old.userRoleIds,dto.userRoleIds)) throw ApiException.badRequest("Change system user roles through the dedicated Roles API");
        references.checkDepartment(dto.department, dto.organizationUnitId);
        references.checkDesignation(dto.designation, dto.organizationUnitId);
        if (dto.dateOfBirth != null && dto.dateOfBirth.isAfter(LocalDate.now())) throw ApiException.badRequest("dateOfBirth cannot be in the future");
        Employee entity = mapper.map(dto, Employee.class);
        entity.id = null;
        if (entity.dateOfBirth != null) entity.age = Period.between(entity.dateOfBirth, LocalDate.now()).getYears();
        Set<Long> organizationIds = new LinkedHashSet<>();
        organizationIds.add(dto.organizationUnitId);
        if (dto.organizationUnitIds != null) organizationIds.addAll(dto.organizationUnitIds);
        if (dto.organizationUnitListIds != null) organizationIds.addAll(dto.organizationUnitListIds);
        if (dto.organizationUnitIdsMapped != null) organizationIds.addAll(dto.organizationUnitIdsMapped);
                organizationIds.forEach(id -> {
            if(!Objects.equals(references.organization(id).tenantId,unit.tenantId)) throw ApiException.badRequest("Employee memberships must stay within the instance");
        });
        entity.organizationUnitIds = new ArrayList<>(organizationIds);
        entity.organizationUnitListIds = new ArrayList<>(organizationIds);
        entity.organizationUnitIdsMapped = new ArrayList<>(organizationIds);
        if (entity.userRoleIds == null) entity.userRoleIds = new ArrayList<>();
                entity.userRoleIds.forEach(id -> {
            var role=roles.findById(id).orElseThrow(() -> ApiException.badRequest("Role not found"));
            if(role.systemRole||!Objects.equals(role.tenantId,unit.tenantId)) throw ApiException.badRequest("Role is not assignable in this instance");
        });
        return entity;
    }
    public PageResult<EmployeeDto> list(ListRequest request) {
        return queries.list(Employee.class, request, "organizationUnitId", "firstName", this::response);
    }
    public PageResult<EmployeeDto> listUsers(ListRequest request) {
        request.systemUsersOnly=true;
        return list(request);
    }
    private EmployeeDto response(Employee entity) {
        EmployeeDto dto = mapper.map(entity, EmployeeDto.class);
        dto.password = null;
        accounts.findByEmployeeId(entity.id).ifPresent(account -> {
            dto.systemAccountId=account.id;dto.basicRoleId=account.basicRoleId;
            dto.additionalRoleIds=new HashSet<>(account.additionalRoleIds);dto.systemUsername=account.username;dto.systemAccountEnabled=account.enabled;
        });
        if(entity.dateOfBirth!=null) dto.age=Period.between(entity.dateOfBirth,LocalDate.now()).getYears();
        dto.countryName=lookups.name("COUNTRY",entity.country);
        dto.contractorName=entity.contractorId==null ? null : contractors.findById(entity.contractorId).map(c -> c.name).orElse(null);
        dto.departmentName = references.departmentName(entity.department);
        dto.designationName = references.designationName(entity.designation);
        dto.organizationUnitName = references.organizationName(entity.organizationUnitId);
        dto.organizationUnitNames = entity.organizationUnitIds.stream().map(references::organizationName)
            .filter(Objects::nonNull).collect(Collectors.joining(", "));
        dto.fullName = references.fullName(entity);
        dto.workerTypeName = Integer.valueOf(1).equals(entity.userType) ? "Employee" : "Contract Employee";
        dto.userRoleNames = roles.findAllById(entity.userRoleIds).stream().map(r -> r.name).collect(Collectors.joining(", "));
        return dto;
    }
}
