package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root de la instrucción de fabricación generada desde una Orden comercial.
 * <p>
 * Mantiene la consistencia de la Orden de Producción y sus operaciones.
 */
public class ProductionOrder {

    private final UUID id;
    private final UUID orderId;
    private final LocalDate creationDate;
    private ProductionStatus status;
    private ProductionPriority priority;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private final String observations;
    private final List<ProductionOperation> operations;

    private ProductionOrder(
            UUID id,
            UUID orderId,
            LocalDate creationDate,
            ProductionStatus status,
            ProductionPriority priority,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            String observations,
            List<ProductionOperation> operations
    ) {
        this.id = Objects.requireNonNull(id, "Production order id must not be null");
        this.orderId = Objects.requireNonNull(orderId, "Order id must not be null");
        this.creationDate = Objects.requireNonNull(creationDate, "Creation date must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.priority = Objects.requireNonNull(priority, "Priority must not be null");
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.observations = observations;
        this.operations = new ArrayList<>(Objects.requireNonNull(operations, "Operations must not be null"));
    }

    /**
     * Crea una Orden de Producción en estado inicial válido {@link ProductionStatus#CREATED}.
     * <p>
     * Referencia la Orden comercial únicamente por identidad.
     */
    public static ProductionOrder create(
            UUID orderId,
            LocalDate creationDate,
            ProductionPriority priority,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            String observations
    ) {
        return new ProductionOrder(
                UUID.randomUUID(),
                orderId,
                creationDate,
                ProductionStatus.CREATED,
                priority,
                plannedStartDate,
                plannedEndDate,
                observations,
                List.of()
        );
    }

    /**
     * Reconstruye una Orden de Producción desde persistencia. No aplica lógica de creación de negocio.
     */
    public static ProductionOrder reconstitute(
            UUID id,
            UUID orderId,
            LocalDate creationDate,
            ProductionStatus status,
            ProductionPriority priority,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            String observations,
            List<ProductionOperation> operations
    ) {
        return new ProductionOrder(
                id,
                orderId,
                creationDate,
                status,
                priority,
                plannedStartDate,
                plannedEndDate,
                observations,
                operations
        );
    }

    /**
     * Planifica la Orden de Producción.
     * <p>
     * Transición válida: {@link ProductionStatus#CREATED} → {@link ProductionStatus#PLANNED}.
     */
    public void plan(
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            ProductionPriority priority
    ) {
        Objects.requireNonNull(plannedStartDate, "Planned start date must not be null");
        Objects.requireNonNull(plannedEndDate, "Planned end date must not be null");
        Objects.requireNonNull(priority, "Priority must not be null");

        if (plannedEndDate.isBefore(plannedStartDate)) {
            throw new ProductionDomainException("Planned end date must not be before planned start date");
        }

        if (status != ProductionStatus.CREATED) {
            throw new ProductionDomainException(
                    "A production order can only be planned from CREATED status. Current status: " + status
            );
        }

        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.priority = priority;
        this.status = ProductionStatus.PLANNED;
    }

    /**
     * Inicia la ejecución de la Orden de Producción.
     * <p>
     * Transición válida: {@link ProductionStatus#PLANNED} → {@link ProductionStatus#IN_PROGRESS}.
     */
    public void start() {
        if (status == ProductionStatus.IN_PROGRESS) {
            return;
        }

        if (status != ProductionStatus.PLANNED) {
            throw new ProductionDomainException(
                    "A production order can only be started from PLANNED status. Current status: " + status
            );
        }

        this.status = ProductionStatus.IN_PROGRESS;
    }

    /**
     * Completa la Orden de Producción.
     * <p>
     * Transición válida: {@link ProductionStatus#IN_PROGRESS} → {@link ProductionStatus#COMPLETED}.
     * Todas las operaciones deben estar completadas.
     */
    public void complete() {
        if (status == ProductionStatus.COMPLETED) {
            return;
        }

        if (status != ProductionStatus.IN_PROGRESS) {
            throw new ProductionDomainException(
                    "A production order can only be completed from IN_PROGRESS status. Current status: " + status
            );
        }

        ensureAllOperationsCompleted();

        this.status = ProductionStatus.COMPLETED;
    }

    /**
     * Agrega una operación de fabricación a la Orden de Producción.
     * <p>
     * Solo permitido mientras el estado sea {@link ProductionStatus#CREATED}.
     * No permite tipos de operación duplicados dentro de la misma orden.
     */
    public void addOperation(
            ProductionOperationType type,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            String observations
    ) {
        ensureEditable();
        Objects.requireNonNull(type, "Operation type must not be null");
        ensureOperationTypeIsUnique(type);

        ProductionOperation operation = ProductionOperation.create(
                type,
                plannedStartDate,
                plannedEndDate,
                observations
        );
        operations.add(operation);
    }

    /**
     * Elimina una operación de fabricación de la Orden de Producción.
     * <p>
     * Solo permitido mientras el estado sea {@link ProductionStatus#CREATED}.
     */
    public void removeOperation(UUID operationId) {
        ensureEditable();
        Objects.requireNonNull(operationId, "Operation id must not be null");

        boolean removed = operations.removeIf(operation -> operation.getId().equals(operationId));
        if (!removed) {
            throw new ProductionDomainException("Production operation not found: " + operationId);
        }
    }

    /**
     * Asigna un operador a una operación de fabricación existente.
     */
    public void assignOperator(UUID operationId, String operator) {
        findOperation(operationId).assignOperator(operator);
    }

    /**
     * Inicia una operación de fabricación existente.
     * <p>
     * Solo permitido mientras el estado de la Orden de Producción sea
     * {@link ProductionStatus#IN_PROGRESS}, para preservar el ciclo
     * CREATED → PLANNED → IN_PROGRESS → COMPLETED.
     */
    public void startOperation(UUID operationId) {
        ensureExecutable();
        findOperation(operationId).start();
    }

    /**
     * Completa una operación de fabricación existente.
     * <p>
     * Solo permitido mientras el estado de la Orden de Producción sea
     * {@link ProductionStatus#IN_PROGRESS}.
     */
    public void completeOperation(UUID operationId) {
        ensureExecutable();
        findOperation(operationId).complete();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public ProductionStatus getStatus() {
        return status;
    }

    public ProductionPriority getPriority() {
        return priority;
    }

    public LocalDate getPlannedStartDate() {
        return plannedStartDate;
    }

    public LocalDate getPlannedEndDate() {
        return plannedEndDate;
    }

    public String getObservations() {
        return observations;
    }

    public List<ProductionOperation> getOperations() {
        return Collections.unmodifiableList(operations);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ProductionOrder that = (ProductionOrder) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void ensureEditable() {
        if (status != ProductionStatus.CREATED) {
            throw new ProductionDomainException(
                    "Production operations can only be modified while status is CREATED. Current status: "
                            + status
            );
        }
    }

    private void ensureExecutable() {
        if (status != ProductionStatus.IN_PROGRESS) {
            throw new ProductionDomainException(
                    "Production operations can only be executed while status is IN_PROGRESS. Current status: "
                            + status
            );
        }
    }

    private void ensureOperationTypeIsUnique(ProductionOperationType type) {
        boolean typeAlreadyExists = operations.stream()
                .anyMatch(operation -> operation.getType() == type);

        if (typeAlreadyExists) {
            throw new ProductionDomainException(
                    "A production operation of type " + type + " already exists in this production order"
            );
        }
    }

    private ProductionOperation findOperation(UUID operationId) {
        Objects.requireNonNull(operationId, "Operation id must not be null");

        return operations.stream()
                .filter(operation -> operation.getId().equals(operationId))
                .findFirst()
                .orElseThrow(() -> new ProductionDomainException(
                        "Production operation not found: " + operationId
                ));
    }

    private void ensureAllOperationsCompleted() {
        boolean allCompleted = operations.stream()
                .allMatch(operation -> operation.getStatus() == ProductionOperationStatus.COMPLETED);

        if (!allCompleted) {
            throw new ProductionDomainException(
                    "A production order cannot be completed while operations remain unfinished"
            );
        }
    }
}
