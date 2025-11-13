package com.dansmultipro.ops.service.impl;

import com.dansmultipro.ops.config.RabbitConfig;
import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.dto.auth.RegisterRequestDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiResponseDto;
import com.dansmultipro.ops.dto.notification.EmailNotificationMessageDto;
import com.dansmultipro.ops.dto.user.ForgotPasswordRequestDto;
import com.dansmultipro.ops.dto.user.PasswordUpdateRequestDto;
import com.dansmultipro.ops.dto.user.UserResponseDto;
import com.dansmultipro.ops.exception.BusinessRuleException;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.User;
import com.dansmultipro.ops.model.master.Role;
import com.dansmultipro.ops.repository.RoleRepo;
import com.dansmultipro.ops.service.UserService;
import com.dansmultipro.ops.spec.UserSpecification;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl extends BaseService implements UserService {

    private static final String RESOURCE_NAME = "User";

    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    public UserServiceImpl(
            RoleRepo roleRepo,
            PasswordEncoder passwordEncoder,
            RabbitTemplate rabbitTemplate) {
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public ApiPostResponseDto register(RegisterRequestDto request) {
        validateEmailUniqueness(request.email());

        RoleTypeConstant targetRole = determineRegistrationRole();

        if (targetRole == RoleTypeConstant.GATEWAY && gatewayUserExists()) {
            throw new BusinessRuleException("Gateway user already exists.");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(fetchRole(targetRole.name()));

        boolean activeFlag = targetRole == RoleTypeConstant.GATEWAY;


        User saved = userRepo.save(prepareCreate(user, activeFlag));

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.SAVED.getValue());
        return new ApiPostResponseDto(saved.getId().toString(), message);
    }

    @Override
    @Transactional
    public ApiResponseDto updatePassword(PasswordUpdateRequestDto request) {
        UUID loginId = authUtil.getLoginId();

        User user = fetchUser(loginId);

        if (!user.getIsActive()) {
            throw new BusinessRuleException(messageBuilder(RESOURCE_NAME, ResponseConstant.ACCOUNT_INACTIVE));
        }

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessRuleException(messageBuilder("Password:", ResponseConstant.OLD_PASSWORD_INVALID));
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepo.save(prepareUpdate(user));

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.UPDATED.getValue());
        return new ApiResponseDto(message);
    }

    @Override
    @Transactional
    public ApiResponseDto forgotPassword(ForgotPasswordRequestDto request) {
        User user = userRepo.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageBuilder("Email", ResponseConstant.NOT_FOUND)));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BusinessRuleException(messageBuilder(RESOURCE_NAME, ResponseConstant.ACCOUNT_INACTIVE));
        }

        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));

        userRepo.save(prepareUpdate(user));

        notifyForgotPassword(user, temporaryPassword);

        String message = messageBuilder("User password", ResponseConstant.UPDATED.getValue());
        return new ApiResponseDto(message);
    }

    @Override
    @Transactional
    public ApiResponseDto approveCustomer(List<String> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            throw new BusinessRuleException("customerIds must not be empty.");
        }

        List<UUID> userIds = customerIds.stream().map(this::getUUID).toList();
        List<User> users = userRepo.findAllById(userIds);
        if (users.isEmpty()) {
            throw new ResourceNotFoundException(
                    messageBuilder(RESOURCE_NAME, ResponseConstant.NOT_FOUND));
        }

        users.forEach(user -> {
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                user.setIsActive(true);
                prepareUpdate(user);
            }
        });

        userRepo.saveAllAndFlush(users);

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.UPDATED.getValue());
        return new ApiResponseDto(message);
    }

    @Override
    public List<UserResponseDto> getAll(Boolean isActive, String roleCode) {
        Specification<User> spec = Specification.allOf(
                UserSpecification.hasActiveStatus(isActive),
                UserSpecification.hasRole(roleCode))
                .and(
                        Specification.not(UserSpecification.hasRole(RoleTypeConstant.SA.name())
                                .or(UserSpecification.hasRole(RoleTypeConstant.SYSTEM.name()))));

        List<User> users = userRepo.findAll(spec);

        return users
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public UserResponseDto getById(String id) {
        User user = fetchUser(getUUID(id));
        return toDto(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageBuilder(RESOURCE_NAME, ResponseConstant.NOT_FOUND)));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getCode());
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getIsActive()),
                true,
                true,
                true,
                List.of(authority));
    }

    private void ensureSuperAdminRole() {
        if (!authUtil.hasRole(RoleTypeConstant.SA)) {
            throw new BusinessRuleException(messageBuilder("Access", ResponseConstant.SUPER_ADMIN_REQUIRED));
        }
    }

    private RoleTypeConstant determineRegistrationRole() {
        if (!authUtil.isAuthenticated()) {
            return RoleTypeConstant.CUSTOMER;
        }
        ensureSuperAdminRole();
        return RoleTypeConstant.GATEWAY;
    }

    private void validateEmailUniqueness(String email) {
        boolean exists = userRepo.existsByEmailIgnoreCase(email);
        if (exists) {
            throw new BusinessRuleException(messageBuilder("Email", ResponseConstant.ALREADY_EXISTS));
        }
    }

    private boolean gatewayUserExists() {
        return userRepo.findFirstByRoleCode(RoleTypeConstant.GATEWAY.name()).isPresent();
    }

    private User fetchUser(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageBuilder(RESOURCE_NAME, ResponseConstant.NOT_FOUND)));
    }

    private Role fetchRole(String code) {
        return roleRepo.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageBuilder("Role", ResponseConstant.NOT_FOUND)));
    }

    private UserResponseDto toDto(User entity) {
        return new UserResponseDto(
                entity.getId().toString(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getRole().getName(),
                entity.getIsActive(),
                entity.getOptLock());
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        int tempPasswordLength = 12;
        SecureRandom secureRandom = new SecureRandom();

        StringBuilder builder = new StringBuilder(tempPasswordLength);
        for (int i = 0; i < tempPasswordLength; i++) {
            int index = secureRandom.nextInt(chars.length());
            builder.append(chars.charAt(index));
        }

        return builder.toString();
    }

    private void notifyForgotPassword(User user, String temporaryPassword) {
        EmailNotificationMessageDto message = new EmailNotificationMessageDto(
                user.getEmail(),
                null,
                temporaryPassword);

        rabbitTemplate.convertAndSend(
                RabbitConfig.NOTIFICATION_EXCHANGE,
                RabbitConfig.FORGOT_PASSWORD_NOTIFICATION_ROUTING_KEY,
                message);
    }
}
