package com.ehspro.service;

import com.ehspro.entity.*;
import com.ehspro.exception.ApiException;
import com.ehspro.security.AccessService;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Explicit master deletion: referenced records are retained, never recursively erased. */
@Service
@Transactional
public class MasterDeleteService {
    private final EntityManager em;
    private final AccessService access;
    public MasterDeleteService(EntityManager em,AccessService access) {this.em=em;this.access=access;}
    private <T> T find(Class<T> type,Long id) {
        T value=em.find(type,id);
        if(value==null) throw ApiException.notFound("Record not found");
        return value;
    }
    private void unused(boolean used) {
        if(used) throw new ApiException(HttpStatus.CONFLICT,"Record is in use. Remove or reassign its dependencies before deleting.");
    }
    private void reference(String entity,String field,Long id) {
        unused(em.createQuery("select count(e) from "+entity+" e where e."+field+"=:id",Long.class).setParameter("id",id).getSingleResult()>0);
    }
    public void delete(String resource,Long id) {
        access.administrator();
        Object entity;
        switch(resource) {
            case "OrganizationUnit" -> {var e=find(OrganizationUnit.class,id);access.organization(e.id);entity=e;}
            case "Department" -> {var e=find(Department.class,id);access.organization(e.businessUnitId);reference("Employee","department",id);entity=e;}
            case "Designation" -> {var e=find(Designation.class,id);access.organization(e.businessUnitId);reference("Employee","designation",id);entity=e;}
            case "Contractor" -> {var e=find(Contractor.class,id);access.organization(e.businessUnitId);reference("Employee","contractorId",id);entity=e;}
            case "Location" -> {var e=find(Location.class,id);access.organization(e.organizationUnitId);entity=e;}
            case "Equipment" -> {var e=find(Equipment.class,id);access.organization(e.organizationUnitId);entity=e;}
            case "OperationActivity" -> {var e=find(OperationActivity.class,id);access.organization(e.businessUnitId);entity=e;}
            case "ObservationType" -> {var e=find(ObservationType.class,id);access.organization(e.businessUnitId);entity=e;}
            case "ExternalCollaborator" -> {var e=find(ExternalCollaborator.class,id);access.organization(e.organizationUnitId);e.organizationUnitListIds.forEach(access::organization);entity=e;}
            case "Role" -> {
                var e=find(Role.class,id);access.tenant(e.tenantId);
                if(e.systemRole||e.builtInAdmin) throw new ApiException(HttpStatus.CONFLICT,"Built-in roles cannot be deleted");
                for(var employee:em.createQuery("from Employee",Employee.class).getResultList()) unused(employee.userRoleIds!=null&&employee.userRoleIds.contains(id));
                entity=e;
            }
            case "User", "ContractEmployee" -> {
                var e=find(Employee.class,id);access.organization(e.organizationUnitId);e.organizationUnitIds.forEach(access::organization);
                if(resource.equals("ContractEmployee")&&!Integer.valueOf(2).equals(e.userType)) throw ApiException.notFound("Contract employee not found");
                reference("UserAccount","employeeId",id);
                for(var location:em.createQuery("from Location",Location.class).getResultList()) unused(location.supervisorIds!=null&&location.supervisorIds.contains(id));
                entity=e;
            }
            case "SystemUser" -> {
                var e=find(UserAccount.class,id);access.tenant(e.tenantId);
                var employee=find(Employee.class,e.employeeId);access.organization(employee.organizationUnitId);
                Set<Long> roleIds=new HashSet<>(e.additionalRoleIds);roleIds.add(e.basicRoleId);
                boolean adminAccount=roleIds.stream().map(r -> find(Role.class,r)).anyMatch(r -> r.builtInAdmin);
                e.scopes.forEach(s -> access.requireScopeWithinAccess(s.organizationUnitId,s.includeDescendants||adminAccount));
                employee.hasAccess=false;employee.userRoleIds=new ArrayList<>();entity=e;
            }
            default -> throw ApiException.notFound("Resource not found");
        }
        em.remove(entity);
        // Flush inside the transaction so FK failures roll back all dependent changes.
        em.flush();
    }
    public void deleteLookup(String kind,Long id) {
        access.administrator();
        var item=em.find(ReferenceItem.class,new ReferenceItemId(kind,id));
        if(item==null) throw ApiException.notFound("Lookup not found");
        switch(kind) {
            case "COUNTRY" -> {reference("ReferenceItem","countryId",id);reference("OrganizationUnit","country",id);reference("Employee","country",id);reference("Contractor","countryId",id);reference("ExternalCollaborator","country",id);}
            case "STATE" -> {reference("ReferenceItem","stateId",id);reference("OrganizationUnit","state",id);reference("Contractor","stateId",id);}
            case "CITY" -> {reference("OrganizationUnit","city",id);reference("Contractor","cityId",id);}
            case "TIME_ZONE" -> reference("OrganizationUnit","timeZoneId",id);
            case "LANGUAGE" -> {
                reference("OrganizationUnit","languageId",id);reference("Employee","languageID",id);
                // Translation lists are JSON columns rather than foreign keys.
                var mapper=new com.fasterxml.jackson.databind.ObjectMapper();
                for(String table:List.of("department","designation","location","operation_activity","observation_type")) {
                    for(Object raw:em.createNativeQuery("select translations from "+table+" where translations is not null").getResultList()) {
                        try {
                            var tree=raw instanceof java.sql.Clob clob ? mapper.readTree(clob.getCharacterStream()) : mapper.readTree(raw.toString());
                            unused(tree.findValues("languageId").stream().anyMatch(v -> v.asLong()==id));
                        } catch(java.io.IOException|java.sql.SQLException ex) {throw new ApiException(HttpStatus.CONFLICT,"Cannot delete language while invalid translation data exists");}
                    }
                }
            }
            case "LANDING_PAGE" -> reference("Role","landingPageId",id);
            default -> throw ApiException.badRequest("Unknown lookup kind");
        }
        em.remove(item);em.flush();
    }
}
