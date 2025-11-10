package com.dansmultipro.ops.service;

import com.dansmultipro.ops.dto.auth.LoginRequestDto;
import com.dansmultipro.ops.dto.auth.LoginResponseDto;
import com.dansmultipro.ops.dto.auth.RegisterRequestDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiPutResponseDto;
import com.dansmultipro.ops.dto.user.PasswordUpdateRequestDto;
import com.dansmultipro.ops.dto.user.UserApproveRequestDto;
import com.dansmultipro.ops.dto.user.UserResponseDto;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    ApiPostResponseDto register(RegisterRequestDto request);

    LoginResponseDto login(LoginRequestDto request);

    ApiPutResponseDto updatePassword(PasswordUpdateRequestDto request);

    ApiPutResponseDto approveCustomer(UserApproveRequestDto request);

    List<UserResponseDto> getAll(Boolean isActive, String roleCode);

    UserResponseDto getById(String id);
}
