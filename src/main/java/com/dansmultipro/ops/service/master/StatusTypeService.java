package com.dansmultipro.ops.service.master;

import com.dansmultipro.ops.dto.master.StatusTypeResponseDto;

import java.util.List;

public interface StatusTypeService {

    List<StatusTypeResponseDto> findAll();

    StatusTypeResponseDto findById(String id);

    StatusTypeResponseDto findByCode(String code);
}
