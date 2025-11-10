package com.dansmultipro.ops.service;

import java.util.List;

import com.dansmultipro.ops.dto.master.RoleResponseDto;

public interface RoleService {

    List<RoleResponseDto> findAll();

    RoleResponseDto findById(String id);
}
