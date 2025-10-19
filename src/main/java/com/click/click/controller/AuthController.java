package com.click.click.controller;


import com.click.click.dto.LoginDTO;
import com.click.click.dto.RegisterDTO;
import com.click.click.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/click")
@RequiredArgsConstructor
public class AuthController {


    private final com.click.click.service.AuthService authService;


    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterDTO request) {
        authService.register(request);
        return ApiResponse.ok("registered");
    }


    @PostMapping("/login")
    public ApiResponse<LoginDTO.LoginResponse> login(@Valid @RequestBody LoginDTO request) {
        return ApiResponse.ok(authService.login(request));
    }


    @GetMapping("/me")
    public ApiResponse<String> me(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(user.getUsername());
    }
}