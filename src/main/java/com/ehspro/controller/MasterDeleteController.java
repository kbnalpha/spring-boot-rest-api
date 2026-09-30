package com.ehspro.controller;
import com.ehspro.dto.ApiResponse;
import com.ehspro.service.MasterDeleteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class MasterDeleteController {
    private final MasterDeleteService service;
    public MasterDeleteController(MasterDeleteService service) { this.service=service; }
    @DeleteMapping("/OrganizationUnit/{id}")
    public ApiResponse<?> deleteOrganizationUnit(@PathVariable Long id) { service.delete("OrganizationUnit",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/Department/{id}")
    public ApiResponse<?> deleteDepartment(@PathVariable Long id) { service.delete("Department",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/Designation/{id}")
    public ApiResponse<?> deleteDesignation(@PathVariable Long id) { service.delete("Designation",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/Location/{id}")
    public ApiResponse<?> deleteLocation(@PathVariable Long id) { service.delete("Location",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/OperationActivity/{id}")
    public ApiResponse<?> deleteOperationActivity(@PathVariable Long id) { service.delete("OperationActivity",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/ObservationType/{id}")
    public ApiResponse<?> deleteObservationType(@PathVariable Long id) { service.delete("ObservationType",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/Equipment/{id}")
    public ApiResponse<?> deleteEquipment(@PathVariable Long id) { service.delete("Equipment",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/Contractor/{id}")
    public ApiResponse<?> deleteContractor(@PathVariable Long id) { service.delete("Contractor",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/Role/{id}")
    public ApiResponse<?> deleteRole(@PathVariable Long id) { service.delete("Role",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/User/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Long id) { service.delete("User",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/ContractEmployee/{id}")
    public ApiResponse<?> deleteContractEmployee(@PathVariable Long id) { service.delete("ContractEmployee",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping({"/ExternalCollaborator/{id}","/User/ExternalCollaborator/{id}"})
    public ApiResponse<?> deleteExternalCollaborator(@PathVariable Long id) { service.delete("ExternalCollaborator",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/SystemUser/{id}")
    public ApiResponse<?> deleteSystemUser(@PathVariable Long id) { service.delete("SystemUser",id); return ApiResponse.success("Deleted successfully."); }
    @DeleteMapping("/Lookup/{kind}/{id}")
    public ApiResponse<?> deleteLookup(@PathVariable String kind,@PathVariable Long id) { service.deleteLookup(kind,id); return ApiResponse.success("Deleted successfully."); }
}
