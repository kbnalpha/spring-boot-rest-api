package com.ehspro.config;

import com.ehspro.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean SecurityFilterChain secured(HttpSecurity http, ObjectMapper mapper) throws Exception {
        org.springframework.security.web.AuthenticationEntryPoint unauthorized = (request,response,exception) -> {
            response.setStatus(401);
            response.setHeader("WWW-Authenticate", "Basic realm=\"EHS Pro API\"");
            response.setContentType("application/json");
            mapper.writeValue(response.getOutputStream(),new ApiResponse<>(401,"Authentication required",null));
        };
        return http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.anyRequest().authenticated())
            .httpBasic(b -> b.authenticationEntryPoint(unauthorized))
            .exceptionHandling(e -> e.authenticationEntryPoint(unauthorized).accessDeniedHandler((request,response,exception) -> {
                response.setStatus(403); response.setContentType("application/json");
                mapper.writeValue(response.getOutputStream(),new ApiResponse<>(403,"Access denied",null));
            })).build();
    }
}