package com.dansmultipro.ops.service;

import com.dansmultipro.ops.dto.master.PaymentTypeResponseDto;

import java.util.List;

public interface PaymentTypeService {

    List<PaymentTypeResponseDto> findAll();

    PaymentTypeResponseDto findById(String id);
}
