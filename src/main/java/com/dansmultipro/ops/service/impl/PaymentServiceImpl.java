package com.dansmultipro.ops.service.impl;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.dto.common.ApiDeleteResponseDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiPutResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentCreateRequestDto;
import com.dansmultipro.ops.dto.payment.PaymentRejectRequestDto;
import com.dansmultipro.ops.dto.payment.PaymentResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentUpdateRequestDto;
import com.dansmultipro.ops.exception.BusinessRuleException;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.Payment;
import com.dansmultipro.ops.model.master.PaymentType;
import com.dansmultipro.ops.model.master.ProductType;
import com.dansmultipro.ops.model.master.StatusType;
import com.dansmultipro.ops.model.user.User;
import com.dansmultipro.ops.repository.PaymentRepo;
import com.dansmultipro.ops.repository.PaymentTypeRepo;
import com.dansmultipro.ops.repository.ProductTypeRepo;
import com.dansmultipro.ops.repository.StatusTypeRepo;
import com.dansmultipro.ops.repository.UserRepo;
import com.dansmultipro.ops.service.PaymentService;
import com.dansmultipro.ops.spec.PaymentSpecsification;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl extends BaseService implements PaymentService {

    private static final String RESOURCE_NAME = "Payment";
    private static final DateTimeFormatter REF_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int REF_RANDOM_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PaymentRepo paymentRepo;
    private final UserRepo userRepo;
    private final ProductTypeRepo productTypeRepo;
    private final PaymentTypeRepo paymentTypeRepo;
    private final StatusTypeRepo statusTypeRepo;

    public PaymentServiceImpl(
            PaymentRepo paymentRepo,
            UserRepo userRepo,
            ProductTypeRepo productTypeRepo,
            PaymentTypeRepo paymentTypeRepo,
            StatusTypeRepo statusTypeRepo) {
        this.paymentRepo = paymentRepo;
        this.userRepo = userRepo;
        this.productTypeRepo = productTypeRepo;
        this.paymentTypeRepo = paymentTypeRepo;
        this.statusTypeRepo = statusTypeRepo;
    }

    @Override
    public ApiPostResponseDto create(PaymentCreateRequestDto request) {
        ensureCustomerRole();
        User customer = fetchUser(authUtil.getLoginId());

        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setProductType(fetchProductType(request.productType()));
        payment.setPaymentType(fetchPaymentType(request.paymentType()));
        payment.setCustomerNumber(request.customerNumber());
        payment.setAmount(request.amount());
        payment.setDescription(request.description());
        payment.setStatus(fetchStatusEntity(StatusTypeConstant.PROCESSING));
        payment.setGatewayNote(null);
        payment.setReferenceNo(null);
        payment.setReceivedAt(null);

        prepareInsert(payment);
        Payment saved = paymentRepo.save(payment);

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.SAVED.getValue());
        return new ApiPostResponseDto(saved.getId().toString(), message);
    }

    @Override
    public ApiPutResponseDto update(String id, PaymentUpdateRequestDto request) {
        ensureCustomerRole();
        Payment payment = fetchPayment(id);
        ensureOwner(payment);
        ensureProcessing(payment);

        if (!Objects.equals(payment.getOptLock(), request.optLock())) {
            throw new BusinessRuleException(messageBuilder(RESOURCE_NAME, ResponseConstant.STALE_VERSION));
        }

        payment.setProductType(fetchProductType(request.productType()));
        payment.setPaymentType(fetchPaymentType(request.paymentType()));
        payment.setCustomerNumber(request.customerNumber());
        payment.setAmount(request.amount());
        payment.setDescription(request.description());

        prepareUpdate(payment);
        Payment updated = paymentRepo.save(payment);

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.UPDATED.getValue());
        return new ApiPutResponseDto(updated.getOptLock(), message);
    }

    @Override
    public ApiDeleteResponseDto cancel(String id) {
        ensureCustomerRole();
        Payment payment = fetchPayment(id);
        ensureOwner(payment);
        ensureProcessing(payment);

        payment.setStatus(fetchStatusEntity(StatusTypeConstant.CANCELLED));
        prepareUpdate(payment);
        paymentRepo.save(payment);

        String message = messageBuilder(RESOURCE_NAME, "cancelled");
        return new ApiDeleteResponseDto(message);
    }

    @Override
    public ApiPutResponseDto approve(String id) {
        ensureGatewayRole();
        Payment payment = fetchPayment(id);
        ensureProcessing(payment);

        payment.setStatus(fetchStatusEntity(StatusTypeConstant.APPROVED));
        payment.setReferenceNo(generateReferenceNo());
        payment.setGatewayNote(null);
        payment.setReceivedAt(LocalDateTime.now());

        prepareUpdate(payment);
        Payment updated = paymentRepo.save(payment);

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.UPDATED.getValue());
        return new ApiPutResponseDto(updated.getOptLock(), message);
    }

    @Override
    public ApiPutResponseDto reject(String id, PaymentRejectRequestDto request) {
        ensureGatewayRole();
        Payment payment = fetchPayment(id);
        ensureProcessing(payment);

        payment.setStatus(fetchStatusEntity(StatusTypeConstant.REJECTED));
        payment.setGatewayNote(request.gatewayNote());
        payment.setReferenceNo(null);
        payment.setReceivedAt(null);

        prepareUpdate(payment);
        Payment updated = paymentRepo.save(payment);

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.UPDATED.getValue());
        return new ApiPutResponseDto(updated.getOptLock(), message);
    }

    @Override
    public Page<PaymentResponseDto> getAll(StatusTypeConstant status, Pageable pageable) {
        Specification<Payment> spec = Specification.allOf(
                PaymentSpecsification.isActive(Boolean.TRUE),
                PaymentSpecsification.byStatus(status));

        RoleTypeConstant role = authUtil.roleLogin();
        if (role == RoleTypeConstant.CUSTOMER) {
            spec = spec.and(PaymentSpecsification.byCustomerId(authUtil.getLoginId()));
        }

        Page<Payment> payments = paymentRepo.findAll(spec, pageable);
        return payments.map(this::toDto);
    }

    @Override
    public PaymentResponseDto getById(String id) {
        Payment payment = fetchPayment(id);
        RoleTypeConstant role = authUtil.roleLogin();
        if (role == RoleTypeConstant.CUSTOMER) {
            ensureOwner(payment);
        }
        return toDto(payment);
    }

    private void ensureCustomerRole() {
        if (!authUtil.hasRole(RoleTypeConstant.CUSTOMER)) {
            throw new BusinessRuleException(messageBuilder("Access", ResponseConstant.CUSTOMER_REQUIRED));
        }
    }

    private void ensureGatewayRole() {
        if (!authUtil.hasRole(RoleTypeConstant.GATEWAY)) {
            throw new BusinessRuleException(messageBuilder("Access", ResponseConstant.GATEWAY_REQUIRED));
        }
    }

    private void ensureOwner(Payment payment) {
        UUID loginId = authUtil.getLoginId();
        UUID customerId = payment.getCustomer().getId();
        if (!customerId.equals(loginId)) {
            throw new BusinessRuleException(messageBuilder(RESOURCE_NAME, ResponseConstant.NOT_OWNER));
        }
    }

    private void ensureProcessing(Payment payment) {
        if (payment.getStatus() == null
                || !StatusTypeConstant.PROCESSING.name().equals(payment.getStatus().getCode())) {
            throw new BusinessRuleException(messageBuilder(RESOURCE_NAME, ResponseConstant.NOT_PROCESSING));
        }
    }

    private Payment fetchPayment(String id) {
        UUID uuid = Objects.requireNonNull(getUUID(id), "Invalid UUID: id is null");
        return fetchPayment(uuid);
    }

    private Payment fetchPayment(UUID id) {
        return paymentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageBuilder(RESOURCE_NAME, ResponseConstant.NOT_FOUND)));
    }

    private User fetchUser(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageBuilder("User", ResponseConstant.NOT_FOUND)));
    }

    private ProductType fetchProductType(String code) {
        if (code == null) {
            throw new BusinessRuleException(messageBuilder("Product type", ResponseConstant.INVALID_VALUE));
        }
        return productTypeRepo.findByCode(code)
                .orElseThrow(() -> new BusinessRuleException(
                        messageBuilder("Product type", ResponseConstant.INVALID_VALUE)));
    }

    private PaymentType fetchPaymentType(String code) {
        if (code == null) {
            throw new BusinessRuleException(messageBuilder("Payment type", ResponseConstant.INVALID_VALUE));
        }
        return paymentTypeRepo.findByCode(code)
                .orElseThrow(() -> new BusinessRuleException(
                        messageBuilder("Payment type", ResponseConstant.INVALID_VALUE)));
    }

    private StatusType fetchStatusEntity(StatusTypeConstant statusType) {
        if (statusType == null) {
            throw new BusinessRuleException(messageBuilder("Status", ResponseConstant.INVALID_VALUE));
        }
        return statusTypeRepo.findByCode(statusType.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageBuilder("Status Type", ResponseConstant.NOT_FOUND)));
    }

    private String generateReferenceNo() {
        String timestamp = LocalDateTime.now().format(REF_FORMATTER);
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < REF_RANDOM_LENGTH; i++) {
            int value = RANDOM.nextInt(36);
            randomPart.append(Character.toUpperCase(Character.forDigit(value, 36)));
        }
        return "PGW-%s-%s".formatted(timestamp, randomPart);
    }

    private PaymentResponseDto toDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId().toString(),
                payment.getCustomer().getId().toString(),
                payment.getCustomer().getFullName(),
                payment.getProductType().getCode(),
                payment.getPaymentType().getCode(),
                payment.getCustomerNumber(),
                payment.getAmount(),
                payment.getStatus().getCode(),
                payment.getReferenceNo(),
                payment.getIsActive(),
                payment.getOptLock());
    }
}
