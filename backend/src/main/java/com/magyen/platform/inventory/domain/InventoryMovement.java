package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Hecho histórico inmutable de un movimiento de inventario.
 * <p>
 * Conserva la evidencia de un cambio de stock ya aplicado y, cuando aplica,
 * la valoración monetaria congelada y la referencia al proceso de origen.
 */
public final class InventoryMovement {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final UUID id;
    private final UUID inventoryItemId;
    private final InventoryMovementType movementType;
    private final BigDecimal quantity;
    private final InventoryUnitOfMeasure unitOfMeasure;
    private final LocalDateTime movementDate;
    private final String observation;
    private final BigDecimal resultingStock;
    private final BigDecimal unitCost;
    private final BigDecimal totalCost;
    private final InventoryMovementSourceType sourceType;
    private final UUID sourceId;

    private InventoryMovement(
            UUID id,
            UUID inventoryItemId,
            InventoryMovementType movementType,
            BigDecimal quantity,
            InventoryUnitOfMeasure unitOfMeasure,
            LocalDateTime movementDate,
            String observation,
            BigDecimal resultingStock,
            BigDecimal unitCost,
            BigDecimal totalCost,
            InventoryMovementSourceType sourceType,
            UUID sourceId
    ) {
        this.id = Objects.requireNonNull(id, "Movement id must not be null");
        this.inventoryItemId = Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");
        this.movementType = Objects.requireNonNull(movementType, "Movement type must not be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity must not be null");
        this.unitOfMeasure = Objects.requireNonNull(unitOfMeasure, "Unit of measure must not be null");
        this.movementDate = Objects.requireNonNull(movementDate, "Movement date must not be null");
        this.observation = normalizeObservation(observation);
        this.resultingStock = Objects.requireNonNull(resultingStock, "Resulting stock must not be null");
        this.unitCost = normalizeMoney(unitCost);
        this.totalCost = normalizeMoney(totalCost);
        this.sourceType = Objects.requireNonNull(sourceType, "Source type must not be null");
        this.sourceId = sourceId;

        if (resultingStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new InventoryDomainException("Resulting stock must not be negative");
        }

        if ((this.unitCost == null) != (this.totalCost == null)) {
            throw new InventoryDomainException(
                    "Movement unit cost and total cost must both be present or both be absent"
            );
        }

        requireValidSource(this.sourceType, this.sourceId);
    }

    /**
     * Crea un movimiento histórico asociado a un material.
     * <p>
     * Solo debe invocarse desde el agregado {@link InventoryItem} tras aplicar el efecto en stock.
     */
    static InventoryMovement record(
            UUID inventoryItemId,
            InventoryMovementType movementType,
            BigDecimal quantity,
            InventoryUnitOfMeasure unitOfMeasure,
            LocalDateTime movementDate,
            String observation,
            BigDecimal resultingStock,
            BigDecimal unitCost,
            InventoryMovementSourceType sourceType,
            UUID sourceId
    ) {
        BigDecimal snapshotUnitCost = normalizeMoney(unitCost);
        BigDecimal snapshotTotalCost = null;

        if (snapshotUnitCost != null) {
            snapshotTotalCost = quantity
                    .multiply(snapshotUnitCost)
                    .setScale(MONEY_SCALE, MONEY_ROUNDING);
        }

        InventoryMovementSourceType effectiveSourceType = sourceType == null
                ? InventoryMovementSourceType.MANUAL
                : sourceType;

        return new InventoryMovement(
                UUID.randomUUID(),
                inventoryItemId,
                movementType,
                quantity,
                unitOfMeasure,
                movementDate,
                observation,
                resultingStock,
                snapshotUnitCost,
                snapshotTotalCost,
                effectiveSourceType,
                sourceId
        );
    }

    /**
     * Reconstruye un movimiento desde persistencia. No aplica lógica de creación de negocio.
     */
    public static InventoryMovement reconstitute(
            UUID id,
            UUID inventoryItemId,
            InventoryMovementType movementType,
            BigDecimal quantity,
            InventoryUnitOfMeasure unitOfMeasure,
            LocalDateTime movementDate,
            String observation,
            BigDecimal resultingStock
    ) {
        return reconstitute(
                id,
                inventoryItemId,
                movementType,
                quantity,
                unitOfMeasure,
                movementDate,
                observation,
                resultingStock,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Reconstruye un movimiento desde persistencia incluyendo snapshot de costo.
     */
    public static InventoryMovement reconstitute(
            UUID id,
            UUID inventoryItemId,
            InventoryMovementType movementType,
            BigDecimal quantity,
            InventoryUnitOfMeasure unitOfMeasure,
            LocalDateTime movementDate,
            String observation,
            BigDecimal resultingStock,
            BigDecimal unitCost,
            BigDecimal totalCost
    ) {
        return reconstitute(
                id,
                inventoryItemId,
                movementType,
                quantity,
                unitOfMeasure,
                movementDate,
                observation,
                resultingStock,
                unitCost,
                totalCost,
                null,
                null
        );
    }

    /**
     * Reconstruye un movimiento desde persistencia incluyendo costo y origen.
     */
    public static InventoryMovement reconstitute(
            UUID id,
            UUID inventoryItemId,
            InventoryMovementType movementType,
            BigDecimal quantity,
            InventoryUnitOfMeasure unitOfMeasure,
            LocalDateTime movementDate,
            String observation,
            BigDecimal resultingStock,
            BigDecimal unitCost,
            BigDecimal totalCost,
            InventoryMovementSourceType sourceType,
            UUID sourceId
    ) {
        return new InventoryMovement(
                id,
                inventoryItemId,
                movementType,
                quantity,
                unitOfMeasure,
                movementDate,
                observation,
                resultingStock,
                unitCost,
                totalCost,
                sourceType == null ? InventoryMovementSourceType.MANUAL : sourceType,
                sourceId
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getInventoryItemId() {
        return inventoryItemId;
    }

    public InventoryMovementType getMovementType() {
        return movementType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public InventoryUnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public String getObservation() {
        return observation;
    }

    public BigDecimal getResultingStock() {
        return resultingStock;
    }

    /**
     * Costo unitario histórico del movimiento, o {@code null} si no había valoración configurada.
     */
    public BigDecimal getUnitCost() {
        return unitCost;
    }

    /**
     * Costo total histórico del movimiento ({@code quantity × unitCost}), o {@code null}.
     */
    public BigDecimal getTotalCost() {
        return totalCost;
    }

    /**
     * Tipo de origen del movimiento. Nunca {@code null} en dominio tras reconstitución.
     */
    public InventoryMovementSourceType getSourceType() {
        return sourceType;
    }

    /**
     * Identificador del hecho de negocio origen, o {@code null} para {@link InventoryMovementSourceType#MANUAL}.
     */
    public UUID getSourceId() {
        return sourceId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        InventoryMovement that = (InventoryMovement) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static void requireValidSource(InventoryMovementSourceType sourceType, UUID sourceId) {
        if (sourceType.requiresSourceId() && sourceId == null) {
            throw new InventoryDomainException(
                    "Source id is required for movement source type: " + sourceType.name()
            );
        }
    }

    private static String normalizeObservation(String observation) {
        if (observation == null || observation.isBlank()) {
            return null;
        }
        return observation.trim();
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
