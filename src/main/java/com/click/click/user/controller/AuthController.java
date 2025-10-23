package com.click.click.user.controller;


import com.click.click.user.dto.LoginDTO;
import com.click.click.user.dto.RegisterDTO;
import com.click.click.user.service.AuthService;
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


    private final AuthService authService;


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