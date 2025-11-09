package com.dansmultipro.ops.spec;

import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.model.Payment;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class PaymentSpecsification {

    private PaymentSpecsification() {
    }

    public static Specification<Payment> isActive(Boolean isActive) {
        return (root, query, builder) -> isActive == null
                ? null
                : builder.equal(root.get("isActive"), isActive);
    }

    public static Specification<Payment> byStatus(StatusTypeConstant status) {
        return (root, query, builder) -> status == null
                ? null
                : builder.equal(root.get("status").get("code"), status.name());
    }

    public static Specification<Payment> byCustomerId(UUID customerId) {
        return (root, query, builder) -> customerId == null
                ? null
                : builder.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Payment> byPaymentType(String paymentTypeCode) {
        return (root, query, builder) -> paymentTypeCode == null
                ? null
                : builder.equal(root.get("paymentType").get("code"), paymentTypeCode);
    }

    public static Specification<Payment> byProductType(String productTypeCode) {
        return (root, query, builder) -> productTypeCode == null
                ? null
                : builder.equal(root.get("productType").get("code"), productTypeCode);
    }
}
