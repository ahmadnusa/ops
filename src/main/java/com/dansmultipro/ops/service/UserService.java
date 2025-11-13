package com.dansmultipro.ops.service;

import com.dansmultipro.ops.dto.auth.RegisterRequestDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiResponseDto;
import com.dansmultipro.ops.dto.user.ForgotPasswordRequestDto;
import com.dansmultipro.ops.dto.user.PasswordUpdateRequestDto;
import com.dansmultipro.ops.dto.user.UserResponseDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    ApiPostResponseDto register(RegisterRequestDto request);

    ApiResponseDto updatePassword(PasswordUpdateRequestDto request);

    ApiResponseDto forgotPassword(ForgotPasswordRequestDto request);

    ApiResponseDto approveCustomer(List<String> customerIds);

    List<UserResponseDto> getAll(Boolean isActive, String roleCode);

    UserResponseDto getById(String id);
}
