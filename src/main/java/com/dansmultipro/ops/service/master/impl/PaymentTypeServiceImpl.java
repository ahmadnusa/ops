package com.dansmultipro.ops.service.master.impl;

import com.dansmultipro.ops.constant.ResponseConstant;
import com.dansmultipro.ops.dto.master.PaymentTypeResponseDto;
import com.dansmultipro.ops.exception.ResourceNotFoundException;
import com.dansmultipro.ops.model.master.PaymentType;
import com.dansmultipro.ops.repository.PaymentTypeRepository;
import com.dansmultipro.ops.service.BaseService;
import com.dansmultipro.ops.service.master.PaymentTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Service
public class PaymentTypeServiceImpl extends BaseService implements PaymentTypeService {

    private final PaymentTypeRepository repository;

    public PaymentTypeServiceImpl(PaymentTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PaymentTypeResponseDto> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public PaymentTypeResponseDto findById(String id) {
        UUID uuid = Objects.requireNonNull(getUUID(id), "Invalid UUID: id is null");
        PaymentType paymentType = repository.findById(uuid).orElseThrow(
                () -> new ResourceNotFoundException(messageBuilder("Payment Type", ResponseConstant.NOT_FOUND)));
        return toDto(paymentType);
    }

    @Override
    public PaymentTypeResponseDto findByCode(String code) {
        PaymentType paymentType = repository.findByCode(code).orElseThrow(
                () -> new ResourceNotFoundException(messageBuilder("Payment Type", ResponseConstant.NOT_FOUND)));
        return toDto(paymentType);
    }

    private PaymentTypeResponseDto toDto(PaymentType entity) {
        return new PaymentTypeResponseDto(
                entity.getId().toString(),
                entity.getCode(),
                entity.getName(),
                entity.getIsActive(),
                entity.getOptLock());
    }
}
