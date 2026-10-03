package com.ehspro.service;

import com.ehspro.entity.*;
import com.ehspro.exception.ApiException;
import com.ehspro.repository.*;
import org.springframework.stereotype.Service;

@Service
public class ReferenceService {
    private final OrganizationUnitRepository organizations;
    private final com.ehspro.security.AccessService access;
    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final DesignationRepository designations;
    public ReferenceService(OrganizationUnitRepository organizations, EmployeeRepository employees,
            DepartmentRepository departments, DesignationRepository designations, com.ehspro.security.AccessService access) {
        this.access=access; this.organizations = organizations; this.employees = employees;
        this.departments = departments; this.designations = designations;
    }
    public OrganizationUnit organization(Long id) {
        if (id == null) throw ApiException.badRequest("Organization ID is required");
        access.organization(id);
        return organizations.findById(id).orElseThrow(() -> ApiException.notFound("Organization not found: " + id));
    }
    public String organizationName(Long id) { return id == null ? null : organizations.findById(id).map(o -> o.name).orElse(null); }
    public void employee(Long id) {
        if (id == null || !employees.existsById(id)) throw ApiException.notFound("Employee not found: " + id);
        access.organization(employees.findById(id).orElseThrow().organizationUnitId);
    }
    public String employeeName(Long id) { return id == null ? "" : employees.findById(id).map(this::fullName).orElse(""); }
    public String fullName(Employee e) {
        String name = java.util.stream.Stream.of(e.firstName, e.middleName, e.lastName)
            .filter(s -> s != null && !s.isBlank()).collect(java.util.stream.Collectors.joining(" "));
        return e.userNumber == null ? name : name + " (" + e.userNumber + ")";
    }
    public String departmentName(Long id) { return id == null ? null : departments.findById(id).map(e -> e.name).orElse(null); }
    public String designationName(Long id) { return id == null ? null : designations.findById(id).map(e -> e.name).orElse(null); }
    public void checkDepartment(Long id, Long businessUnitId) {
        if (id == null || id == 0) return;
        Department d = departments.findById(id).orElseThrow(() -> ApiException.notFound("Department not found: " + id));
        if (!d.businessUnitId.equals(businessUnitId)) throw ApiException.badRequest("Department belongs to another organization");
    }
    public void checkDesignation(Long id, Long businessUnitId) {
        if (id == null || id == 0) return;
        Designation d = designations.findById(id).orElseThrow(() -> ApiException.notFound("Designation not found: " + id));
        if (!d.businessUnitId.equals(businessUnitId)) throw ApiException.badRequest("Designation belongs to another organization");
    }
    public static void creating(Long id) { if (id != null && id != 0) throw ApiException.badRequest("Create requests must have id 0 or omit id"); }
    public static String status(Integer value) { return value == null ? null : value == 1 ? "Active" : "Inactive"; }
}
