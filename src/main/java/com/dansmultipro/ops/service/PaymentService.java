package com.dansmultipro.ops.service;

import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiResponseDto;
import com.dansmultipro.ops.dto.payment.*;

import java.util.UUID;

public interface PaymentService {

    ApiPostResponseDto create(PaymentCreateRequestDto request);

    ApiResponseDto updateStatus(String id, String status, PaymentStatusUpdateRequestDto request);

    PageResponseDto<PaymentResponseDto> getAll(StatusTypeConstant status, int page, int size);

    PageResponseDto<PaymentCustomerResponseDto> getAllByCustomer(UUID customerId,
                                                                 StatusTypeConstant status,
                                                                 int page, int size);

    PaymentResponseDto getById(String id);
}
