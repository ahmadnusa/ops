package com.dansmultipro.ops.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.dto.common.ApiDeleteResponseDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.payment.*;
import com.dansmultipro.ops.exception.BusinessRuleException;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.Payment;
import com.dansmultipro.ops.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.dansmultipro.ops.model.master.StatusType;
import com.dansmultipro.ops.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PaymentServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void createShouldPersistPaymentAndNotifyGateway() {
        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());

        PaymentCreateRequestDto request = new PaymentCreateRequestDto(
                defaultProductType.getCode(),
                defaultPaymentType.getCode(),
                "CUST-001",
                BigDecimal.valueOf(15_000),
                "Monthly bill");

        ApiPostResponseDto response = paymentService.create(request);

        assertThat(response.id()).isNotBlank();
        assertThat(response.message()).isEqualTo("Payment has been Saved successfully.");

        Optional<Payment> saved = paymentRepo.findById(UUID.fromString(response.id()));
        assertThat(saved).isPresent();
        Payment payment = saved.get();
        assertThat(payment.getCustomer().getId()).isEqualTo(customerUser.getId());
        assertThat(payment.getStatus().getCode()).isEqualTo(StatusTypeConstant.PROCESSING.name());
        assertThat(payment.getProductType().getCode()).isEqualTo(defaultProductType.getCode());
        assertThat(payment.getPaymentType().getCode()).isEqualTo(defaultPaymentType.getCode());
    }

    @Test
    void createShouldThrowWhenProductTypeMissing() {
        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());

        PaymentCreateRequestDto request = new PaymentCreateRequestDto(
                "UNKNOWN",
                defaultPaymentType.getCode(),
                "CUST-0002",
                BigDecimal.ONE,
                null);

        assertThatThrownBy(() -> paymentService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Product type");
    }

    @Test
    void createShouldThrowWhenPaymentTypeMissing() {
        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());

        PaymentCreateRequestDto request = new PaymentCreateRequestDto(
                defaultProductType.getCode(),
                "UNKNOWN",
                "CUST-0003",
                BigDecimal.ONE,
                null);

        assertThatThrownBy(() -> paymentService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Payment type");
    }

    @Test
    void createShouldThrowWhenStatusTypeMissing() {
        statusTypeRepo.delete(processingStatus);

        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());

        PaymentCreateRequestDto request = new PaymentCreateRequestDto(
                defaultProductType.getCode(),
                defaultPaymentType.getCode(),
                "CUST-0004",
                BigDecimal.ONE,
                null);

        assertThatThrownBy(() -> paymentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Status Type");
    }

    @Test
    void updateStatusShouldCancelPaymentWhenCustomerOwner() {
        Payment payment = createPayment(customerUser);

        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());
        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(true);

        ApiDeleteResponseDto response = paymentService.updateStatus(
                payment.getId().toString(),
                "CANCELLED",
                null);

        assertThat(response.message()).contains("Updated successfully");

        Payment updated = paymentRepo.findById(payment.getId()).orElseThrow();
        assertThat(updated.getStatus().getCode()).isEqualTo(StatusTypeConstant.CANCELLED.name());
        assertThat(updated.getGatewayNote()).isNull();
        assertThat(updated.getReferenceNo()).isNull();
        assertThat(updated.getUpdatedBy()).isEqualTo(customerUser.getId());
    }

    @Test
    void updateStatusShouldRejectPaymentWithGatewayNote() {
        Payment payment = createPayment(customerUser);

        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(gatewayUser.getId());
        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(false);
        when(authUtil.hasRole(RoleTypeConstant.GATEWAY)).thenReturn(true);

        PaymentStatusUpdateRequestDto request = new PaymentStatusUpdateRequestDto("Invalid account");

        ApiDeleteResponseDto response = paymentService.updateStatus(
                payment.getId().toString(),
                "REJECTED",
                request);

        assertThat(response.message()).contains("Updated successfully");

        Payment updated = paymentRepo.findById(payment.getId()).orElseThrow();
        assertThat(updated.getStatus().getCode()).isEqualTo(StatusTypeConstant.REJECTED.name());
        assertThat(updated.getGatewayNote()).isEqualTo("Invalid account");
        assertThat(updated.getReferenceNo()).isNull();
        assertThat(updated.getUpdatedBy()).isEqualTo(gatewayUser.getId());
    }

    @Test
    void updateStatusShouldApprovePaymentAndGenerateReference() {
        Payment payment = createPayment(customerUser);

        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(gatewayUser.getId());
        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(false);
        when(authUtil.hasRole(RoleTypeConstant.GATEWAY)).thenReturn(true);

        ApiDeleteResponseDto response = paymentService.updateStatus(
                payment.getId().toString(),
                "APPROVED",
                null);

        assertThat(response.message()).contains("Updated successfully");

        Payment updated = paymentRepo.findById(payment.getId()).orElseThrow();
        assertThat(updated.getStatus().getCode()).isEqualTo(StatusTypeConstant.APPROVED.name());
        assertThat(updated.getGatewayNote()).isNull();
        assertThat(updated.getReferenceNo()).isNotBlank();
        assertThat(updated.getUpdatedBy()).isEqualTo(gatewayUser.getId());
    }

    @Test
    void updateStatusShouldThrowWhenCustomerIsNotOwner() {
        Payment payment = createPayment(customerUser);
        User anotherCustomer = createAdditionalCustomer();

        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(anotherCustomer.getId());
        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(true);

        assertThatThrownBy(() -> paymentService.updateStatus(
                payment.getId().toString(),
                "CANCELLED",
                null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not owned");
    }

    @Test
    void updateStatusShouldThrowWhenCustomerRoleMissing() {
        Payment payment = createPayment(customerUser);

        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());
        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(false);

        assertThatThrownBy(() -> paymentService.updateStatus(
                payment.getId().toString(),
                "CANCELLED",
                null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Customer privileges");
    }

    @Test
    void updateStatusShouldThrowWhenGatewayNoteMissingOnReject() {
        Payment payment = createPayment(customerUser);

        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(gatewayUser.getId());
        when(authUtil.hasRole(RoleTypeConstant.GATEWAY)).thenReturn(true);
        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(false);

        assertThatThrownBy(() -> paymentService.updateStatus(
                payment.getId().toString(),
                "REJECTED",
                new PaymentStatusUpdateRequestDto(" ")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Gateway note is required");
    }

    @Test
    void updateStatusShouldThrowWhenPaymentNotProcessing() {
        Payment payment = createPaymentWithStatus(customerUser, approvedStatus);

        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(gatewayUser.getId());
        when(authUtil.hasRole(RoleTypeConstant.GATEWAY)).thenReturn(true);
        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(false);

        assertThatThrownBy(() -> paymentService.updateStatus(
                payment.getId().toString(),
                "APPROVED",
                null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PROCESSING status");
    }

    @Test
    void updateStatusShouldThrowWhenStatusInvalid() {
        Payment payment = createPayment(customerUser);

        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(gatewayUser.getId());
        when(authUtil.hasRole(RoleTypeConstant.GATEWAY)).thenReturn(true);
        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(false);

        assertThatThrownBy(() -> paymentService.updateStatus(
                payment.getId().toString(),
                "UNKNOWN",
                null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Status");
    }

    @Test
    void updateStatusShouldThrowWhenPaymentNotFound() {
        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(gatewayUser.getId());
        when(authUtil.hasRole(RoleTypeConstant.GATEWAY)).thenReturn(true);

        assertThatThrownBy(() -> paymentService.updateStatus(
                UUID.randomUUID().toString(),
                "APPROVED",
                null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment");
    }

    @Test
    void getAllShouldReturnPagedResult() {
        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());

        createPayment(customerUser);
        createPayment(customerUser);
        createPaymentWithStatus(customerUser, approvedStatus);

        PageResponseDto<PaymentResponseDto> response = paymentService.getAll(
                StatusTypeConstant.PROCESSING,
                0,
                10);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getAllShouldHandleNegativePagingParameters() {
        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());

        createPayment(customerUser);

        PageResponseDto<PaymentResponseDto> response = paymentService.getAll(
                StatusTypeConstant.PROCESSING,
                -1,
                -5);

        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void getAllByCustomerShouldReturnOnlyOwnedPayments() {
        User anotherCustomer = createAdditionalCustomer();

        createPayment(customerUser);
        createPaymentWithStatus(customerUser, approvedStatus);
        createPayment(anotherCustomer);

        when(authUtil.isAuthenticated()).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());
        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(true);

        PageResponseDto<PaymentCustomerResponseDto> response = paymentService.getAllByCustomer(
                null,
                0,
                10);

        assertThat(response.getContent())
                .extracting(PaymentCustomerResponseDto::customerNumber)
                .allMatch(number -> number.startsWith("CUST-"));
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getAllByCustomerShouldThrowWhenRoleMissing() {
        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(false);

        assertThatThrownBy(() -> paymentService.getAllByCustomer(null, 0, 10))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Customer privileges");
    }

    @Test
    void getByIdShouldReturnPaymentForOwnerCustomer() {
        Payment payment = createPayment(customerUser);

        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(customerUser.getId());

        PaymentResponseDto response = paymentService.getById(payment.getId().toString());

        assertThat(response.id()).isEqualTo(payment.getId().toString());
        assertThat(response.customerId()).isEqualTo(customerUser.getId().toString());
    }

    @Test
    void getByIdShouldSkipOwnershipCheckForNonCustomer() {
        Payment payment = createPayment(customerUser);

        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(false);

        PaymentResponseDto response = paymentService.getById(payment.getId().toString());

        assertThat(response.id()).isEqualTo(payment.getId().toString());
    }

    @Test
    void getByIdShouldThrowWhenCustomerNotOwner() {
        Payment payment = createPayment(customerUser);
        User anotherCustomer = createAdditionalCustomer();

        when(authUtil.hasRole(RoleTypeConstant.CUSTOMER)).thenReturn(true);
        when(authUtil.getLoginId()).thenReturn(anotherCustomer.getId());

        assertThatThrownBy(() -> paymentService.getById(payment.getId().toString()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not owned");
    }

    private Payment createPayment(User customer) {
        return createPaymentWithStatus(customer, processingStatus);
    }

    private Payment createPaymentWithStatus(User customer, StatusType statusType) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setCustomer(customer);
        payment.setProductType(defaultProductType);
        payment.setPaymentType(defaultPaymentType);
        payment.setCustomerNumber("CUST-" + UUID.randomUUID().toString().substring(0, 8));
        payment.setAmount(BigDecimal.valueOf(10_000L));
        payment.setStatus(statusType);
        payment.setDescription("Test payment");
        payment.setIsActive(Boolean.TRUE);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setCreatedBy(customer.getId());
        return paymentRepo.save(payment);
    }

    private User createAdditionalCustomer() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Additional Customer");
        user.setEmail(UUID.randomUUID() + "@ops.local");
        user.setPassword("password");
        user.setRole(customerRole);
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(user.getId());
        user.setIsActive(Boolean.TRUE);
        return userRepo.save(user);
    }
}
