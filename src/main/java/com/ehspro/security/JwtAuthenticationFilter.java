package com.ehspro.security;
import com.ehspro.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;private final ObjectMapper mapper;
    public JwtAuthenticationFilter(JwtService jwt,ObjectMapper mapper) {this.jwt=jwt;this.mapper=mapper;}
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws IOException,ServletException {
        String header=request.getHeader("Authorization");
        if(header!=null&&header.regionMatches(true,0,"Bearer ",0,7)) {
            try {
                var p=jwt.verify(header.substring(7).trim());
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(p,null,p.getAuthorities()));
            } catch(io.jsonwebtoken.JwtException|org.springframework.security.core.AuthenticationException|IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();response.setStatus(401);response.setContentType("application/json");
                response.setHeader("WWW-Authenticate","Bearer error=\"invalid_token\"");
                mapper.writeValue(response.getOutputStream(),new ApiResponse<>(401,"Invalid or expired token",null));return;
            }
        }
        chain.doFilter(request,response);
    }
}
