package com.ehspro;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.security.test.context.support.WithMockUser(roles="SUPER_ADMIN")
class MasterApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @org.springframework.boot.test.mock.mockito.MockBean org.springframework.mail.javamail.JavaMailSender mail;
    @Autowired com.ehspro.service.ReferenceDataService lookups;

    @org.junit.jupiter.api.BeforeEach
    void referenceFixtures() {
        var country=new com.ehspro.dto.ReferenceItemDto();country.name="India";country.code="91";country.currency="INR";country.symbol="\u20b9";
        lookups.save("COUNTRY",101L,country);
        var state=new com.ehspro.dto.ReferenceItemDto();state.name="Test State";state.countryId=101L;lookups.save("STATE",4026L,state);
        var city=new com.ehspro.dto.ReferenceItemDto();city.name="Test City";city.countryId=101L;city.stateId=4026L;lookups.save("CITY",57933L,city);
        var language=new com.ehspro.dto.ReferenceItemDto();language.name="English";lookups.save("LANGUAGE",1L,language);
        var zone=new com.ehspro.dto.ReferenceItemDto();zone.name="India Standard Time";zone.countryId=101L;zone.zoneId="Asia/Kolkata";
        lookups.save("TIME_ZONE",1L,zone);lookups.save("TIME_ZONE",16L,zone);
    }

    @Test
    void openApiPublishesAllNineteenOperations() throws Exception {
        MvcResult result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        JsonNode paths = json.readTree(result.getResponse().getContentAsString()).path("paths");
        assertThat(paths.size()).isGreaterThanOrEqualTo(19);
        assertThat(paths.get("/api/Role/GetAllRoles").has("get")).isTrue();
        assertThat(paths.get("/api/ObservationType/GetAll").has("post")).isTrue();
    }

    @Test
    void allDocumentedEndpointsPersistAndReturnTheirDocumentedResultShapes() throws Exception {
        long organization = organization("Head Office");
        long department = create("/api/Department/Create", """
            {"id":0,"name":"Operations","description":"Department","status":1,
             "translations":[{"languageId":1,"name":"Operations","description":"Department"}]}
            """, "businessUnitId", organization).path("id").asLong();
        long designation = create("/api/Designation/Create", """
            {"name":"Manager","status":1,"translations":[{"languageId":1,"name":"Manager"}]}
            """, "businessUnitId", organization).path("id").asLong();
        JsonNode role = postJson("/api/Role/CreateRole", """
            {"name":"Observer","displayName":"Observer","status":1,"createdBy":4,
             "permissionLookupHierarchyDto":[{"id":1024,"name":"Observation","isGranted":false,"parentId":1024,
               "children":[{"id":2031,"name":"CreateObservation","isGranted":true,"children":[]}]}]}
            """);
        assertThat(role.path("message").asText()).isEqualTo("Role created successfully.");
        ObjectNode employee = (ObjectNode) json.readTree("""
            {"id":0,"firstName":"Test","middleName":"M","lastName":"Employee","emailAddress":"employee@example.com",
             "hasAccess":true,"isMobileUser":false,"userType":1,"languageID":1,"dateOfBirth":"1994-04-03","age":"32","status":1}
            """);
        employee.put("userNumber", UUID.randomUUID().toString());
        employee.put("department", department).put("designation", designation).put("organizationUnitId", organization);
        employee.putArray("userRoleIds").add(role.path("id").asLong());
        long employeeId = postJson("/api/User/CreateEmployee", employee.toString()).asLong();
        postJson("/api/User/" + employeeId + "/ActivateSystemUser", """
            {"basicRoleId":%d,
             "scopes":[{"organizationUnitId":%d,"includeDescendants":false}]}
            """.formatted(role.path("id").asLong(),organization));
        assertThat(employeeId).isPositive();
        JsonNode employees = list("/api/User/GetAllEmployees", organization);
        assertThat(employees.path("totalCount").asInt()).isEqualTo(1);
        assertThat(employees.at("/items/0/departmentName").asText()).isEqualTo("Operations");
        assertThat(employees.at("/items/0/designationName").asText()).isEqualTo("Manager");
        assertThat(employees.at("/items/0/password").isNull()).isTrue();
        assertThat(list("/api/User/GetUsers", organization).at("/items/0/userRoleNames").asText()).isEqualTo("Observer");

        ObjectNode location = (ObjectNode) json.readTree("""
            {"name":"Factory","locationDescription":"Main site","status":1,"subLocations":[],"subLocationIds":[],
             "newSublocations":[{"name":"Zone A","description":"First zone","status":1}],
             "translations":[{"languageId":1,"name":"Factory","description":"Main site","subLocations":[{"name":"Zone A"}]}]}
            """);
        location.put("organizationUnitId", organization).put("createdBy", employeeId);
        location.putArray("supervisorIds").add(employeeId);
        assertThat(postJson("/api/Location/add", location.toString()).asLong()).isPositive();
        JsonNode locations = list("/api/Location/GetAllLocations", organization);
        assertThat(locations.at("/items/0/subLocations/0/id").asLong()).isPositive();
        assertThat(locations.at("/items/0/translations/0/subLocations/0/subLocationId").asLong())
            .isEqualTo(locations.at("/items/0/subLocations/0/id").asLong());
        assertThat(locations.at("/items/0/supervisorNames").asText()).contains("Test M Employee");

        assertThat(create("/api/OperationActivity", """
            {"activityName":"Forklift Operations","categoryId":1,"status":1,
             "translations":[{"languageId":1,"activityName":"Forklift Operations","description":"Routine"}]}
            """, "businessUnitId", organization).path("id").asLong()).isPositive();
        assertThat(list("/api/OperationActivity/list", organization).at("/items/0/businessUnitName").asText()).isEqualTo("Head Office");
        JsonNode activities = postJson("/api/OperationActivity/list", """
            {"id":0,"businessUnitIds":"%d","userId":-1,"sorting":"","sortingType":"","filter":"",
             "filters":[],"maxResultCount":10,"skipCount":0,"multiSortMeta":[],"isExportToExcel":false}
            """.formatted(organization));
        assertThat(activities.path("totalCount").asInt()).isEqualTo(1);
        assertThat(activities.at("/items/0/activityName").asText()).isEqualTo("Forklift Operations");
        JsonNode observation = create("/api/ObservationType/Create", """
            {"observationCategoryId":4,"typeDescription":"Safe work","enableSvt":false,"status":1,
             "observationSubTypes":[{"subTypeDescription":"PPE","status":1}],
             "translations":[{"languageId":1,"typeDescription":"Safe work","observationSubTypes":[{"subTypeDescription":"PPE"}]}]}
            """, "businessUnitId", organization);
        assertThat(observation.path("id").asLong()).isPositive();
        assertThat(observation.at("/observationSubTypes/0/observationTypeId").asLong()).isEqualTo(observation.path("id").asLong());
        JsonNode observations = list("/api/ObservationType/GetAll", organization);
        assertThat(observations.at("/items/0/translations/0/observationSubTypes/0/observationSubTypeId").asLong())
            .isEqualTo(observation.at("/observationSubTypes/0/id").asLong());
        assertThat(create("/api/Equipment/Add", """
            {"equipmentCategoryId":3,"equipmentTypeId":18,"uid":"EQ-1","manufacturer":"Acme",
             "modelNumber":"M1","serialNumber":"123","yearofManufacture":"1990","status":1}
            """, "organizationUnitId", organization).asText()).isEqualTo("Equipment added successfully.");
        assertThat(list("/api/Equipment/List", organization).at("/items/0/active").asText()).isEqualTo("Active");
        assertThat(list("/api/Department/GetList", organization).at("/items/0/translations/0/name").asText()).isEqualTo("Operations");
        assertThat(list("/api/Designation/GetList", organization).at("/items/0/statusDisplay").asText()).isEqualTo("Active");
        JsonNode roles = getJson("/api/Role/GetAllRoles");
        JsonNode savedRole = null;
        for (JsonNode r : roles) if (r.path("id").asLong() == role.path("id").asLong()) savedRole = r;
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.at("/permissions/0/isGranted").asBoolean()).isTrue();
    }

    @Test
    void organizationTreeAcceptsDocumentedAliasesAndRejectsCrossTenantParents() throws Exception {
        long parent = organization("Parent");
        postJson("/api/OrganizationUnit", """
            {"name":"Child","parentId":%d,"tenantId":1,"languageID":1,"timeZoneID":16,"isAnonymous":true,"shifts":[]}
            """.formatted(parent));
        JsonNode tree = getJson("/api/OrganizationUnit/GetAllOrganizations");
        JsonNode root = null;
        for (JsonNode n : tree) if (n.path("id").asLong() == parent) root = n;
        assertThat(root).isNotNull();
        assertThat(root.at("/children/0/languageId").asLong()).isEqualTo(1);
        assertThat(root.at("/children/0/timeZoneId").asLong()).isEqualTo(16);
        assertThat(root.at("/children/0/isAnonymous").asBoolean()).isTrue();
        mvc.perform(post("/api/OrganizationUnit").contentType("application/json")
            .content("{\"name\":\"Invalid child\",\"tenantId\":2,\"parentId\":" + parent + "}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void paginationFiltersMultiSortAndBusinessUnitIsolationWorkInDatabase() throws Exception {
        long unit = organization("Paging");
        for (String name : new String[]{"Alpha", "Beta", "Gamma", "Omega"})
            create("/api/Department/Create", "{\"name\":\"" + name + "\",\"status\":1}", "businessUnitId", unit);
        JsonNode result = postJson("/api/Department/GetList", """
            {"businessUnitIds":"%d","skipCount":1,"maxResultCount":2,"sorting":"name","sortingType":"asc"}
            """.formatted(unit));
        assertThat(result.path("totalCount").asInt()).isEqualTo(4);
        assertThat(result.at("/items/0/name").asText()).isEqualTo("Beta");
        assertThat(result.at("/items/1/name").asText()).isEqualTo("Gamma");
        result = postJson("/api/Department/GetList", """
            {"businessUnitIds":"%d","filters":[{"field":"name","value":"a","matchMode":"endsWith"}],
             "multiSortMeta":[{"field":"name","order":-1}],"maxResultCount":2}
            """.formatted(unit));
        assertThat(result.at("/items/0/name").asText()).isEqualTo("Omega");
        assertThat(result.path("totalCount").asInt()).isEqualTo(4);
        assertThat(list("/api/Department/GetList", organization("Empty")).path("totalCount").asInt()).isZero();
        result = postJson("/api/Department/GetList", "{\"businessUnitIds\":\"" + unit + "\",\"filter\":\"amm\"}");
        assertThat(result.path("totalCount").asInt()).isEqualTo(1);
        result = postJson("/api/Department/GetList", """
            {"id":0,"businessUnitIds":"%d","userId":-1,"sorting":"","sortingType":"","filter":"",
             "filters":[],"maxResultCount":10,"skipCount":0,"multiSortMeta":[],"isExportToExcel":false}
            """.formatted(unit));
        assertThat(result.path("totalCount").asInt()).isEqualTo(4);
        long designation = create("/api/Designation/Create", """
            {"name":"Paging designation","status":1}
            """, "businessUnitId", unit).path("id").asLong();
        result = postJson("/api/Designation/GetList", """
            {"id":0,"businessUnitIds":"%d","userId":-1,"sorting":"","sortingType":"","filter":"",
             "filters":[],"maxResultCount":10,"skipCount":0,"multiSortMeta":[],"isExportToExcel":false}
            """.formatted(unit));
        assertThat(result.path("totalCount").asInt()).isEqualTo(1);
        assertThat(result.at("/items/0/id").asLong()).isEqualTo(designation);
        result = postJson("/api/Department/GetList", "{\"businessUnitIds\":\"" + unit + "\",\"isExportToExcel\":true,\"maxResultCount\":1}");
        assertThat(result.path("items").size()).isEqualTo(4);
    }

    @Test
    void badRequestsUnknownReferencesAndDuplicatesHaveConsistentErrors() throws Exception {
        mvc.perform(post("/api/Department/Create").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400));
        mvc.perform(post("/api/Department/Create").contentType("application/json").content("{\"name\":\"Missing\",\"businessUnitId\":999999}"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.statusCode").value(404));
        for (String request : new String[]{"{\"sorting\":\"name; drop table department\"}", "{\"skipCount\":-1}",
                "{\"maxResultCount\":0}", "{\"businessUnitIds\":\"1,invalid\"}", "{\"filters\":[{\"field\":\"status\",\"value\":\"bad\",\"matchMode\":\"equals\"}]}"})
            mvc.perform(post("/api/Department/GetList").contentType("application/json").content(request))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400));
        long unit = organization("Duplicates");
        String equipment = "{\"uid\":\"DUP\",\"equipmentCategoryId\":3,\"equipmentTypeId\":18,\"organizationUnitId\":" + unit + "}";
        postJson("/api/Equipment/Add", equipment);
        mvc.perform(post("/api/Equipment/Add").contentType("application/json").content(equipment))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.statusCode").value(409));
        mvc.perform(post("/api/Equipment/Add").contentType("application/json").content(equipment.replace("{", "{\"id\":99,")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void invalidNestedReferenceRollsBackEntireObservationCreation() throws Exception {
        long unit = organization("Rollback");
        mvc.perform(post("/api/ObservationType/Create").contentType("application/json").content("""
            {"businessUnitId":%d,"observationCategoryId":4,"typeDescription":"Rollback",
             "observationSubTypes":[{"subTypeDescription":"Child"}],
             "translations":[{"languageId":1,"observationSubTypes":[{"observationSubTypeId":999999,"subTypeDescription":"Bad"}]}]}
            """.formatted(unit))).andExpect(status().isBadRequest());
        assertThat(list("/api/ObservationType/GetAll", unit).path("totalCount").asInt()).isZero();
    }

    @Test
    void employeeCanBeFoundThroughSecondaryOrganizationMembership() throws Exception {
        long primary = organization("Primary");
        long secondary = organization("Secondary");
        postJson("/api/User/CreateEmployee", """
            {"firstName":"Multi","userNumber":"%s","organizationUnitId":%d,"organizationUnitListIds":[%d]}
            """.formatted(UUID.randomUUID(), primary, secondary));
        assertThat(list("/api/User/GetAllEmployees", secondary).path("totalCount").asInt()).isEqualTo(1);
    }

    @Test
    void nullCollectionEntriesReturnValidationErrorsInsteadOfServerErrors() throws Exception {
        String[][] cases = {
            {"/api/Department/GetList", "{\"filters\":[null]}"},
            {"/api/Department/GetList", "{\"multiSortMeta\":[null]}"},
            {"/api/Location/add", """
                {"name":"Invalid","organizationUnitId":1,
                 "translations":[{"languageId":1,"subLocations":[null]}]}
                """},
            {"/api/ObservationType/Create", """
                {"typeDescription":"Invalid","businessUnitId":1,"observationCategoryId":4,
                 "translations":[{"languageId":1,"observationSubTypes":[null]}]}
                """},
            {"/api/Role/CreateRole", """
                {"name":"Invalid","permissionLookupHierarchyDto":[{"id":1,"children":[null]}]}
                """}
        };
        for (String[] request : cases) {
            mvc.perform(post(request[0]).contentType("application/json").content(request[1]))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("must not be null")));
        }
    }

    @Test
    void childTextLimitsAreValidatedBeforeWritingToDatabase() throws Exception {
        String oversized = "X".repeat(256);
        for (String field : new String[]{"name", "description"}) {
            ObjectNode body = (ObjectNode) json.readTree("""
                {"name":"Invalid","organizationUnitId":1,"newSublocations":[{"name":"Child"}]}
                """);
            ((ObjectNode) body.path("newSublocations").get(0)).put(field, oversized);
            mvc.perform(post("/api/Location/add").contentType("application/json").content(body.toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400));
        }
        mvc.perform(post("/api/ObservationType/Create").contentType("application/json").content("""
            {"typeDescription":"Invalid","businessUnitId":1,"observationCategoryId":4,
             "observationSubTypes":[{"subTypeDescription":"%s"}]}
            """.formatted(oversized))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400));
        long unit = organization("Child text boundary");
        create("/api/Location/add", """
            {"name":"Boundary","newSublocations":[{"name":"%s","description":"%s"}]}
            """.formatted("N".repeat(255), "D".repeat(255)), "organizationUnitId", unit);
        assertThat(list("/api/Location/GetAllLocations", unit).at("/items/0/subLocations/0/name").asText()).hasSize(255);
    }

    private long organization(String name) throws Exception {
        String unique = UUID.randomUUID().toString();
        JsonNode response = postJson("/api/OrganizationUnit", "{\"name\":\"" + name + "\",\"description\":\"" + unique + "\",\"tenantId\":1,\"status\":1}");
        assertThat(response.asText()).isEqualTo("Organization Unit created successfully.");
        for (JsonNode n : getJson("/api/OrganizationUnit/GetAllOrganizations"))
            if (unique.equals(n.path("description").asText())) return n.path("id").asLong();
        throw new AssertionError("Created organization missing");
    }
    private JsonNode create(String path, String body, String unitField, long unit) throws Exception {
        ObjectNode node = (ObjectNode) json.readTree(body);
        node.put(unitField, unit);
        return postJson(path, node.toString());
    }
    private JsonNode list(String path, long unit) throws Exception {
        return postJson(path, "{\"businessUnitIds\":\"" + unit + "\",\"maxResultCount\":100}");
    }
    private JsonNode postJson(String path, String body) throws Exception {
        ObjectNode request=(ObjectNode)json.readTree(body);
        if(path.equals("/api/OrganizationUnit")) {
            var defaults=(ObjectNode)json.readTree("""
                {"line1":"Test address","country":101,"state":4026,"city":57933,"languageID":1,"timeZoneID":1,
                 "keyContactName":"Test Contact","phoneNumber":"9000000000","emailAddress":"test@example.com"}
                """);
            defaults.fields().forEachRemaining(e -> { if(!request.has(e.getKey())) request.set(e.getKey(),e.getValue()); });
        }
        if(path.equals("/api/Role/CreateRole")&&!request.has("landingPageId")) request.put("landingPageId",1);
        if(path.equals("/api/User/CreateEmployee")) {
            if(!request.has("lastName")) request.put("lastName","Tester");
            if(!request.has("gender")) request.put("gender",1);
            if(!request.has("status")) request.put("status",1);
            if(!request.has("languageID")) request.put("languageID",1);
            long unit=request.path("organizationUnitId").asLong();
            if(!request.has("department")) request.put("department",create("/api/Department/Create","{\"name\":\"Employee department\",\"status\":1}","businessUnitId",unit).path("id").asLong());
            if(!request.has("designation")) request.put("designation",create("/api/Designation/Create","{\"name\":\"Employee designation\",\"status\":1}","businessUnitId",unit).path("id").asLong());
        }
        body=request.toString();
        MvcResult result = mvc.perform(post(path).contentType("application/json").content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.message").value("Successful")).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).path("results");
    }
    private JsonNode getJson(String path) throws Exception {
        MvcResult result = mvc.perform(get(path)).andExpect(status().isOk()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).path("results");
    }
}
