package com.dansmultipro.ops.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.dansmultipro.ops.model.master.PaymentType;
import com.dansmultipro.ops.model.master.ProductType;
import com.dansmultipro.ops.model.master.StatusType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_type_id", nullable = false)
    private PaymentType paymentType;

    @Column(name = "customer_number", nullable = false, length = 64)
    private String customerNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_type_id", nullable = false)
    private StatusType status;

    @Column(name = "description")
    private String description;

    @Column(name = "gateway_note")
    private String gatewayNote;

    @Column(name = "reference_no", length = 64)
    private String referenceNo;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public com.dansmultipro.ops.model.master.StatusType getStatus() {
        return status;
    }

    public void setStatus(com.dansmultipro.ops.model.master.StatusType status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGatewayNote() {
        return gatewayNote;
    }

    public void setGatewayNote(String gatewayNote) {
        this.gatewayNote = gatewayNote;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
