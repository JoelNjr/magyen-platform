package com.magyen.platform.plotter.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo relacional de {@link com.magyen.platform.plotter.domain.PlotterPayment}.
 * <p>
 * {@code plotter_job_id} es referencia UUID blanda; sin FK JPA al agregado PlotterJob.
 */
@Entity
@Table(name = "plotter_payments")
public class PlotterPaymentEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    /**
     * Nulo en pagos de cuenta corriente. Los pagos históricos siguen apuntando al trabajo.
     */
    @Column(name = "plotter_job_id")
    private UUID plotterJobId;

    @Column(name = "registration_key")
    private UUID registrationKey;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false, updatable = false)
    private LocalDate paymentDate;

    @Column(name = "observations", length = 2000)
    private String observations;

    public PlotterPaymentEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getPlotterJobId() {
        return plotterJobId;
    }

    public void setPlotterJobId(UUID plotterJobId) {
        this.plotterJobId = plotterJobId;
    }

    public UUID getRegistrationKey() {
        return registrationKey;
    }

    public void setRegistrationKey(UUID registrationKey) {
        this.registrationKey = registrationKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
