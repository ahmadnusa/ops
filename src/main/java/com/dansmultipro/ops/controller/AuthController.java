package com.dansmultipro.ops.controller;

import com.dansmultipro.ops.dto.auth.LoginRequestDto;
import com.dansmultipro.ops.dto.auth.LoginResponseDto;
import com.dansmultipro.ops.dto.common.ApiResponseDto;
import com.dansmultipro.ops.service.AuthService;
import com.dansmultipro.ops.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String auth = request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);
        String token = auth.substring(7);

        jwtUtil.blacklist(token);

        ApiResponseDto res = new ApiResponseDto("Logged out successfully");
        return ResponseEntity.ok(res);
    }
}
