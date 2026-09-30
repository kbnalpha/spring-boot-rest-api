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
    public LoginController(AuthenticationManager authentication) {this.authentication=authentication;}
    @PostMapping("/Login")
    public ApiResponse<?> login(@Valid @RequestBody AccountRequests.Login dto) {
        var result=authentication.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(dto.username(),dto.password()));
        var p=(EhsPrincipal)result.getPrincipal();
        Map<String,Object> response=new LinkedHashMap<>();response.put("username",p.getUsername());response.put("accountType",p.accountType);
        response.put("mustChangePassword",p.mustChangePassword);response.put("nextAction",p.mustChangePassword?"RESET_PASSWORD":"LOGIN_SUCCESS");
        response.put("authenticationType","HTTP_BASIC");
        if(p.mustChangePassword) response.put("resetPasswordEndpoint","/api/Auth/FirstLoginPasswordReset");
        else response.put("permissions",p.getAuthorities().stream().map(a -> a.getAuthority()).toList());
        return ApiResponse.success(response);
    }
}
