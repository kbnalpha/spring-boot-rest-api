package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.security.EhsPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/Auth")
public class LoginController {
    private final AuthenticationManager authentication;
    private final com.ehspro.service.AuthenticationResponseService responses;
    public LoginController(AuthenticationManager authentication,com.ehspro.service.AuthenticationResponseService responses) {this.authentication=authentication;this.responses=responses;}
    @PostMapping("/authenticate")
    public ApiResponse<?> login(@Valid @RequestBody AccountRequests.Login dto) {
        var result=authentication.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(dto.email(),dto.password()));
        var p=(EhsPrincipal)result.getPrincipal();
        return ApiResponse.success(responses.response(p));
    }
}
