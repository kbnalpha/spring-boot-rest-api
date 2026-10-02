package com.ehspro.config;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/** Runs before database connections and bean creation, without logging configuration values. */
public class DeploymentDiagnostics implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger LOG=LoggerFactory.getLogger(DeploymentDiagnostics.class);
    @Override public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment env=context.getEnvironment();
        if(!Arrays.asList(env.getActiveProfiles()).contains("render")) return;
        LOG.info("Render deployment configuration preflight (secret values are never logged)");
        Map<String,String> checks=new LinkedHashMap<>();
        checks.put("DB_URL / DB_HOST / DB_PORT / DB_NAME","spring.datasource.url");
        checks.put("DB_USERNAME","spring.datasource.username");
        checks.put("DB_PASSWORD","spring.datasource.password");
        checks.put("LIQUIBASE_DB_URL","spring.liquibase.url");
        checks.put("API_USERNAME","spring.security.user.name");
        checks.put("JWT_SECRET","ehs.jwt.secret");
        checks.put("API_PASSWORD","spring.security.user.password");
        checks.put("SMTP_HOST","spring.mail.host");
        checks.put("SMTP_PORT","spring.mail.port");
        checks.put("SMTP_USERNAME","spring.mail.username");
        checks.put("SMTP_PASSWORD","spring.mail.password");
        checks.put("SMTP_FROM","ehs.mail.from");
        checks.put("SMTP_AUTH","spring.mail.properties.mail.smtp.auth");
        checks.put("SMTP_STARTTLS","spring.mail.properties.mail.smtp.starttls.required");
        checks.put("PORT","server.port");
        List<String> missing=new ArrayList<>();
        checks.forEach((name,property) -> {
            boolean present;
            try {String value=env.getProperty(property);present=value!=null&&!value.isBlank()&&!value.contains("${");}
            catch(IllegalArgumentException ex) {present=false;}
            LOG.info("Deployment setting {}: {}",name,present?"SET (configured or default)":"MISSING / UNRESOLVED");
            if(!present) missing.add(name);
        });
        for(String name:List.of("DB_HOST","DB_PORT","DB_NAME","DB_URL","DB_USERNAME","DB_PASSWORD","LIQUIBASE_DB_URL","API_USERNAME","API_PASSWORD","SMTP_HOST","SMTP_PORT","SMTP_USERNAME","SMTP_PASSWORD","SMTP_FROM","SMTP_AUTH","SMTP_STARTTLS","PORT")) {
            String state;
            try {String value=env.getProperty(name);state=value==null?"not supplied; profile default if available":value.isBlank()?"blank":"supplied";}
            catch(IllegalArgumentException ex) {state="unresolved";}
            LOG.debug("Deployment variable {}: {}",name,state);
        }
        if(!missing.isEmpty()) throw new IllegalStateException("Render configuration incomplete: "+String.join(", ",missing)+". Set these variables in Render Environment and redeploy. A Docker deploy alone does not sync render.yaml.");
        LOG.info("Render configuration preflight passed; required settings resolve. Database and SMTP connectivity are checked separately.");
    }
}
