package com.magyen.platform.plotter.infrastructure.persistence.entity;

import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.PlotterJobType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo relacional del agregado {@link com.magyen.platform.plotter.domain.PlotterJob}.
 * <p>
 * {@code customer_id}, {@code order_id} y {@code paper_inventory_item_id} son referencias
 * técnicas blandas (sin FK a Commercial ni Inventory).
 */
@Entity
@Table(name = "plotter_jobs")
public class PlotterJobEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private PlotterJobType jobType;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @Column(name = "paper_inventory_item_id", nullable = false)
    private UUID paperInventoryItemId;

    @Column(name = "printed_meters", nullable = false, precision = 19, scale = 4)
    private BigDecimal printedMeters;

    @Column(name = "price_per_meter", nullable = false, precision = 19, scale = 2)
    private BigDecimal pricePerMeter;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PlotterJobStatus status;

    @Column(name = "observations", length = 2000)
    private String observations;

    public PlotterJobEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlotterJobType getJobType() {
        return jobType;
    }

    public void setJobType(PlotterJobType jobType) {
        this.jobType = jobType;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public UUID getPaperInventoryItemId() {
        return paperInventoryItemId;
    }

    public void setPaperInventoryItemId(UUID paperInventoryItemId) {
        this.paperInventoryItemId = paperInventoryItemId;
    }

    public BigDecimal getPrintedMeters() {
        return printedMeters;
    }

    public void setPrintedMeters(BigDecimal printedMeters) {
        this.printedMeters = printedMeters;
    }

    public BigDecimal getPricePerMeter() {
        return pricePerMeter;
    }

    public void setPricePerMeter(BigDecimal pricePerMeter) {
        this.pricePerMeter = pricePerMeter;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PlotterJobStatus getStatus() {
        return status;
    }

    public void setStatus(PlotterJobStatus status) {
        this.status = status;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
