package com.ehspro.security;
import com.ehspro.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;

public class FirstLoginFilter extends OncePerRequestFilter {
    private final ObjectMapper mapper;
    public FirstLoginFilter(ObjectMapper mapper) {this.mapper=mapper;}
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        var authentication=SecurityContextHolder.getContext().getAuthentication();
        String path=request.getRequestURI().substring(request.getContextPath().length());
        boolean onboarding="POST".equals(request.getMethod())&&(path.equals("/api/Auth/Login")||path.equals("/api/Auth/FirstLoginPasswordReset"));
        if(authentication!=null&&authentication.getPrincipal() instanceof EhsPrincipal p&&p.mustChangePassword&&!onboarding) {
            response.setStatus(403);response.setContentType("application/json");
            mapper.writeValue(response.getOutputStream(),new ApiResponse<>(403,"Password change required",Map.of("mustChangePassword",true,"nextAction","RESET_PASSWORD","resetPasswordEndpoint","/api/Auth/FirstLoginPasswordReset")));
            return;
        }
        chain.doFilter(request,response);
    }
}
