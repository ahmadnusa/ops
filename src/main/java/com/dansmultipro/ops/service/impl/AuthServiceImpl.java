package com.dansmultipro.ops.service.impl;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.dto.auth.LoginRequestDto;
import com.dansmultipro.ops.dto.auth.LoginResponseDto;
import com.dansmultipro.ops.exception.BusinessRuleException;
import com.dansmultipro.ops.model.User;
import com.dansmultipro.ops.service.AuthService;
import com.dansmultipro.ops.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl extends BaseService implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                request.email(), request.password());

        try {
            authenticationManager.authenticate(auth);
        } catch (DisabledException ex) {
            throw new BusinessRuleException(messageBuilder("User",
                    ResponseConstant.ACCOUNT_INACTIVE));
        } catch (BadCredentialsException ex) {
            throw new BusinessRuleException(messageBuilder("Credential:", ResponseConstant.INVALID_CREDENTIAL));
        }

        User user = userRepo.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessRuleException(
                        messageBuilder("User", ResponseConstant.NOT_FOUND)));

        String token = jwtUtil.generateToken(user);

        return new LoginResponseDto(
                user.getFullName(),
                user.getRole().getCode(),
                token);
    }
}
