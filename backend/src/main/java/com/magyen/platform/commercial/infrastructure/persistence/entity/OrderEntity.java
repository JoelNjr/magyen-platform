package com.magyen.platform.commercial.infrastructure.persistence.entity;

import com.magyen.platform.commercial.domain.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo relacional del agregado {@link com.magyen.platform.commercial.domain.Order}.
 */
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_number", nullable = false, updatable = false, length = 100)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "quotation_id", nullable = false, updatable = false)
    private UUID quotationId;

    @Column(name = "confirmation_date", nullable = false, updatable = false)
    private LocalDate confirmationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "promised_delivery_date", nullable = false)
    private LocalDate promisedDeliveryDate;

    @Column(name = "delivery_observations", length = 2000)
    private String deliveryObservations;

    @Column(name = "advance_acknowledged", nullable = false)
    private boolean advanceAcknowledged;

    @Column(name = "final_payment_acknowledged", nullable = false)
    private boolean finalPaymentAcknowledged;

    @Column(name = "committed_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal committedTotal;

    @Column(name = "remaining_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingBalance;

    @Column(name = "salesperson", nullable = false, length = 255)
    private String salesperson;

    @Column(name = "observations", length = 2000)
    private String observations;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    public OrderEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(UUID quotationId) {
        this.quotationId = quotationId;
    }

    public LocalDate getConfirmationDate() {
        return confirmationDate;
    }

    public void setConfirmationDate(LocalDate confirmationDate) {
        this.confirmationDate = confirmationDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDate getPromisedDeliveryDate() {
        return promisedDeliveryDate;
    }

    public void setPromisedDeliveryDate(LocalDate promisedDeliveryDate) {
        this.promisedDeliveryDate = promisedDeliveryDate;
    }

    public String getDeliveryObservations() {
        return deliveryObservations;
    }

    public void setDeliveryObservations(String deliveryObservations) {
        this.deliveryObservations = deliveryObservations;
    }

    public boolean isAdvanceAcknowledged() {
        return advanceAcknowledged;
    }

    public void setAdvanceAcknowledged(boolean advanceAcknowledged) {
        this.advanceAcknowledged = advanceAcknowledged;
    }

    public boolean isFinalPaymentAcknowledged() {
        return finalPaymentAcknowledged;
    }

    public void setFinalPaymentAcknowledged(boolean finalPaymentAcknowledged) {
        this.finalPaymentAcknowledged = finalPaymentAcknowledged;
    }

    public BigDecimal getCommittedTotal() {
        return committedTotal;
    }

    public void setCommittedTotal(BigDecimal committedTotal) {
        this.committedTotal = committedTotal;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public String getSalesperson() {
        return salesperson;
    }

    public void setSalesperson(String salesperson) {
        this.salesperson = salesperson;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEntity> items) {
        this.items = items;
    }
}
