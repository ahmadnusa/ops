package com.dansmultipro.ops.service;

import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.dto.common.ApiDeleteResponseDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.payment.PageResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentCreateRequestDto;
import com.dansmultipro.ops.dto.payment.PaymentCustomerResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentStatusUpdateRequestDto;

public interface PaymentService {

    ApiPostResponseDto create(PaymentCreateRequestDto request);

    ApiDeleteResponseDto updateStatus(String id, String status, PaymentStatusUpdateRequestDto request);

    PageResponseDto<PaymentResponseDto> getAll(StatusTypeConstant status, int page, int size);

    PageResponseDto<PaymentCustomerResponseDto> getAllByCustomer(StatusTypeConstant status, int page, int size);

    PaymentResponseDto getById(String id);
}
