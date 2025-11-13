package com.dansmultipro.ops.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dansmultipro.ops.config.RabbitConfig;
import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.dto.auth.RegisterRequestDto;
import com.dansmultipro.ops.dto.common.ApiResponseDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.notification.EmailNotificationMessageDto;
import com.dansmultipro.ops.dto.user.ForgotPasswordRequestDto;
import com.dansmultipro.ops.dto.user.PasswordUpdateRequestDto;
import com.dansmultipro.ops.dto.user.UserResponseDto;
import com.dansmultipro.ops.exception.BusinessRuleException;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.User;
import com.dansmultipro.ops.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerShouldCreateCustomerWhenUnauthenticated() {
        RegisterRequestDto request = new RegisterRequestDto("New User", "new.user@ops.local", "Secret123!");

        ApiPostResponseDto response = userService.register(request);

        assertThat(response.id()).isNotBlank();
        User saved = userRepo.findById(UUID.fromString(response.id())).orElseThrow();
        assertThat(saved.getRole().getCode()).isEqualTo(RoleTypeConstant.CUSTOMER.name());
        assertThat(saved.getIsActive()).isFalse();
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequestDto request = new RegisterRequestDto("Duplicate", customerUser.getEmail(), "Password123!");

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void registerShouldThrowWhenAuthenticatedUserNotSuperAdmin() {
        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.hasRole(RoleTypeConstant.SA)).thenReturn(false);

        RegisterRequestDto request = new RegisterRequestDto("Gateway User", "gateway.new@ops.local", "Gateway123!");

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Super Admin");
    }

    @Test
    void updatePasswordShouldUpdateWhenOldPasswordMatches() {
        String oldPassword = "OldPass123!";
        String newPassword = "NewPass456!";
        customerUser.setPassword(passwordEncoder.encode(oldPassword));
        userRepo.saveAndFlush(customerUser);

        when(authUtil.getLoginId()).thenReturn(customerUser.getId());

        PasswordUpdateRequestDto request = new PasswordUpdateRequestDto(oldPassword, newPassword);

        ApiResponseDto response = userService.updatePassword(request);

        assertThat(response.message()).isEqualTo("User has been Updated successfully.");

        User updated = userRepo.findById(customerUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, updated.getPassword())).isTrue();
    }

    @Test
    void updatePasswordShouldThrowWhenOldPasswordInvalid() {
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());

        PasswordUpdateRequestDto request = new PasswordUpdateRequestDto("Wrong123!", "WillNotUse!");

        assertThatThrownBy(() -> userService.updatePassword(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Old password");
    }

    @Test
    void forgotPasswordShouldResetPasswordAndPublishNotification() {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto(customerUser.getEmail());

        ApiResponseDto response = userService.forgotPassword(request);

        assertThat(response.message()).isEqualTo("User password has been Updated successfully.");

        ArgumentCaptor<EmailNotificationMessageDto> messageCaptor = ArgumentCaptor.forClass(
                EmailNotificationMessageDto.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitConfig.FORGOT_PASSWORD_NOTIFICATION_ROUTING_KEY),
                messageCaptor.capture());

        EmailNotificationMessageDto message = messageCaptor.getValue();
        assertThat(message.email()).isEqualTo(customerUser.getEmail());
        assertThat(message.temporaryPassword()).isNotBlank();

        User updated = userRepo.findById(customerUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(message.temporaryPassword(), updated.getPassword())).isTrue();
    }

    @Test
    void forgotPasswordShouldFailWhenEmailUnknown() {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("unknown@ops.local");

        assertThatThrownBy(() -> userService.forgotPassword(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void forgotPasswordShouldFailWhenUserInactive() {
        User inactiveUser = createUser("Inactive", "inactive@ops.local", customerRole, false, "password");
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto(inactiveUser.getEmail());

        assertThatThrownBy(() -> userService.forgotPassword(request))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void approveCustomerShouldActivateInactiveUsers() {
        User pendingUser = createUser("Pending User", "pending@ops.local", customerRole, false, "password");

        ApiResponseDto response = userService.approveCustomer(List.of(pendingUser.getId().toString()));

        assertThat(response.message()).isEqualTo("User has been Updated successfully.");

        User updated = userRepo.findById(pendingUser.getId()).orElseThrow();
        assertThat(updated.getIsActive()).isTrue();
    }

    @Test
    void approveCustomerShouldThrowWhenUsersNotFound() {
        assertThatThrownBy(() -> userService.approveCustomer(List.of(UUID.randomUUID().toString())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllShouldReturnActiveCustomersOnly() {
        createUser("Inactive Customer", "inactive.customer@ops.local", customerRole, false, "password");

        List<UserResponseDto> responses = userService.getAll(true, RoleTypeConstant.CUSTOMER.name());

        assertThat(responses).isNotEmpty();
        assertThat(responses).allMatch(user -> Boolean.TRUE.equals(user.isActive()));
        assertThat(responses).allMatch(user -> RoleTypeConstant.CUSTOMER.getName().equals(user.role()));
    }

    @Test
    void getAllShouldReturnEmptyWhenNoUsersMatchCriteria() {
        List<UserResponseDto> responses = userService.getAll(true, "UNKNOWN");

        assertThat(responses).isEmpty();
    }

    @Test
    void getByIdShouldReturnUser() {
        UserResponseDto response = userService.getById(customerUser.getId().toString());

        assertThat(response.id()).isEqualTo(customerUser.getId().toString());
        assertThat(response.email()).isEqualTo(customerUser.getEmail());
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        assertThatThrownBy(() -> userService.getById(UUID.randomUUID().toString()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void loadUserByUsernameShouldReturnDetails() {
        UserDetails details = userService.loadUserByUsername(customerUser.getEmail());

        assertThat(details.getUsername()).isEqualTo(customerUser.getEmail());
        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_" + customerUser.getRole().getCode());
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUnknown() {
        assertThatThrownBy(() -> userService.loadUserByUsername("missing@ops.local"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

}
