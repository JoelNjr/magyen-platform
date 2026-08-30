package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.shared.domain.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root de la instrucción de fabricación generada desde una Orden comercial.
 * <p>
 * Mantiene la consistencia de la Orden de Producción, su snapshot productivo,
 * sus operaciones y los consumos reales de material.
 * <p>
 * Relación de negocio: una Orden comercial puede tener como máximo una Orden de Producción.
 * La referencia técnica se conserva mediante {@code orderId}.
 */
public class ProductionOrder {

    private final UUID id;
    private final UUID orderId;
    private final LocalDate creationDate;
    private ProductionStatus status;
    private ProductionPriority priority;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualCompletionDate;
    private final String observations;
    private final List<ProductionItem> items;
    private final List<ProductionOperation> operations;
    private final List<ProductionMaterialConsumption> materialConsumptions;
    private final List<ProductionLaborWork> laborWorks;
    private final List<ProductionAdditionalCost> additionalCosts;
    private ProductionReferenceImage referenceImage;

    private ProductionOrder(
            UUID id,
            UUID orderId,
            LocalDate creationDate,
            ProductionStatus status,
            ProductionPriority priority,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            LocalDate actualStartDate,
            LocalDate actualCompletionDate,
            String observations,
            List<ProductionItem> items,
            List<ProductionOperation> operations,
            List<ProductionMaterialConsumption> materialConsumptions,
            List<ProductionLaborWork> laborWorks,
            List<ProductionAdditionalCost> additionalCosts,
            ProductionReferenceImage referenceImage
    ) {
        this.id = Objects.requireNonNull(id, "Production order id must not be null");
        this.orderId = Objects.requireNonNull(orderId, "Order id must not be null");
        this.creationDate = Objects.requireNonNull(creationDate, "Creation date must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.priority = Objects.requireNonNull(priority, "Priority must not be null");
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.actualStartDate = actualStartDate;
        this.actualCompletionDate = actualCompletionDate;
        this.observations = observations;
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Items must not be null"));
        this.operations = new ArrayList<>(Objects.requireNonNull(operations, "Operations must not be null"));
        this.materialConsumptions = new ArrayList<>(
                Objects.requireNonNull(materialConsumptions, "Material consumptions must not be null")
        );
        this.laborWorks = new ArrayList<>(
                Objects.requireNonNull(laborWorks, "Labor works must not be null")
        );
        this.additionalCosts = new ArrayList<>(
                Objects.requireNonNull(additionalCosts, "Additional costs must not be null")
        );
        this.referenceImage = referenceImage;
    }

    /**
     * Crea una Orden de Producción en estado inicial válido {@link ProductionStatus#CREATED}.
     * <p>
     * Referencia la Orden comercial únicamente por identidad y puede incorporar
     * un snapshot productivo independiente del estado posterior de los ítems comerciales.
     */
    public static ProductionOrder create(
            UUID orderId,
            LocalDate creationDate,
            ProductionPriority priority,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            String observations
    ) {
        return create(
                orderId,
                creationDate,
                priority,
                plannedStartDate,
                plannedEndDate,
                observations,
                List.of()
        );
    }

    /**
     * Crea una Orden de Producción con snapshot productivo.
     */
    public static ProductionOrder create(
            UUID orderId,
            LocalDate creationDate,
            ProductionPriority priority,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            String observations,
            List<ProductionItem> items
    ) {
        return new ProductionOrder(
                UUID.randomUUID(),
                orderId,
                creationDate,
                ProductionStatus.CREATED,
                priority,
                plannedStartDate,
                plannedEndDate,
                null,
                null,
                observations,
                items == null ? List.of() : items,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null
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
            List<ProductionItem> items,
            List<ProductionOperation> operations
    ) {
        return reconstitute(
                id,
                orderId,
                creationDate,
                status,
                priority,
                plannedStartDate,
                plannedEndDate,
                observations,
                items,
                operations,
                List.of()
        );
    }

    /**
     * Reconstruye una Orden de Producción incluyendo consumos de material.
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
            List<ProductionItem> items,
            List<ProductionOperation> operations,
            List<ProductionMaterialConsumption> materialConsumptions
    ) {
        return reconstitute(
                id,
                orderId,
                creationDate,
                status,
                priority,
                plannedStartDate,
                plannedEndDate,
                observations,
                items,
                operations,
                materialConsumptions,
                List.of()
        );
    }

    /**
     * Reconstruye una Orden de Producción incluyendo consumos y mano de obra.
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
            List<ProductionItem> items,
            List<ProductionOperation> operations,
            List<ProductionMaterialConsumption> materialConsumptions,
            List<ProductionLaborWork> laborWorks
    ) {
        return reconstitute(
                id,
                orderId,
                creationDate,
                status,
                priority,
                plannedStartDate,
                plannedEndDate,
                observations,
                items,
                operations,
                materialConsumptions,
                laborWorks,
                null,
                null
        );
    }

    /**
     * Reconstruye una Orden de Producción incluyendo fechas reales de inicio y cierre.
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
            List<ProductionItem> items,
            List<ProductionOperation> operations,
            List<ProductionMaterialConsumption> materialConsumptions,
            List<ProductionLaborWork> laborWorks,
            LocalDate actualStartDate,
            LocalDate actualCompletionDate
    ) {
        return reconstitute(
                id,
                orderId,
                creationDate,
                status,
                priority,
                plannedStartDate,
                plannedEndDate,
                observations,
                items,
                operations,
                materialConsumptions,
                laborWorks,
                actualStartDate,
                actualCompletionDate,
                null
        );
    }

    /**
     * Reconstruye una Orden de Producción incluyendo la referencia de imagen operativa.
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
            List<ProductionItem> items,
            List<ProductionOperation> operations,
            List<ProductionMaterialConsumption> materialConsumptions,
            List<ProductionLaborWork> laborWorks,
            LocalDate actualStartDate,
            LocalDate actualCompletionDate,
            ProductionReferenceImage referenceImage
    ) {
        return reconstitute(
                id,
                orderId,
                creationDate,
                status,
                priority,
                plannedStartDate,
                plannedEndDate,
                observations,
                items,
                operations,
                materialConsumptions,
                laborWorks,
                actualStartDate,
                actualCompletionDate,
                referenceImage,
                List.of()
        );
    }

    /**
     * Reconstruye una Orden de Producción incluyendo costos directos adicionales.
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
            List<ProductionItem> items,
            List<ProductionOperation> operations,
            List<ProductionMaterialConsumption> materialConsumptions,
            List<ProductionLaborWork> laborWorks,
            LocalDate actualStartDate,
            LocalDate actualCompletionDate,
            ProductionReferenceImage referenceImage,
            List<ProductionAdditionalCost> additionalCosts
    ) {
        return new ProductionOrder(
                id,
                orderId,
                creationDate,
                status,
                priority,
                plannedStartDate,
                plannedEndDate,
                actualStartDate,
                actualCompletionDate,
                observations,
                items == null ? List.of() : items,
                operations == null ? List.of() : operations,
                materialConsumptions == null ? List.of() : materialConsumptions,
                laborWorks == null ? List.of() : laborWorks,
                additionalCosts == null ? List.of() : additionalCosts,
                referenceImage
        );
    }

    public void attachReferenceImage(ProductionReferenceImage referenceImage) {
        this.referenceImage = Objects.requireNonNull(referenceImage, "Reference image must not be null");
    }

    public void clearReferenceImage() {
        this.referenceImage = null;
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
        start(LocalDate.now());
    }

    /**
     * Inicia la ejecución con una fecha real de inicio, incluyendo fechas históricas.
     */
    public void start(LocalDate actualStartDate) {
        Objects.requireNonNull(actualStartDate, "Actual start date must not be null");

        if (status == ProductionStatus.IN_PROGRESS) {
            return;
        }

        if (status != ProductionStatus.PLANNED) {
            throw new ProductionDomainException(
                    "A production order can only be started from PLANNED status. Current status: " + status
            );
        }

        this.actualStartDate = actualStartDate;
        this.status = ProductionStatus.IN_PROGRESS;
    }

    /**
     * Completa la Orden de Producción.
     * <p>
     * Transición válida: {@link ProductionStatus#IN_PROGRESS} → {@link ProductionStatus#COMPLETED}.
     * Todas las operaciones deben estar completadas.
     */
    public void complete() {
        complete(LocalDate.now());
    }

    /**
     * Completa la Orden de Producción con una fecha real de cierre, incluyendo fechas históricas.
     */
    public void complete(LocalDate actualCompletionDate) {
        Objects.requireNonNull(actualCompletionDate, "Actual completion date must not be null");

        if (status == ProductionStatus.COMPLETED) {
            return;
        }

        if (status != ProductionStatus.IN_PROGRESS) {
            throw new ProductionDomainException(
                    "A production order can only be completed from IN_PROGRESS status. Current status: " + status
            );
        }

        if (actualStartDate != null && actualCompletionDate.isBefore(actualStartDate)) {
            throw new ProductionDomainException(
                    "Production completion date must not be before production start date"
            );
        }

        ensureAllOperationsCompleted();

        this.actualCompletionDate = actualCompletionDate;
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

    /**
     * Registra un consumo real de material durante la fabricación.
     * <p>
     * Solo permitido mientras el estado sea {@link ProductionStatus#IN_PROGRESS}.
     * {@link ProductionStatus#COMPLETED} se rechaza en V1 para evitar correcciones silenciosas
     * post-producción. No modifica Inventory ni genera movimientos de stock.
     */
    public ProductionMaterialConsumption registerMaterialConsumption(
            UUID inventoryItemId,
            BigDecimal quantity,
            ProductionMaterialUnitOfMeasure unitOfMeasure,
            String observation
    ) {
        ensureMaterialConsumptionAllowed();
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");
        Objects.requireNonNull(unitOfMeasure, "Unit of measure must not be null");

        ProductionMaterialConsumption consumption = ProductionMaterialConsumption.create(
                this.id,
                inventoryItemId,
                quantity,
                unitOfMeasure,
                observation
        );
        materialConsumptions.add(consumption);
        return consumption;
    }

    /**
     * Registra trabajo de mano de obra por producción.
     * <p>
     * Solo permitido mientras el estado sea {@link ProductionStatus#IN_PROGRESS}.
     * No crea movimientos financieros; el pago es un flujo explícito posterior.
     */
    public ProductionLaborWork registerLaborWork(
            UUID operatorEmployeeId,
            LocalDate workDate,
            String operation,
            BigDecimal quantity,
            String unitOfMeasure,
            BigDecimal unitRate,
            String observation
    ) {
        ensureLaborWorkAllowed();
        Objects.requireNonNull(operatorEmployeeId, "Operator employee id must not be null");
        Objects.requireNonNull(workDate, "Work date must not be null");

        ProductionLaborWork laborWork = ProductionLaborWork.create(
                this.id,
                operatorEmployeeId,
                workDate,
                operation,
                quantity,
                unitOfMeasure,
                unitRate,
                observation
        );
        laborWorks.add(laborWork);
        return laborWork;
    }

    /**
     * Registra un costo directo adicional (categoría OTROS u otras futuras).
     * <p>
     * Solo permitido mientras el estado sea {@link ProductionStatus#IN_PROGRESS}.
     * No crea el movimiento financiero; Application lo registra de forma idempotente.
     */
    public ProductionAdditionalCost registerAdditionalCost(
            ProductionDirectCostCategory category,
            String description,
            Money amount,
            LocalDate incurredDate
    ) {
        ensureAdditionalCostAllowed();
        Objects.requireNonNull(category, "Category must not be null");
        Objects.requireNonNull(incurredDate, "Incurred date must not be null");

        ProductionAdditionalCost additionalCost = ProductionAdditionalCost.create(
                this.id,
                category,
                description,
                amount,
                incurredDate
        );
        additionalCosts.add(additionalCost);
        return additionalCost;
    }

    public ProductionAdditionalCost requireAdditionalCost(UUID additionalCostId) {
        Objects.requireNonNull(additionalCostId, "Additional cost id must not be null");

        return additionalCosts.stream()
                .filter(cost -> cost.getId().equals(additionalCostId))
                .findFirst()
                .orElseThrow(() -> new ProductionDomainException(
                        "Production additional cost not found: " + additionalCostId
                ));
    }

    public ProductionLaborWork requireLaborWork(UUID laborWorkId) {
        Objects.requireNonNull(laborWorkId, "Labor work id must not be null");

        return laborWorks.stream()
                .filter(laborWork -> laborWork.getId().equals(laborWorkId))
                .findFirst()
                .orElseThrow(() -> new ProductionDomainException(
                        "Production labor work not found: " + laborWorkId
                ));
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

    public LocalDate getActualStartDate() {
        return actualStartDate;
    }

    public LocalDate getActualCompletionDate() {
        return actualCompletionDate;
    }

    public String getObservations() {
        return observations;
    }

    public List<ProductionItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<ProductionOperation> getOperations() {
        return Collections.unmodifiableList(operations);
    }

    public List<ProductionMaterialConsumption> getMaterialConsumptions() {
        return Collections.unmodifiableList(materialConsumptions);
    }

    public List<ProductionLaborWork> getLaborWorks() {
        return Collections.unmodifiableList(laborWorks);
    }

    public List<ProductionAdditionalCost> getAdditionalCosts() {
        return Collections.unmodifiableList(additionalCosts);
    }

    public ProductionReferenceImage getReferenceImage() {
        return referenceImage;
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

    private void ensureMaterialConsumptionAllowed() {
        ensureInProgressForProductionFacts("Material consumption");
    }

    private void ensureLaborWorkAllowed() {
        ensureInProgressForProductionFacts("Labor work");
    }

    private void ensureAdditionalCostAllowed() {
        ensureInProgressForProductionFacts("Additional cost");
    }

    private void ensureInProgressForProductionFacts(String factLabel) {
        if (status != ProductionStatus.IN_PROGRESS) {
            throw new ProductionDomainException(
                    factLabel + " can only be registered while status is IN_PROGRESS. Current status: "
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
