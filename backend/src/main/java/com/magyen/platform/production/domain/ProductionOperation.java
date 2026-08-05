package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa una operación de fabricación dentro de una Orden de Producción.
 * <p>
 * Solo puede ser creada por el agregado {@link ProductionOrder}.
 */
public class ProductionOperation {

    private final UUID id;
    private final ProductionOperationType type;
    private ProductionOperationStatus status;
    private String assignedOperator;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private String observations;

    ProductionOperation(
            UUID id,
            ProductionOperationType type,
            ProductionOperationStatus status,
            String assignedOperator,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            LocalDate actualStartDate,
            LocalDate actualEndDate,
            String observations
    ) {
        this.id = Objects.requireNonNull(id, "Operation id must not be null");
        this.type = Objects.requireNonNull(type, "Operation type must not be null");
        this.status = Objects.requireNonNull(status, "Operation status must not be null");
        this.assignedOperator = assignedOperator;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.actualStartDate = actualStartDate;
        this.actualEndDate = actualEndDate;
        this.observations = observations;
    }

    static ProductionOperation create(
            ProductionOperationType type,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            String observations
    ) {
        Objects.requireNonNull(type, "Operation type must not be null");

        return new ProductionOperation(
                UUID.randomUUID(),
                type,
                ProductionOperationStatus.PENDING,
                null,
                plannedStartDate,
                plannedEndDate,
                null,
                null,
                observations
        );
    }

    /**
     * Reconstruye una operación desde persistencia. No aplica lógica de creación de negocio.
     */
    public static ProductionOperation reconstitute(
            UUID id,
            ProductionOperationType type,
            ProductionOperationStatus status,
            String assignedOperator,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            LocalDate actualStartDate,
            LocalDate actualEndDate,
            String observations
    ) {
        return new ProductionOperation(
                id,
                type,
                status,
                assignedOperator,
                plannedStartDate,
                plannedEndDate,
                actualStartDate,
                actualEndDate,
                observations
        );
    }

    /**
     * Asigna un operador responsable de la operación.
     */
    public void assignOperator(String operator) {
        if (operator == null || operator.isBlank()) {
            throw new ProductionDomainException("Assigned operator must not be blank");
        }
        this.assignedOperator = operator;
    }

    /**
     * Inicia la ejecución de la operación.
     * <p>
     * Transición válida: {@link ProductionOperationStatus#PENDING} → {@link ProductionOperationStatus#IN_PROGRESS}.
     */
    public void start() {
        if (status == ProductionOperationStatus.IN_PROGRESS) {
            throw new ProductionDomainException("Production operation is already in progress");
        }

        if (status == ProductionOperationStatus.COMPLETED) {
            throw new ProductionDomainException("A completed production operation cannot be started");
        }

        if (status != ProductionOperationStatus.PENDING) {
            throw new ProductionDomainException(
                    "Production operation can only be started from PENDING status. Current status: " + status
            );
        }

        this.status = ProductionOperationStatus.IN_PROGRESS;
        this.actualStartDate = LocalDate.now();
    }

    /**
     * Completa la ejecución de la operación.
     * <p>
     * Transición válida: {@link ProductionOperationStatus#IN_PROGRESS} → {@link ProductionOperationStatus#COMPLETED}.
     */
    public void complete() {
        if (status == ProductionOperationStatus.COMPLETED) {
            throw new ProductionDomainException("Production operation is already completed");
        }

        if (status == ProductionOperationStatus.PENDING) {
            throw new ProductionDomainException("Production operation cannot be completed before it is started");
        }

        if (status != ProductionOperationStatus.IN_PROGRESS) {
            throw new ProductionDomainException(
                    "Production operation can only be completed from IN_PROGRESS status. Current status: " + status
            );
        }

        this.status = ProductionOperationStatus.COMPLETED;
        this.actualEndDate = LocalDate.now();
    }

    public UUID getId() {
        return id;
    }

    public ProductionOperationType getType() {
        return type;
    }

    public ProductionOperationStatus getStatus() {
        return status;
    }

    public String getAssignedOperator() {
        return assignedOperator;
    }

    public LocalDate getPlannedStartDate() {
        return plannedStartDate;
    }

    public LocalDate getPlannedEndDate() {
        return plannedEndDate;
    }

    public LocalDate getActualStartDate() {
        return actualStartDate;
    }

    public LocalDate getActualEndDate() {
        return actualEndDate;
    }

    public String getObservations() {
        return observations;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ProductionOperation that = (ProductionOperation) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
