package com.ehspro;

import com.ehspro.dto.ReferenceItemDto;
import com.ehspro.service.ReferenceDataService;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class RbacIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ReferenceDataService lookups;
    final String superUser="ehs-api",superPassword="ehs-api-local",password="Rbac-test-password-123";
    long tenant;
    @BeforeEach void references() {
        tenant=Math.abs(UUID.randomUUID().getMostSignificantBits()/2);
        ReferenceItemDto country=new ReferenceItemDto();country.name="Test Country";country.code="99";country.currency="TST";country.symbol="T";
        lookups.save("COUNTRY",50001L,country);
        ReferenceItemDto state=new ReferenceItemDto();state.name="Test State";state.countryId=50001L;lookups.save("STATE",50002L,state);
        ReferenceItemDto city=new ReferenceItemDto();city.name="Test City";city.countryId=50001L;city.stateId=50002L;lookups.save("CITY",50003L,city);
        ReferenceItemDto language=new ReferenceItemDto();language.name="Test Language";language.countryId=50001L;lookups.save("LANGUAGE",50004L,language);
        ReferenceItemDto timezone=new ReferenceItemDto();timezone.name="UTC";timezone.countryId=50001L;timezone.zoneId="UTC";lookups.save("TIME_ZONE",50005L,timezone);
    }
    @Test void sameRoleHasDifferentScopesAndCannotReadOrWriteOtherUnits() throws Exception {
        long parent=organization(null,tenant),child=organization(parent,tenant),other=organization(null,tenant),foreign=organization(null,tenant+1);
        long department=department(parent),childDepartment=department(child),otherDepartment=department(other);
        department(foreign);
        long role=role(tenant,List.of(3342L));
        var a=activate(employee(parent),role,List.of(scope(parent,true)));
        var b=activate(employee(other),role,List.of(scope(other,false)));
        JsonNode list=request("POST","/api/Department/GetList",Map.of(),a.username,password,200);
        assertThat(list.path("totalCount").asInt()).isEqualTo(2);
        assertThat(request("POST","/api/Department/GetList",Map.of(),b.username,password,200).path("totalCount").asInt()).isEqualTo(1);
        request("POST","/api/Department/GetList",Map.of("businessUnitIds",Long.toString(other),"userId",1),a.username,password,403);
        request("POST","/api/Department/Create",Map.of("name","Denied","businessUnitId",other,"status",1),a.username,password,403);
        request("PUT","/api/Department/"+otherDepartment,Map.of("name","Denied edit","businessUnitId",parent,"status",1),a.username,password,403);
        request("PUT","/api/Department/"+childDepartment,Map.of("name","Allowed edit","businessUnitId",child,"status",1),a.username,password,200);
        request("POST","/api/Equipment/List",Map.of(),a.username,password,403);
        admin("PUT","/api/SystemUser/"+a.id+"/Scope",Map.of("scopes",List.of(scope(parent,true),scope(other,false))),200);
        assertThat(request("POST","/api/Department/GetList",Map.of(),a.username,password,200).path("totalCount").asInt()).isEqualTo(3);
        admin("PUT","/api/SystemUser/"+a.id+"/Scope",Map.of("scopes",List.of(scope(foreign,true))),400);
        admin("PUT","/api/SystemUser/"+a.id+"/Enabled",Map.of("enabled",false),200);
        request("POST","/api/Department/GetList",Map.of(),a.username,password,401);
    }
    @Test void rolesAreInstanceSpecificAndRestrictedActionsCannotBeDelegated() throws Exception {
        long unit=organization(null,tenant),other=organization(null,tenant+1);
        long role=role(tenant,List.of(3302L,3303L,3342L));
        long foreignRole=role(tenant+1,List.of(3342L));
        var user=activate(employee(unit),role,List.of(scope(unit,false)));
        request("POST","/api/Role/CreateRole",Map.of("name","Customer role","landingPageId",1,"permissionIds",List.of(3342)),user.username,password,200);
        JsonNode roles=request("GET","/api/Role/GetAllRoles",null,user.username,password,200);
        for(JsonNode item:roles) assertThat(item.path("tenantId").asLong()).isEqualTo(tenant);
        request("POST","/api/Role/CreateRole",Map.of("name","Forbidden grant","landingPageId",1,"permissionIds",List.of(3301)),user.username,password,400);
        request("POST","/api/Role/CreateRole",Map.of("name","Unknown grant","landingPageId",1,"permissionIds",List.of(999999)),user.username,password,400);
        request("PUT","/api/Role/"+role,Map.of("name","No","landingPageId",1,"permissionIds",List.of(3342)),user.username,password,403);
        request("PUT","/api/SystemUser/"+user.id+"/Roles",Map.of("basicRoleId",role),user.username,password,403);
        admin("PUT","/api/SystemUser/"+user.id+"/Roles",Map.of("basicRoleId",foreignRole),400);
        admin("PUT","/api/Role/-1",Map.of("name","Hacked","landingPageId",1,"permissionIds",List.of(3342)),400);
        request("POST","/api/OrganizationUnit",organizationBody(null,tenant),user.username,password,403);
        JsonNode permissions=request("GET","/api/Permission/GetAll",null,user.username,password,200);
        for(JsonNode permission:permissions) if(permission.path("superAdminOnly").asBoolean()) {
            assertThat(permission.path("disabled").asBoolean()).isTrue();
            assertThat(permission.path("selectable").asBoolean()).isFalse();
        }
        long observer=role(tenant,List.of(2033L));
        admin("PUT","/api/SystemUser/"+user.id+"/Roles",Map.of("basicRoleId",observer),200);
        request("POST","/api/Department/GetList",Map.of(),user.username,password,403);
    }
    @Test void systemUsersComeFromEmployeeMastersAndContractEmployeesRequireContractor() throws Exception {
        long unit=organization(null,tenant);
        long employee=employee(unit);
        assertThat(admin("POST","/api/User/GetUsers",Map.of("businessUnitIds",Long.toString(unit)),200).path("totalCount").asInt()).isZero();
        long role=role(tenant,List.of(3342L));
        var account=activate(employee,role,List.of(scope(unit,false)));
        assertThat(admin("POST","/api/User/GetUsers",Map.of("businessUnitIds",Long.toString(unit)),200).path("totalCount").asInt()).isEqualTo(1);
        JsonNode contractor=admin("POST","/api/Contractor/Create",Map.of("name","Contract Company","businessUnitId",unit,"status",1),200);
        ObjectNode body=employeeBody(unit);body.remove("department");
        admin("POST","/api/ContractEmployee/Create",body,400);
        body.put("contractorId",contractor.path("id").asLong());
        admin("POST","/api/ContractEmployee/Create",body,200);
        assertThat(admin("POST","/api/ContractEmployee/GetList",Map.of("businessUnitIds",Long.toString(unit)),200).path("totalCount").asInt()).isEqualTo(1);
        request("POST","/api/User/"+employee+"/ActivateSystemUser",Map.of("username","blocked","password",password,"basicRoleId",role,"scopes",List.of(scope(unit,false))),account.username,password,403);
    }
    @Test void workbookFieldsValidateAndOrganizationDerivesCountryMetadata() throws Exception {
        ObjectNode body=organizationBody(null,tenant);body.put("name","X".repeat(51));admin("POST","/api/OrganizationUnit",body,400);
        body.put("name","Workbook "+UUID.randomUUID().toString().substring(0,8));body.put("currency","WRONG");body.put("countryCode","WRONG");
        body.putArray("shifts").addObject().put("name","Night").put("startTime","22:00").put("endTime","06:00");
        admin("POST","/api/OrganizationUnit",body,200);
        JsonNode tree=admin("GET","/api/OrganizationUnit/GetAllOrganizations",null,200);
        JsonNode created=null;for(JsonNode unit:tree) if(unit.path("name").asText().equals(body.path("name").asText())) created=unit;
        assertThat(created).isNotNull();assertThat(created.path("countryCode").asText()).isEqualTo("99");assertThat(created.path("currency").asText()).isEqualTo("TST");
        assertThat(created.at("/shifts/0/startTime").asText()).startsWith("22:00");
        body.put("state",999999);admin("POST","/api/OrganizationUnit",body,400);
        body.put("state",50002);body.put("latitude",20);admin("POST","/api/OrganizationUnit",body,400);
        ObjectNode employee=employeeBody(created.path("id").asLong());employee.remove("lastName");admin("POST","/api/User/CreateEmployee",employee,400);
    }
    @Test void adminsCanCreateEditAndAssignDynamicRolesWithinTheirInstanceAndScope() throws Exception {
        long unit=organization(null,tenant),outside=organization(null,tenant),foreign=organization(null,tenant+1);
        long adminRole=role(tenant,List.of(3302L,3272L,3274L));
        long viewer=role(tenant,List.of(2033L));
        var admin=activate(employee(unit),adminRole,List.of(scope(unit,false)));
        var target=activate(employee(unit),viewer,List.of(scope(unit,false)));
        var out=activate(employee(outside),viewer,List.of(scope(outside,false)));
        long foreignRole=role(tenant+1,List.of(3342L));
        var other=activate(employee(foreign),foreignRole,List.of(scope(foreign,false)));
        var body=Map.of("name","Custom department administrator","landingPageId",1,"permissionIds",List.of(3342));
        long created=request("POST","/api/Role/CreateRole",body,admin.username,password,200).path("id").asLong();
        request("PUT","/api/Role/"+created,body,admin.username,password,200);
        request("PUT","/api/Role/"+foreignRole,body,admin.username,password,403);
        request("PUT","/api/Role/-1",body,admin.username,password,400);
        request("PUT","/api/SystemUser/"+target.id+"/Roles",Map.of("basicRoleId",created),admin.username,password,200);
        request("POST","/api/Department/GetList",Map.of(),target.username,password,200);
        request("PUT","/api/SystemUser/"+out.id+"/Roles",Map.of("basicRoleId",created),admin.username,password,403);
        request("PUT","/api/SystemUser/"+other.id+"/Roles",Map.of("basicRoleId",created),admin.username,password,403);
        request("PUT","/api/SystemUser/"+target.id+"/Roles",Map.of("basicRoleId",foreignRole),admin.username,password,400);
    }
    @Test void documentPermissionTreeResolvesSourceIdsWithoutChangingExistingAuthorities() throws Exception {
        var payload=(ObjectNode)mapper.readTree(getClass().getResourceAsStream("/document-role-payload.json"));
        payload.put("tenantId",tenant).put("status",1);
        long roleId=admin("POST","/api/Role/CreateRole",payload,200).path("id").asLong();
        var all=admin("GET","/api/Role/GetAllRoles",null,200);
        JsonNode created=null;for(var role:all) if(role.path("id").asLong()==roleId) created=role;
        assertThat(created).isNotNull();
        Set<String> codes=new HashSet<>();created.path("permissions").forEach(p -> codes.add(p.path("name").asText()));
        assertThat(codes).contains("ViewObservation","SubmitIncident","ViewIncident","IncidentDashboard","ObservationDashboard","MyTasks");
        // 3301 means a different action in the source tree; flat permissionIds retain canonical meaning.
        admin("POST","/api/Role/CreateRole",Map.of("name","Source permission","landingPageId",1,"permissionLookupHierarchyDto",
            List.of(Map.of("id",3301,"name","ViewShiftInspectionChecklist","isGranted",true))),200);
        admin("POST","/api/Role/CreateRole",Map.of("name","Reserved permission","landingPageId",1,"permissionIds",List.of(3301)),400);
        admin("POST","/api/Role/CreateRole",Map.of("name","Mismatched permission","landingPageId",1,"permissionLookupHierarchyDto",
            List.of(Map.of("id",3301,"name","InventedPermission","isGranted",true))),400);
    }
    @Test void completeContractorAndTemporaryUserContractsPersistAndValidate() throws Exception {
        long unit=organization(null,tenant),outside=organization(null,tenant),foreign=organization(null,tenant+1);
        ObjectNode contractor=mapper.createObjectNode();contractor.put("contractorName","Contract Co").put("businessUnitId",unit).put("status",1);
        contractor.put("contractorCode","C-123").put("servicesOffered","Maintenance").put("addressLine1","Test Road")
            .put("countryId",50001).put("stateId",50002).put("cityId",50003).put("email","test@example.com").put("website","example.com");
        assertThat(admin("POST","/api/Contractor/Add",contractor,200).asText()).isEqualTo("Contractor added successfully.");
        var company=admin("POST","/api/Contractor/GetAllContractors",Map.of("businessUnitIds",Long.toString(unit),"sorting","contractorName"),200).at("/items/0");
        assertThat(company.path("contractorCode").asText()).isEqualTo("C-123");
        assertThat(company.path("country").asText()).isEqualTo("Test Country");
        contractor.put("cityId",999999);admin("POST","/api/Contractor/Add",contractor,400);
        ObjectNode employee=employeeBody(unit);employee.put("userType",2).put("department",0).put("contractorId",company.path("id").asLong());
        long managerRole=role(tenant,List.of(3355L,3357L,3274L));
        var manager=activate(employee(unit),managerRole,List.of(scope(unit,false)));
        request("POST","/api/User/CreateEmployee",employee,manager.username,password,200);
        assertThat(request("POST","/api/User/GetAllContractEmployees",Map.of(),manager.username,password,200).path("totalCount").asInt()).isEqualTo(1);
        long role=role(tenant,List.of(2033L));
        ObjectNode temp=mapper.createObjectNode();temp.put("id",0).put("roleId",role).put("organizationUnitId",unit)
            .put("firstName","Temporary").put("lastName","Auditor").put("gender",1).put("status",1).put("country",50001).put("hasAccess",true);
        temp.putObject("externalDetails").put("companyName","Audit Co").put("designation","Auditor").put("status",1);
        long id=request("POST","/api/User/CreateOrUpdateExternalCollabarator",temp,manager.username,password,200).asLong();
        var page=request("POST","/api/User/GetAllExternalCollabarator",Map.of(),manager.username,password,200);
        assertThat(page.path("totalCount").asInt()).isEqualTo(1);
        assertThat(page.at("/items/0/externalDetails/designation").asText()).isEqualTo("Auditor");
        assertThat(page.at("/items/0/password").isNull()).isTrue();
        temp.put("id",id).put("lastName","Updated");request("POST","/api/User/CreateOrUpdateExternalCollabarator",temp,manager.username,password,200);
        temp.put("organizationUnitId",outside);request("POST","/api/User/CreateOrUpdateExternalCollabarator",temp,manager.username,password,400);
        temp.put("id",0);request("POST","/api/User/CreateOrUpdateExternalCollabarator",temp,manager.username,password,403);
        temp.put("organizationUnitId",unit).put("roleId",role(tenant+1,List.of(2033L)));
        request("POST","/api/User/CreateOrUpdateExternalCollabarator",temp,manager.username,password,400);
        temp.put("roleId",role).putNull("firstName");admin("POST","/api/User/CreateOrUpdateExternalCollabarator",temp,400);
    }
    private ObjectNode organizationBody(Long parent,long tenantId) {
        ObjectNode node=mapper.createObjectNode();node.put("name","BU-"+UUID.randomUUID().toString().substring(0,12));node.put("tenantId",tenantId);if(parent!=null)node.put("parentId",parent);
        node.put("line1","Test address").put("country",50001).put("state",50002).put("city",50003).put("languageID",50004).put("timeZoneID",50005);
        node.put("keyContactName","Test Contact").put("phoneNumber","9000000000").put("emailAddress","test@example.com").put("status",1);return node;
    }
    private long organization(Long parent,long instance) throws Exception {
        ObjectNode body=organizationBody(parent,instance);admin("POST","/api/OrganizationUnit",body,200);
        JsonNode tree=admin("GET","/api/OrganizationUnit/GetAllOrganizations",null,200);
        return findUnit(tree,body.path("name").asText());
    }
    private long findUnit(JsonNode nodes,String name) {
        for(JsonNode n:nodes) { if(n.path("name").asText().equals(name))return n.path("id").asLong();long nested=findUnit(n.path("children"),name);if(nested>0)return nested; }return 0;
    }
    private long department(long unit) throws Exception {
        JsonNode existing=admin("POST","/api/Department/GetList",Map.of("businessUnitIds",Long.toString(unit)),200);
        if(existing.path("totalCount").asInt()>0) return existing.at("/items/0/id").asLong();
        return admin("POST","/api/Department/Create",Map.of("name","Department","businessUnitId",unit,"status",1),200).path("id").asLong();
    }
    private long role(long instance,List<Long> permissions) throws Exception { return admin("POST","/api/Role/CreateRole",Map.of("name","Role-"+UUID.randomUUID(),"tenantId",instance,"landingPageId",1,"permissionIds",permissions),200).path("id").asLong(); }
    private ObjectNode employeeBody(long unit) throws Exception {
        long department=department(unit);
        long designation=admin("POST","/api/Designation/Create",Map.of("name","Designation","businessUnitId",unit,"status",1),200).path("id").asLong();
        ObjectNode body=mapper.createObjectNode();body.put("firstName","Test").put("lastName","Employee").put("gender",1).put("status",1).put("languageID",50004);
        body.put("userNumber",UUID.randomUUID().toString()).put("organizationUnitId",unit).put("department",department).put("designation",designation);return body;
    }
    private long employee(long unit) throws Exception { return admin("POST","/api/User/CreateEmployee",employeeBody(unit),200).asLong(); }
    private Map<String,Object> scope(long unit,boolean descendants) { return Map.of("organizationUnitId",unit,"includeDescendants",descendants); }
    private record Account(long id,String username) {}
    private Account activate(long employee,long role,List<Map<String,Object>> scopes) throws Exception {
        String username="user-"+UUID.randomUUID();JsonNode result=admin("POST","/api/User/"+employee+"/ActivateSystemUser",Map.of("username",username,"password",password,"basicRoleId",role,"scopes",scopes),200);
        return new Account(result.path("id").asLong(),username);
    }
    private JsonNode admin(String method,String path,Object body,int expected) throws Exception { return request(method,path,body,superUser,superPassword,expected); }
    private JsonNode request(String method,String path,Object body,String username,String secret,int expected) throws Exception {
        var request=org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method),path).with(httpBasic(username,secret));
        if(body!=null)request.contentType("application/json").content(mapper.writeValueAsBytes(body));
        var result=mvc.perform(request).andExpect(status().is(expected)).andExpect(jsonPath("$.statusCode").value(expected)).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).path("results");
    }
}
