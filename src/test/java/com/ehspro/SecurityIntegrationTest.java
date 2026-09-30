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
