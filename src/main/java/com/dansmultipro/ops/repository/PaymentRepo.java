package com.dansmultipro.ops.repository;

import com.dansmultipro.ops.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {
}
