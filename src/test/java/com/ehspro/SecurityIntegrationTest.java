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
    @Test void corsCoversRoutesOutsideApiWithoutMakingThemPublic() throws Exception {
        for(String path:java.util.List.of("/actuator/health","/v3/api-docs","/swagger-ui/index.html")) {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options(path)
                .header("Origin","http://localhost:3000")
                .header("Access-Control-Request-Method","GET")
                .header("Access-Control-Request-Headers","authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin","http://localhost:3000"));
        }
        mvc.perform(get("/actuator/health").header("Origin","http://localhost:3000"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:3000"));
        mvc.perform(get("/v3/api-docs").header("Origin","http://localhost:3000"))
            .andExpect(status().isUnauthorized()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:3000"));
    }
    @Test void loginPreflightAllowsFrontendLanguageHeader() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/Auth/authenticate")
            .header("Origin","http://localhost:3000")
            .header("Access-Control-Request-Method","POST")
            .header("Access-Control-Request-Headers","content-type,accept-language,accept-org-language,accept-nav-language"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin","http://localhost:3000"))
            .andExpect(header().string("Access-Control-Allow-Headers",org.hamcrest.Matchers.containsString("accept-nav-language")))
            .andExpect(header().string("Access-Control-Allow-Headers",org.hamcrest.Matchers.containsString("accept-org-language")))
            .andExpect(header().string("Access-Control-Allow-Headers",org.hamcrest.Matchers.containsString("accept-language")));
    }
    @Test void corsPreflightWorksWithoutLoginForAllOrigins() throws Exception {
        for(String path:java.util.List.of("/api/User/1","/api/Auth/authenticate","/api/OrganizationUnit/GetAllOrganizations","/actuator/health","/v3/api-docs")) {
            for(String method:java.util.List.of("GET","POST","PUT","PATCH","DELETE")) {
                mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options(path)
                    .header("Origin","https://frontend.example")
                    .header("Access-Control-Request-Method",method)
                    .header("Access-Control-Request-Headers","authorization,content-type,accept,accept-language,accept-org-language,accept-nav-language,x-custom-client-header"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin","https://frontend.example"))
                    .andExpect(header().string("Access-Control-Allow-Credentials","true"))
                    .andExpect(header().string("Access-Control-Allow-Headers",org.hamcrest.Matchers.containsString("x-custom-client-header")));
            }
        }
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/User/1")
            .header("Origin","https://untrusted.example").header("Access-Control-Request-Method","DELETE"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin","https://untrusted.example"))
            .andExpect(header().string("Access-Control-Allow-Credentials","true"));
    }
    @Test void allOriginsCoverSuccessAndErrorResponsesAcrossRoutes() throws Exception {
        for(String origin:java.util.List.of("https://frontend.example","https://admin.example","https://untrusted.example")) {
            mvc.perform(get("/api/Role/GetAllRoles").header("Origin",origin))
                .andExpect(status().isUnauthorized()).andExpect(header().string("Access-Control-Allow-Origin",origin));
            mvc.perform(get("/api/Role/GetAllRoles").header("Origin",origin).with(httpBasic("test-api","test-password")))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin",origin));
            mvc.perform(get("/api/User/9223372036854775807").header("Origin",origin).with(httpBasic("test-api","test-password")))
                .andExpect(status().isNotFound()).andExpect(header().string("Access-Control-Allow-Origin",origin));
        }
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
