package com.magyen.platform.production.infrastructure.persistence.entity;

import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo relacional del agregado {@link com.magyen.platform.production.domain.ProductionOrder}.
 */
@Entity
@Table(name = "production_orders")
public class ProductionOrderEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false, updatable = false, unique = true)
    private UUID orderId;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDate creationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProductionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private ProductionPriority priority;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "observations", length = 2000)
    private String observations;

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionOperationEntity> operations = new ArrayList<>();

    public ProductionOrderEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public ProductionStatus getStatus() {
        return status;
    }

    public void setStatus(ProductionStatus status) {
        this.status = status;
    }

    public ProductionPriority getPriority() {
        return priority;
    }

    public void setPriority(ProductionPriority priority) {
        this.priority = priority;
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

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public List<ProductionOperationEntity> getOperations() {
        return operations;
    }

    public void setOperations(List<ProductionOperationEntity> operations) {
        this.operations = operations;
    }
}
