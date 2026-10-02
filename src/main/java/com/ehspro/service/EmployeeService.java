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
    private final com.ehspro.security.AccountDetailsService identities;
    private final com.ehspro.repository.PermissionDefinitionRepository permissions;
    private final DtoMapper mapper;
    private final ListQueryService queries;
    private final ReferenceService references;
    public EmployeeService(EmployeeRepository repository, RoleRepository roles, DtoMapper mapper,
            ListQueryService queries, ReferenceService references, com.ehspro.security.AccessService access,
            ReferenceDataService lookups, ContractorRepository contractors,
            UserAccountRepository accounts, com.ehspro.security.AccountDetailsService identities,
            com.ehspro.repository.PermissionDefinitionRepository permissions) {
        this.repository = repository; this.roles = roles; this.mapper = mapper;
        this.queries = queries; this.references = references; this.access=access; this.lookups=lookups; this.contractors=contractors; this.accounts=accounts;
        this.identities=identities;this.permissions=permissions;
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
        if(!Objects.equals(old.hasAccess,dto.hasAccess)) access.administrator();
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
        if(old==null && (Boolean.TRUE.equals(dto.hasAccess)||rolesSupplied)) access.administrator();
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
            if(role.builtInAdmin) access.administrator();
            if((role.systemRole&&!role.builtInAdmin)||(!role.builtInAdmin&&!Objects.equals(role.tenantId,unit.tenantId))) throw ApiException.badRequest("Role is not assignable in this instance");
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
    private Employee readableEmployee(Long id) { return readableEmployee(id,true); }
    private Employee readableEmployee(Long id,boolean enforceMembershipAccess) {
        Employee employee=repository.findById(id).orElseThrow(() -> ApiException.notFound("Employee not found"));
        boolean self=!access.isSuperAdmin()&&accounts.findById(access.principal().accountId).map(a -> a.employeeId.equals(id)).orElse(false);
        if(!self) {
            access.require(Integer.valueOf(2).equals(employee.userType)?"ManageContractEmployees":"ManageEmployees");
            references.organization(employee.organizationUnitId);
            if(enforceMembershipAccess) employee.organizationUnitIds.forEach(access::organization);
        }
        return employee;
    }
    public com.fasterxml.jackson.databind.node.ObjectNode get(Long id) {
        Employee employee=readableEmployee(id);
        // GET has the frontend's checked-membership objects; write DTOs retain their numeric ID lists.
        var json=new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();
        json.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        com.fasterxml.jackson.databind.node.ObjectNode result=json.valueToTree(response(employee));
        var mapped=result.putArray("organizationUnitIdsMapped");
        employee.organizationUnitIds.stream().distinct().forEach(unit -> mapped.addObject().put("organizationUnitId",unit).put("isChecked",true));
        return result;
    }
    public List<Map<String,Object>> organizations(Long id) {
        if(Long.valueOf(-1).equals(id)) {
            if(!access.isSuperAdmin()) throw new org.springframework.security.access.AccessDeniedException("Only Super Admin can read the reserved user");
            List<Map<String,Object>> result=new ArrayList<>();
            Set<String> superPermissions=permissions.findAll().stream().filter(p -> p.businessAction).map(p -> p.code)
                .collect(Collectors.toCollection(TreeSet::new));
            for(Long unitId:new TreeSet<>(access.organizationIds())) {
                var unit=references.organization(unitId);
                result.add(organizationRow(-1L,unit,Set.of("User","SuperAdmin"),superPermissions,Set.of()));
            }
            return result;
        }
        Employee employee=readableEmployee(id,false);
        var account=accounts.findByEmployeeId(employee.id);
        if(account.isPresent()) {
            var target=account.get();
            if(!target.enabled||target.mustChangePassword||!Boolean.TRUE.equals(employee.hasAccess)||!Integer.valueOf(1).equals(employee.status))
                return List.of();
            Set<Long> roleIds=new HashSet<>(target.additionalRoleIds);roleIds.add(target.basicRoleId);
            var activeRoles=roles.findAllById(roleIds).stream().filter(r -> Integer.valueOf(1).equals(r.status))
                .filter(r -> r.builtInAdmin||(!r.systemRole&&Objects.equals(r.tenantId,target.tenantId))).toList();
            boolean admin=activeRoles.stream().anyMatch(r -> r.builtInAdmin);
            Set<String> roleNames=activeRoles.stream().map(r -> r.builtInAdmin?"Admin":r.name)
                .filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
            roleNames.add("User");
            Set<String> permissionCodes=identities.loadUserByUsername(target.username).getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .filter(code -> !code.startsWith("ROLE_")&&!code.equals("PASSWORD_CHANGE_REQUIRED"))
                .collect(Collectors.toCollection(TreeSet::new));
            List<Map<String,Object>> result=new ArrayList<>();
            for(Long unitId:new TreeSet<>(access.organizationIds(target,admin))) {
                var unit=references.organization(unitId);
                result.add(organizationRow(employee.id,unit,roleNames,permissionCodes,roleIds));
            }
            return result;
        }
        List<Map<String,Object>> result=new ArrayList<>();
        for(Long unitId:new LinkedHashSet<>(employee.organizationUnitIds)) {
            // Returning memberships does not grant account scope or permissions.
            var unit=references.organization(unitId);
            result.add(organizationRow(employee.id,unit,null,null,null));
        }
        return result;
    }
    private Map<String,Object> organizationRow(Long userId,com.ehspro.entity.OrganizationUnit unit,
            Set<String> roleNames,Set<String> permissionCodes,Set<Long> roleIds) {
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("userId",userId);row.put("buImage",unit.buImage);
        row.put("organizationUnitId",unit.id);row.put("organizationUnitName",unit.name);
        row.put("isAnonymous",unit.isAnonymous);row.put("isObservationProofRequired",unit.isObservationProofRequired);
        row.put("languageId",unit.languageId);row.put("currency",unit.currency);
        if(roleNames!=null) {row.put("roleIds",new TreeSet<>(roleIds));row.put("roles",roleNames);row.put("permissions",permissionCodes);}
        return row;
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
