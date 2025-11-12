package com.dansmultipro.ops.controller;

import java.util.List;

import com.dansmultipro.ops.dto.common.ApiDeleteResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dansmultipro.ops.dto.auth.RegisterRequestDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiPutResponseDto;
import com.dansmultipro.ops.dto.user.PasswordUpdateRequestDto;
import com.dansmultipro.ops.dto.user.UserResponseDto;
import com.dansmultipro.ops.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiPostResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        ApiPostResponseDto response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('SA')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto> >getAll(@RequestParam(required = false) Boolean isActive,
                                        @RequestParam(required = false) String roleCode) {
        List<UserResponseDto> res = userService.getAll(isActive, roleCode);
        return ResponseEntity.ok(res);
    }

    @PreAuthorize("hasRole('SA')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable String id) {
        UserResponseDto response = userService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SA')")
    @PutMapping("/approve")
    public ResponseEntity<ApiDeleteResponseDto> approve(
            @RequestBody List<String> customerIds
    ) {
        ApiDeleteResponseDto response = userService.approveCustomer(customerIds);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/password")
    public ResponseEntity<ApiDeleteResponseDto> updatePassword(
            @Valid @RequestBody PasswordUpdateRequestDto request) {
        ApiDeleteResponseDto response = userService.updatePassword(request);
        return ResponseEntity.ok(response);
    }
}
