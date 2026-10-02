package com.ehspro.controller;
import com.ehspro.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/Common")
public class CommonController {
    @GetMapping("/getLanguages")
    public ApiResponse<?> languages() {
        return ApiResponse.success(List.of(Map.of("id",1,"name","English (US)","code","en-US")));
    }
}
