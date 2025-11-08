package com.dansmultipro.ops.service.master;

import com.dansmultipro.ops.dto.master.PaymentTypeResponseDto;

import java.util.List;

public interface PaymentTypeService {

    List<PaymentTypeResponseDto> findAll();

    PaymentTypeResponseDto findById(String id);

    PaymentTypeResponseDto findByCode(String code);
}
