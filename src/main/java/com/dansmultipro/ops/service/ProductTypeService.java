package com.dansmultipro.ops.service;

import com.dansmultipro.ops.dto.master.ProductTypeResponseDto;

import java.util.List;

public interface ProductTypeService {

    List<ProductTypeResponseDto> findAll();

    ProductTypeResponseDto findById(String id);
}
