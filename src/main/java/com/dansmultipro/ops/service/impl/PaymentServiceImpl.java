package com.dansmultipro.ops.service.impl;

import com.dansmultipro.ops.config.RabbitConfig;
import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiResponseDto;
import com.dansmultipro.ops.dto.notification.EmailNotificationMessageDto;
import com.dansmultipro.ops.dto.notification.PaymentEmailPayload;
import com.dansmultipro.ops.dto.payment.*;
import com.dansmultipro.ops.exception.BusinessRuleException;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.Payment;
import com.dansmultipro.ops.model.User;
import com.dansmultipro.ops.model.master.PaymentType;
import com.dansmultipro.ops.model.master.ProductType;
import com.dansmultipro.ops.model.master.StatusType;
import com.dansmultipro.ops.repository.PaymentRepo;
import com.dansmultipro.ops.repository.PaymentTypeRepo;
import com.dansmultipro.ops.repository.ProductTypeRepo;
import com.dansmultipro.ops.repository.StatusTypeRepo;
import com.dansmultipro.ops.service.PaymentService;
import com.dansmultipro.ops.spec.PaymentSpecsification;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentServiceImpl extends BaseService implements PaymentService {

    private static final String RESOURCE_NAME = "Payment";

    private final PaymentRepo paymentRepo;
    private final ProductTypeRepo productTypeRepo;
    private final PaymentTypeRepo paymentTypeRepo;
    private final StatusTypeRepo statusTypeRepo;
    private final RabbitTemplate rabbitTemplate;

    public PaymentServiceImpl(
            PaymentRepo paymentRepo,
            ProductTypeRepo productTypeRepo,
            PaymentTypeRepo paymentTypeRepo,
            StatusTypeRepo statusTypeRepo, RabbitTemplate rabbitTemplate) {
        this.paymentRepo = paymentRepo;
        this.productTypeRepo = productTypeRepo;
        this.paymentTypeRepo = paymentTypeRepo;
        this.statusTypeRepo = statusTypeRepo;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public ApiPostResponseDto create(PaymentCreateRequestDto request) {
        User customer = fetchUser(authUtil.getLoginId());

        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setProductType(fetchProductType(request.productType()));
        payment.setPaymentType(fetchPaymentType(request.paymentType()));
        payment.setCustomerNumber(request.customerNumber());
        payment.setAmount(request.amount());
        payment.setDescription(request.description());
        payment.setStatus(fetchStatusType(StatusTypeConstant.PROCESSING));
        payment.setGatewayNote(null);
        payment.setReferenceNo(null);

        Payment saved = paymentRepo.save(prepareCreate(payment));

        notifyGatewayOnCreation(saved);

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.SAVED.getValue());
        return new ApiPostResponseDto(saved.getId().toString(), message);
    }

    @Override
    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public ApiResponseDto updateStatus(String id, String status, PaymentStatusUpdateRequestDto request) {
        String normalizedStatus = status.trim().toUpperCase();

        Payment payment = fetchPayment(getUUID(id));
        ensureProcessing(payment);

        switch (normalizedStatus) {
            case "CANCELLED" -> {
                ensureCustomerRole();
                ensureOwner(payment);
                applyStatusChange(payment, StatusTypeConstant.CANCELLED, null, null);
            }
            case "APPROVED" -> {
                ensureGatewayRole();
                applyStatusChange(payment, StatusTypeConstant.APPROVED, null, generateReferenceNo());
            }
            case "REJECTED" -> {
                ensureGatewayRole();
                String gatewayNote = request == null ? null : request.gatewayNote();
                if (gatewayNote == null || gatewayNote.isBlank()) {
                    throw new BusinessRuleException("Gateway note is required when rejecting a payment.");
                }
                applyStatusChange(payment, StatusTypeConstant.REJECTED, gatewayNote, null);
            }
            default -> throw new BusinessRuleException(
                    messageBuilder("Status", ResponseConstant.INVALID_VALUE));
        }

        Payment updated = paymentRepo.save(prepareUpdate(payment));

        if (!normalizedStatus.equals(StatusTypeConstant.CANCELLED.name())) {
            notifyCustomerOnStatusChange(updated);
        }

        String message = messageBuilder(RESOURCE_NAME, ResponseConstant.UPDATED.getValue());
        return new ApiResponseDto(message);
    }

    @Override
    @Cacheable(
            value = "payments",
            key = "'getAll:' + (#status == null ? 'ALL' : #status.name()) + ':' + #page + ':' + #size"
    )
    public PageResponseDto<PaymentResponseDto> getAll(StatusTypeConstant status, int page, int size) {
        Specification<Payment> spec = Specification.allOf(PaymentSpecsification.byStatus(status));

        Pageable pageable = buildPageable(page, size);
        Page<Payment> payments = paymentRepo.findAll(spec, pageable);
        Page<PaymentResponseDto> mappedDto = payments.map(this::toListDto);

        return toPageResponse(mappedDto);
    }

    @Override
    @Cacheable(
            value = "payments",
            key = "'getAllByCustomer:' + (#status == null ? 'ALL' : #status.name()) + ':' + #page + ':' + #size + ':' + #customerId"
    )
    public PageResponseDto<PaymentCustomerResponseDto> getAllByCustomer(UUID customerId,
                                                                        StatusTypeConstant status,
                                                                        int page,
                                                                        int size) {
        Specification<Payment> spec = Specification.allOf(
                PaymentSpecsification.byStatus(status),
                PaymentSpecsification.byCustomerId(customerId));

        Pageable pageable = buildPageable(page, size);
        Page<Payment> payments = paymentRepo.findAll(spec, pageable);
        Page<PaymentCustomerResponseDto> mappedDto = payments.map(this::toCustomerDto);

        return toPageResponse(mappedDto);
    }

    @Override
    public PaymentResponseDto getById(String id) {
        Payment payment = fetchPayment(getUUID(id));

        if (authUtil.hasRole(RoleTypeConstant.CUSTOMER)) {
            ensureOwner(payment);
        }

        return toListDto(payment);
    }

    private void applyStatusChange(Payment payment, StatusTypeConstant statusType, String gatewayNote,
            String referenceNo) {
        payment.setStatus(fetchStatusType(statusType));
        payment.setGatewayNote(gatewayNote);
        payment.setReferenceNo(referenceNo);
    }

    private void notifyGatewayOnCreation(Payment payment) {
        resolveGatewayEmail().ifPresent(email -> {
            EmailNotificationMessageDto message = new EmailNotificationMessageDto(
                    email,
                    toPaymentPayload(payment),
                    null);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.NOTIFICATION_EXCHANGE,
                    RabbitConfig.PAYMENT_GATEWAY_NOTIFICATION_ROUTING_KEY,
                    message);
        });
    }

    private void notifyCustomerOnStatusChange(Payment payment) {
        String email = payment.getCustomer().getEmail();

        EmailNotificationMessageDto message = new EmailNotificationMessageDto(
                email,
                toPaymentPayload(payment),
                null);

        rabbitTemplate.convertAndSend(
                RabbitConfig.NOTIFICATION_EXCHANGE,
                RabbitConfig.PAYMENT_CUSTOMER_NOTIFICATION_ROUTING_KEY,
                message);
    }

    private Optional<String> resolveGatewayEmail() {
        return userRepo.findFirstByRoleCode(RoleTypeConstant.GATEWAY.name())
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .map(User::getEmail);
    }

    private PaymentEmailPayload toPaymentPayload(Payment payment) {
        return new PaymentEmailPayload(
                payment.getId().toString(),
                payment.getCustomer().getFullName(),
                payment.getCustomerNumber(),
                payment.getStatus().getCode(),
                payment.getGatewayNote(),
                payment.getReferenceNo());
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
        if (!loginId.equals(customerId)) {
            throw new BusinessRuleException(messageBuilder(RESOURCE_NAME, ResponseConstant.NOT_OWNER));
        }
    }

    private void ensureProcessing(Payment payment) {
        if (!StatusTypeConstant.PROCESSING.name().equals(payment.getStatus().getCode())) {
            throw new BusinessRuleException(messageBuilder(RESOURCE_NAME, ResponseConstant.NOT_PROCESSING));
        }
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
        return productTypeRepo.findByCode(code)
                .orElseThrow(() -> new BusinessRuleException(
                        messageBuilder("Product type", ResponseConstant.NOT_FOUND)));
    }

    private PaymentType fetchPaymentType(String code) {
        return paymentTypeRepo.findByCode(code)
                .orElseThrow(() -> new BusinessRuleException(
                        messageBuilder("Payment type", ResponseConstant.NOT_FOUND)));
    }

    private StatusType fetchStatusType(StatusTypeConstant statusType) {
        return statusTypeRepo.findByCode(statusType.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageBuilder("Status Type", ResponseConstant.NOT_FOUND)));
    }

    private String generateReferenceNo() {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int bound = (int) Math.pow(36, 6);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String rand6 = Integer.toString(random.nextInt(bound), 36).toUpperCase();
        return "PGW-%s-%s".formatted(timeStamp, rand6);
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        return PageRequest.of(safePage, safeSize);
    }

    private PaymentResponseDto toListDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId().toString(),
                payment.getCustomer().getId().toString(),
                payment.getCustomer().getFullName(),
                payment.getProductType().getCode(),
                payment.getPaymentType().getCode(),
                payment.getCustomerNumber(),
                payment.getAmount().toString(),
                payment.getStatus().getCode(),
                payment.getReferenceNo(),
                payment.getIsActive(),
                payment.getOptLock());
    }

    private PaymentCustomerResponseDto toCustomerDto(Payment payment) {
        return new PaymentCustomerResponseDto(
                payment.getId().toString(),
                payment.getProductType().getCode(),
                payment.getPaymentType().getCode(),
                payment.getCustomerNumber(),
                payment.getAmount().toString(),
                payment.getStatus().getCode(),
                payment.getReferenceNo());
    }

    private <T> PageResponseDto<T> toPageResponse(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
