package com.dansmultipro.ops.service.impl;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.dto.auth.LoginRequestDto;
import com.dansmultipro.ops.dto.auth.LoginResponseDto;
import com.dansmultipro.ops.dto.auth.RegisterRequestDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiPutResponseDto;
import com.dansmultipro.ops.dto.user.PasswordUpdateRequestDto;
import com.dansmultipro.ops.dto.user.UserResponseDto;
import com.dansmultipro.ops.exception.BusinessRuleException;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.master.Role;
import com.dansmultipro.ops.model.user.User;
import com.dansmultipro.ops.repository.RoleRepo;
import com.dansmultipro.ops.repository.UserRepo;
import com.dansmultipro.ops.spec.UserSpecification;
import com.dansmultipro.ops.service.UserService;
import com.dansmultipro.ops.util.JwtUtil;
import com.dansmultipro.ops.dto.auth.TokenPair;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends BaseService implements UserService {

    private static final String RESOURCE_NAME = "User";

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserServiceImpl(
            UserRepo userRepo,
            RoleRepo roleRepo,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            @Lazy AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public ApiPostResponseDto register(RegisterRequestDto request) {
        validateEmailUniqueness(request.email(), null);

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        boolean authenticated = authUtil.isAuthenticated();
        RoleTypeConstant targetRole = authenticated ? RoleTypeConstant.GATEWAY : RoleTypeConstant.CUSTOMER;
        if (authenticated) {
            ensureSuperAdmin();
        }
        user.setRole(fetchRoleByCode(targetRole.name()));
        boolean activeFlag = targetRole == RoleTypeConstant.GATEWAY;
        prepareInsert(user, activeFlag);

        User saved = userRepo.save(user);
        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.SAVED.getValue());
        return new ApiPostResponseDto(saved.getId().toString(), message);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                request.email(), request.password());

        try {
            authenticationManager.authenticate(auth);
        } catch (DisabledException ex) {
            throw new BusinessRuleException(messageBuilder(RESOURCE_NAME, ResponseConstant.ACCOUNT_INACTIVE));
        } catch (BadCredentialsException ex) {
            throw new BusinessRuleException(messageBuilder("Credential:", ResponseConstant.INVALID_CREDENTIAL));
        }

        User user = userRepo.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessRuleException(
                        messageBuilder(RESOURCE_NAME, ResponseConstant.NOT_FOUND)));

        TokenPair tokenPair = jwtUtil.generateToken(user);

        return new LoginResponseDto(user.getFullName(), user.getRole().getCode(), tokenPair.token(),
                tokenPair.expiresAt().toString());
    }

    @Override
    public ApiPutResponseDto updatePassword(PasswordUpdateRequestDto request) {
        UUID loginId;
        try {
            loginId = authUtil.getLoginId();
        } catch (IllegalStateException ex) {
            throw new BusinessRuleException(messageBuilder("Authentication", ResponseConstant.AUTH_REQUIRED));
        }

        User user = fetchUser(loginId);

        if (!Objects.equals(user.getOptLock(), request.optLock())) {
            throw new BusinessRuleException(messageBuilder(RESOURCE_NAME, ResponseConstant.STALE_VERSION));
        }

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessRuleException(messageBuilder("Password:", ResponseConstant.OLD_PASSWORD_INVALID));
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        prepareUpdate(user);
        User updated = userRepo.save(user);

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.UPDATED.getValue());
        return new ApiPutResponseDto(updated.getOptLock(), message);
    }

    @Override
    public ApiPutResponseDto approveCustomer(String id) {
        ensureSuperAdmin();
        User user = fetchUser(getUUID(id));
        user.setIsActive(Boolean.TRUE);
        prepareUpdate(user);
        User updated = userRepo.save(user);

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.UPDATED.getValue());
        return new ApiPutResponseDto(updated.getOptLock(), message);
    }

    @Override
    public List<UserResponseDto> getAll(Boolean isActive) {
        ensureSuperAdmin();
        Specification<User> spec = UserSpecification.hasActiveStatus(isActive);
        List<User> users = userRepo.findAll(spec);
        return users
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponseDto getById(String id) {
        ensureSuperAdmin();
        User user = fetchUser(getUUID(id));
        return toResponse(user);
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

    private void ensureSuperAdmin() {
        if (!authUtil.hasRole(RoleTypeConstant.SA)) {
            throw new BusinessRuleException(messageBuilder("Access", ResponseConstant.SUPER_ADMIN_REQUIRED));
        }
    }

    private void validateEmailUniqueness(String email, UUID excludeId) {
        boolean exists = excludeId == null
                ? userRepo.existsByEmailIgnoreCase(email)
                : userRepo.existsByEmailIgnoreCaseAndIdNot(email, excludeId);
        if (exists) {
            throw new BusinessRuleException(messageBuilder("Email", ResponseConstant.ALREADY_EXISTS));
        }
    }

    private User fetchUser(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageBuilder(RESOURCE_NAME, ResponseConstant.NOT_FOUND)));
    }

    private Role fetchRoleByCode(String code) {
        return roleRepo.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageBuilder("Role", ResponseConstant.NOT_FOUND)));
    }

    private UserResponseDto toResponse(User entity) {
        return new UserResponseDto(
                entity.getId().toString(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getRole().getName(),
                entity.getIsActive(),
                entity.getOptLock());
    }
}
