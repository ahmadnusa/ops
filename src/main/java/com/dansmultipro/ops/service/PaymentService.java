package com.dansmultipro.ops.service;

import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.dto.common.ApiDeleteResponseDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiPutResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentCreateRequestDto;
import com.dansmultipro.ops.dto.payment.PaymentRejectRequestDto;
import com.dansmultipro.ops.dto.payment.PaymentResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    ApiPostResponseDto create(PaymentCreateRequestDto request);

    ApiPutResponseDto update(String id, PaymentUpdateRequestDto request);

    ApiDeleteResponseDto cancel(String id);

    ApiPutResponseDto approve(String id);

    ApiPutResponseDto reject(String id, PaymentRejectRequestDto request);

    Page<PaymentResponseDto> getAll(StatusTypeConstant status, Pageable pageable);

    PaymentResponseDto getById(String id);
}
