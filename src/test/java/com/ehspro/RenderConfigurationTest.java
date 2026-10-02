package com.ehspro;

import com.ehspro.config.DeploymentDiagnostics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class RenderConfigurationTest {
    private GenericApplicationContext context(Map<String,Object> variables) {
        var context=new GenericApplicationContext();
        // Isolate tests from workstation credentials.
        var sources=context.getEnvironment().getPropertySources();
        sources.remove("systemEnvironment");sources.remove("systemProperties");
        sources.addFirst(new MapPropertySource("test",variables));
        context.getEnvironment().setActiveProfiles("render");
        new ConfigDataApplicationContextInitializer().initialize(context);
        return context;
    }
    @Test void standaloneRenderResolvesDatabaseAndMailWithoutPrintingSecrets(CapturedOutput output) {
        try(var context=context(Map.of("DB_PASSWORD","db-secret-test","API_PASSWORD","api-secret-test","SMTP_PASSWORD","smtp-secret-test","JWT_SECRET","abcdefghijklmnopqrstuvwxyz0123456789abcdefgh"))) {
            new DeploymentDiagnostics().initialize(context);
            var env=context.getEnvironment();
            assertThat(env.getActiveProfiles()).containsExactly("render");
            assertThat(env.getProperty("spring.datasource.driver-class-name")).isEqualTo("com.mysql.cj.jdbc.Driver");
            assertThat(env.getProperty("spring.liquibase.url")).contains("sessionVariables=sql_require_primary_key=0");
            assertThat(env.getProperty("spring.security.user.name")).isEqualTo("ehs-api");
            assertThat(env.getProperty("spring.mail.host")).isEqualTo("smtp-relay.brevo.com");
            assertThat(env.getProperty("spring.security.user.password")).isEqualTo("api-secret-test");
            assertThat(env.getProperty("spring.datasource.password")).isEqualTo("db-secret-test");
            assertThat(env.getProperty("spring.liquibase.password")).isEqualTo("db-secret-test");
            assertThat(env.getProperty("spring.mail.password")).isEqualTo("smtp-secret-test");
            assertThat(output.getAll()).contains("preflight passed").doesNotContain("db-secret-test","api-secret-test","smtp-secret-test");
        }
    }
    @Test void missingSecretsAreReportedTogetherBeforeBeanCreation(CapturedOutput output) {
        try(var context=context(Map.of())) {
            assertThatThrownBy(() -> new DeploymentDiagnostics().initialize(context))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("DB_PASSWORD").hasMessageContaining("API_PASSWORD").hasMessageContaining("SMTP_PASSWORD");
            assertThat(output.getAll()).contains("MISSING / UNRESOLVED");
        }
    }
}
