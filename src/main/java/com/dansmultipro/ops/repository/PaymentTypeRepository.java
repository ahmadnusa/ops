package com.dansmultipro.ops.repository;

import com.dansmultipro.ops.model.master.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTypeRepository extends JpaRepository<PaymentType, UUID> {

    Optional<PaymentType> findByCode(String code);
}
