package com.dansmultipro.ops.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.dto.auth.LoginRequestDto;
import com.dansmultipro.ops.dto.auth.LoginResponseDto;
import com.dansmultipro.ops.exception.BusinessRuleException;
import com.dansmultipro.ops.model.User;
import com.dansmultipro.ops.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void loginShouldReturnTokenWhenCredentialsValid() {
        LoginRequestDto request = new LoginRequestDto(customerUser.getEmail(), "password");

        LoginResponseDto response = authService.login(request);

        assertThat(response.fullName()).isEqualTo(customerUser.getFullName());
        assertThat(response.role()).isEqualTo(customerUser.getRole().getCode());
        assertThat(response.accessToken()).isNotBlank();

    }

    @Test
    void loginShouldFailWhenUserInactive() {
        User inactiveUser = createUser("Inactive", "inactive@ops.local", customerRole, false,
                passwordEncoder.encode("password"));
        LoginRequestDto request = new LoginRequestDto(inactiveUser.getEmail(), "password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(ResponseConstant.ACCOUNT_INACTIVE.getValue());
    }

    @Test
    void loginShouldFailWhenCredentialsInvalid() {
        LoginRequestDto request = new LoginRequestDto(customerUser.getEmail(), "wrong");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(ResponseConstant.INVALID_CREDENTIAL.getValue());
    }
}
