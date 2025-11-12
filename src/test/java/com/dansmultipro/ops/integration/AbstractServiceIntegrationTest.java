package com.dansmultipro.ops.integration;

import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.dto.notification.EmailNotificationMessageDto;
import com.dansmultipro.ops.listener.EmailNotificationListener;
import com.dansmultipro.ops.model.BaseEntity;
import com.dansmultipro.ops.model.Payment;
import com.dansmultipro.ops.model.User;
import com.dansmultipro.ops.model.master.PaymentType;
import com.dansmultipro.ops.model.master.ProductType;
import com.dansmultipro.ops.model.master.Role;
import com.dansmultipro.ops.model.master.StatusType;
import com.dansmultipro.ops.repository.PaymentRepo;
import com.dansmultipro.ops.repository.PaymentTypeRepo;
import com.dansmultipro.ops.repository.ProductTypeRepo;
import com.dansmultipro.ops.repository.RoleRepo;
import com.dansmultipro.ops.repository.StatusTypeRepo;
import com.dansmultipro.ops.repository.UserRepo;
import com.dansmultipro.ops.util.AuthUtil;
import com.dansmultipro.ops.util.EmailUtil;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class AbstractServiceIntegrationTest {

    protected static final UUID DEFAULT_ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    protected RoleRepo roleRepo;

    @Autowired
    protected UserRepo userRepo;

    @Autowired
    protected ProductTypeRepo productTypeRepo;

    @Autowired
    protected PaymentTypeRepo paymentTypeRepo;

    @Autowired
    protected StatusTypeRepo statusTypeRepo;

    @Autowired
    protected PaymentRepo paymentRepo;

    @MockitoBean
    protected AuthUtil authUtil;

    @MockitoBean
    protected RabbitTemplate rabbitTemplate;

    @MockitoBean
    protected EmailUtil emailUtil;

    @MockitoBean
    protected EmailNotificationListener emailNotificationListener;

    protected Role systemRole;
    protected Role customerRole;
    protected Role gatewayRole;
    protected Role superAdminRole;

    protected StatusType processingStatus;
    protected StatusType approvedStatus;
    protected StatusType rejectedStatus;
    protected StatusType cancelledStatus;

    protected ProductType defaultProductType;
    protected PaymentType defaultPaymentType;

    protected User systemUser;
    protected User customerUser;
    protected User gatewayUser;

    @BeforeEach
    void setupBaseData() {
        Mockito.reset(authUtil);

        paymentRepo.deleteAll();
        userRepo.deleteAll();
        roleRepo.deleteAll();
        paymentTypeRepo.deleteAll();
        productTypeRepo.deleteAll();
        statusTypeRepo.deleteAll();

        superAdminRole = createRole(RoleTypeConstant.SA);
        customerRole = createRole(RoleTypeConstant.CUSTOMER);
        gatewayRole = createRole(RoleTypeConstant.GATEWAY);
        systemRole = createRole(RoleTypeConstant.SYSTEM);

        defaultProductType = createProductType("ELECTRICITY_TOKEN", "Electricity Token");
        defaultPaymentType = createPaymentType("QRIS", "QRIS");

        systemUser = createUser("System", "system@ops.test", systemRole, true, "password");
        customerUser = createUser("Customer User", "customer@ops.local", customerRole, true,
                "password");
        gatewayUser = createUser("Gateway User", "gateway@ops.local", gatewayRole, true,
                "password");

        processingStatus = createStatus(StatusTypeConstant.PROCESSING);
        approvedStatus = createStatus(StatusTypeConstant.APPROVED);
        rejectedStatus = createStatus(StatusTypeConstant.REJECTED);
        cancelledStatus = createStatus(StatusTypeConstant.CANCELLED);
    }

    protected Role createRole(RoleTypeConstant type) {
        Role role = new Role();
        role.setCode(type.name());
        role.setName(type.getName());
        initializeBase(role, true, DEFAULT_ACTOR);
        return roleRepo.saveAndFlush(role);
    }

    protected StatusType createStatus(StatusTypeConstant constant) {
        StatusType status = new StatusType();
        status.setCode(constant.name());
        status.setName(constant.name());
        initializeBase(status, true, DEFAULT_ACTOR);
        return statusTypeRepo.saveAndFlush(status);
    }

    protected ProductType createProductType(String code, String name) {
        ProductType productType = new ProductType();
        productType.setCode(code);
        productType.setName(name);
        initializeBase(productType, true, DEFAULT_ACTOR);
        return productTypeRepo.saveAndFlush(productType);
    }

    protected PaymentType createPaymentType(String code, String name) {
        PaymentType paymentType = new PaymentType();
        paymentType.setCode(code);
        paymentType.setName(name);
        initializeBase(paymentType, true, DEFAULT_ACTOR);
        return paymentTypeRepo.saveAndFlush(paymentType);
    }

    protected User createUser(String fullName, String email, Role role, boolean isActive, String rawPassword) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(rawPassword);
        user.setRole(role);
        initializeBase(user, isActive, DEFAULT_ACTOR);
        return userRepo.saveAndFlush(user);
    }

    private void initializeBase(BaseEntity entity, boolean isActive, UUID actorId) {
        entity.setId(UUID.randomUUID());
        entity.setIsActive(isActive);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(actorId);
    }
}