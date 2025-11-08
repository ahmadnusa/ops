package com.dansmultipro.ops.service.master;

import com.dansmultipro.ops.dto.master.ProductTypeResponseDto;

import java.util.List;

public interface ProductTypeService {

    List<ProductTypeResponseDto> findAll();

    ProductTypeResponseDto findById(String id);

    ProductTypeResponseDto findByCode(String code);
}
