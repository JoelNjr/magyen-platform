package com.magyen.platform.production.infrastructure.persistence.entity;

import com.magyen.platform.production.domain.ProductionOperationStatus;
import com.magyen.platform.production.domain.ProductionOperationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo relacional de {@link com.magyen.platform.production.domain.ProductionOperation}.
 */
@Entity
@Table(name = "production_operations")
public class ProductionOperationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrderEntity productionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ProductionOperationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProductionOperationStatus status;

    @Column(name = "assigned_operator", length = 255)
    private String assignedOperator;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "observations", length = 2000)
    private String observations;

    public ProductionOperationEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProductionOrderEntity getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrderEntity productionOrder) {
        this.productionOrder = productionOrder;
    }

    public ProductionOperationType getType() {
        return type;
    }

    public void setType(ProductionOperationType type) {
        this.type = type;
    }

    public ProductionOperationStatus getStatus() {
        return status;
    }

    public void setStatus(ProductionOperationStatus status) {
        this.status = status;
    }

    public String getAssignedOperator() {
        return assignedOperator;
    }

    public void setAssignedOperator(String assignedOperator) {
        this.assignedOperator = assignedOperator;
    }

    public LocalDate getPlannedStartDate() {
        return plannedStartDate;
    }

    public void setPlannedStartDate(LocalDate plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
    }

    public LocalDate getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(LocalDate plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    public LocalDate getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(LocalDate actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public LocalDate getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
