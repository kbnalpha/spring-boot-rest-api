package com.ehspro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:security-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.security.user.name=test-api", "spring.security.user.password=test-password"
})
@AutoConfigureMockMvc
@ActiveProfiles("security-test")
class SecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Test void corsPreflightWorksWithoutLoginAndRejectsUnknownOrigins() throws Exception {
        for(String method:java.util.List.of("GET","POST","PUT","PATCH","DELETE")) {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/User/1")
                .header("Origin","http://localhost:3000")
                .header("Access-Control-Request-Method",method)
                .header("Access-Control-Request-Headers","authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin","http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials","true"));
        }
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/User/1")
            .header("Origin","https://untrusted.example").header("Access-Control-Request-Method","DELETE"))
            .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
    @Test void corsDoesNotBypassAuthenticationAndErrorsHaveCorsHeaders() throws Exception {
        mvc.perform(get("/api/Role/GetAllRoles").header("Origin","http://localhost:3000"))
            .andExpect(status().isUnauthorized()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:3000"));
        mvc.perform(get("/api/Role/GetAllRoles").header("Origin","http://localhost:3000").with(httpBasic("test-api","test-password")))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:3000"));
    }
    @Test
    void healthIsPublicWithoutExposingDetailsOrOtherManagementEndpoints() throws Exception {
        mvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.components").doesNotExist())
            .andExpect(jsonPath("$.details").doesNotExist());
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/env").with(httpBasic("test-api", "test-password")))
            .andExpect(status().isNotFound());
    }
    @Test
    void nonLocalProfileRequiresCredentials() throws Exception {
        mvc.perform(get("/api/Role/GetAllRoles"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.statusCode").value(401));
        mvc.perform(get("/api/Role/GetAllRoles").with(httpBasic("test-api", "test-password")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.statusCode").value(200));
        mvc.perform(get("/api/Role/GetAllRoles").with(httpBasic("test-api", "incorrect")))
            .andExpect(status().isUnauthorized());
    }
}
