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
    @Bean org.springframework.web.cors.UrlBasedCorsConfigurationSource corsConfigurationSource() {
        var config=new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOriginPatterns(java.util.List.of("*"));
        config.setAllowedMethods(java.util.List.of("GET","HEAD","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setExposedHeaders(java.util.List.of("WWW-Authenticate"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        var source=new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);
        return source;
    }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean org.springframework.security.authentication.AuthenticationManager authenticationManager(com.ehspro.security.AccountDetailsService identities,PasswordEncoder encoder) {
        var provider=new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        provider.setUserDetailsService(identities);provider.setPasswordEncoder(encoder);
        return new org.springframework.security.authentication.ProviderManager(provider);
    }
    @Bean SecurityFilterChain secured(HttpSecurity http, ObjectMapper mapper,com.ehspro.security.JwtService jwt) throws Exception {
        org.springframework.security.web.AuthenticationEntryPoint unauthorized = (request,response,exception) -> {
            response.setStatus(401);
            response.setHeader("WWW-Authenticate", "Basic realm=\"EHS Pro API\"");
            response.setContentType("application/json");
            mapper.writeValue(response.getOutputStream(),new ApiResponse<>(401,"Authentication required",null));
        };
        return http.csrf(csrf -> csrf.disable())
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(org.springframework.http.HttpMethod.GET,"/actuator/health").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/Common/getLanguages").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST,"/api/Auth/authenticate").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new com.ehspro.security.FirstLoginFilter(mapper),org.springframework.security.web.access.intercept.AuthorizationFilter.class)
            .addFilterBefore(new com.ehspro.security.JwtAuthenticationFilter(jwt,mapper),org.springframework.security.web.authentication.www.BasicAuthenticationFilter.class)
            .httpBasic(b -> b.authenticationEntryPoint(unauthorized))
            .exceptionHandling(e -> e.authenticationEntryPoint(unauthorized).accessDeniedHandler((request,response,exception) -> {
                response.setStatus(403); response.setContentType("application/json");
                mapper.writeValue(response.getOutputStream(),new ApiResponse<>(403,"Access denied",null));
            })).build();
    }
}
