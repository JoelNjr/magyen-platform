package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del módulo de inventario.
 * <p>
 * Representa un material físico almacenado y mantiene la consistencia de su stock.
 * Los cambios de stock se producen mediante movimientos históricos.
 * {@code unitCost} es la valoración configurada actual; los movimientos congelan su propio snapshot.
 */
public class InventoryItem {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final UUID id;
    private final MaterialCode materialCode;
    private final String name;
    private final String category;
    private final String description;
    private final InventoryMaterialType materialType;
    private final String paperRollNumber;
    private final InventoryUnitOfMeasure unitOfMeasure;
    private BigDecimal stock;
    private BigDecimal minimumStock;
    private BigDecimal unitCost;
    private InventoryItemStatus status;

    private InventoryItem(
            UUID id,
            MaterialCode materialCode,
            String name,
            String category,
            String description,
            InventoryMaterialType materialType,
            String paperRollNumber,
            InventoryUnitOfMeasure unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            BigDecimal unitCost,
            InventoryItemStatus status
    ) {
        this.id = Objects.requireNonNull(id, "Inventory item id must not be null");
        this.materialCode = Objects.requireNonNull(materialCode, "Material code must not be null");
        this.name = requireNonBlank(name, "Name must not be blank");
        this.category = requireNonBlank(category, "Category must not be blank");
        this.description = normalizeDescription(description);
        this.materialType = Objects.requireNonNull(materialType, "Material type must not be null");
        this.paperRollNumber = normalizePaperRollNumber(paperRollNumber);
        this.unitOfMeasure = Objects.requireNonNull(unitOfMeasure, "Unit of measure must not be null");
        this.stock = requireNonNegativeStock(stock, "Stock must not be negative");
        this.minimumStock = requireValidMinimumStock(minimumStock);
        this.unitCost = requireValidUnitCost(unitCost);
        this.status = Objects.requireNonNull(status, "Status must not be null");
        validatePlotterPaperRollConsistency();
    }

    /**
     * Crea un material de inventario en estado inicial válido {@link InventoryItemStatus#ACTIVE}.
     */
    public static InventoryItem create(
            MaterialCode materialCode,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock
    ) {
        return create(materialCode, name, category, unitOfMeasure, stock, minimumStock, null, null);
    }

    /**
     * Crea un material de inventario con descripción opcional.
     */
    public static InventoryItem create(
            MaterialCode materialCode,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            String description
    ) {
        return create(materialCode, name, category, unitOfMeasure, stock, minimumStock, description, null);
    }

    /**
     * Crea un material de inventario con descripción y costo unitario opcionales.
     */
    public static InventoryItem create(
            MaterialCode materialCode,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            String description,
            BigDecimal unitCost
    ) {
        return create(
                materialCode,
                name,
                category,
                unitOfMeasure,
                stock,
                minimumStock,
                description,
                unitCost,
                InventoryMaterialType.OTHER,
                null
        );
    }

    /**
     * Crea un material de inventario con clasificación tipada y número de rollo opcional.
     */
    public static InventoryItem create(
            MaterialCode materialCode,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            String description,
            BigDecimal unitCost,
            InventoryMaterialType materialType,
            String paperRollNumber
    ) {
        return new InventoryItem(
                UUID.randomUUID(),
                materialCode,
                name,
                category,
                description,
                materialType == null ? InventoryMaterialType.OTHER : materialType,
                paperRollNumber,
                InventoryUnitOfMeasure.of(unitOfMeasure),
                stock,
                minimumStock,
                unitCost,
                InventoryItemStatus.ACTIVE
        );
    }

    /**
     * Reconstruye un material de inventario desde persistencia. No aplica lógica de creación de negocio.
     */
    public static InventoryItem reconstitute(
            UUID id,
            MaterialCode materialCode,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            InventoryItemStatus status
    ) {
        return reconstitute(
                id,
                materialCode,
                name,
                category,
                unitOfMeasure,
                stock,
                minimumStock,
                status,
                null,
                null
        );
    }

    /**
     * Reconstruye un material de inventario desde persistencia incluyendo descripción.
     */
    public static InventoryItem reconstitute(
            UUID id,
            MaterialCode materialCode,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            InventoryItemStatus status,
            String description
    ) {
        return reconstitute(
                id,
                materialCode,
                name,
                category,
                unitOfMeasure,
                stock,
                minimumStock,
                status,
                description,
                null
        );
    }

    /**
     * Reconstruye un material de inventario desde persistencia incluyendo descripción y costo.
     */
    public static InventoryItem reconstitute(
            UUID id,
            MaterialCode materialCode,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            InventoryItemStatus status,
            String description,
            BigDecimal unitCost
    ) {
        return reconstitute(
                id,
                materialCode,
                name,
                category,
                unitOfMeasure,
                stock,
                minimumStock,
                status,
                description,
                unitCost,
                InventoryMaterialType.OTHER,
                null
        );
    }

    /**
     * Reconstruye un material de inventario incluyendo tipo y número de rollo.
     */
    public static InventoryItem reconstitute(
            UUID id,
            MaterialCode materialCode,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            InventoryItemStatus status,
            String description,
            BigDecimal unitCost,
            InventoryMaterialType materialType,
            String paperRollNumber
    ) {
        return new InventoryItem(
                id,
                materialCode,
                name,
                category,
                description,
                materialType == null ? InventoryMaterialType.OTHER : materialType,
                paperRollNumber,
                InventoryUnitOfMeasure.reconstitute(unitOfMeasure),
                stock,
                minimumStock,
                unitCost,
                status
        );
    }

    /**
     * Registra un movimiento manual y actualiza el stock de forma determinística.
     * <p>
     * Si existe {@code unitCost} configurado, el movimiento congela
     * {@code unitCost} y {@code totalCost = quantity × unitCost}.
     * Si no hay costo configurado, el movimiento se registra sin valoración.
     * El origen queda como {@link InventoryMovementSourceType#MANUAL}.
     */
    public InventoryMovement registerMovement(
            InventoryMovementType movementType,
            BigDecimal quantity,
            InventoryUnitOfMeasure movementUnitOfMeasure,
            String observation,
            LocalDateTime movementDate
    ) {
        return registerMovement(
                movementType,
                quantity,
                movementUnitOfMeasure,
                observation,
                movementDate,
                InventoryMovementSourceType.MANUAL,
                null
        );
    }

    /**
     * Registra un movimiento con origen auditable y actualiza el stock.
     * <p>
     * {@code sourceType}/{@code sourceId} no afectan el cálculo de costo.
     * Reglas: MANUAL admite {@code sourceId} nulo; PRODUCTION y PLOTTER lo exigen.
     */
    public InventoryMovement registerMovement(
            InventoryMovementType movementType,
            BigDecimal quantity,
            InventoryUnitOfMeasure movementUnitOfMeasure,
            String observation,
            LocalDateTime movementDate,
            InventoryMovementSourceType sourceType,
            UUID sourceId
    ) {
        Objects.requireNonNull(movementType, "Movement type must not be null");
        Objects.requireNonNull(quantity, "Quantity must not be null");
        Objects.requireNonNull(movementDate, "Movement date must not be null");

        InventoryUnitOfMeasure effectiveUnit = movementUnitOfMeasure == null
                ? this.unitOfMeasure
                : movementUnitOfMeasure;

        if (!this.unitOfMeasure.isCompatibleWith(effectiveUnit)) {
            throw new InventoryDomainException(
                    "Incompatible unit of measure. Expected: " + this.unitOfMeasure.getCode()
                            + ", provided: " + effectiveUnit.getCode()
            );
        }

        BigDecimal nextStock = switch (movementType) {
            case IN -> applyIn(quantity);
            case OUT -> applyOut(quantity);
            case ADJUSTMENT -> applyAdjustment(quantity);
        };

        this.stock = nextStock;

        return InventoryMovement.record(
                this.id,
                movementType,
                quantity,
                this.unitOfMeasure,
                movementDate,
                observation,
                this.stock,
                this.unitCost,
                sourceType,
                sourceId
        );
    }

    /**
     * Incrementa el stock disponible del material mediante un movimiento {@link InventoryMovementType#IN}.
     */
    public InventoryMovement increaseStock(BigDecimal quantity) {
        return registerMovement(
                InventoryMovementType.IN,
                quantity,
                this.unitOfMeasure,
                null,
                LocalDateTime.now(),
                InventoryMovementSourceType.MANUAL,
                null
        );
    }

    /**
     * Disminuye el stock disponible del material mediante un movimiento {@link InventoryMovementType#OUT}.
     */
    public InventoryMovement decreaseStock(BigDecimal quantity) {
        return registerMovement(
                InventoryMovementType.OUT,
                quantity,
                this.unitOfMeasure,
                null,
                LocalDateTime.now(),
                InventoryMovementSourceType.MANUAL,
                null
        );
    }

    public void activate() {
        if (status == InventoryItemStatus.ACTIVE) {
            return;
        }
        this.status = InventoryItemStatus.ACTIVE;
    }

    public void deactivate() {
        if (status == InventoryItemStatus.INACTIVE) {
            return;
        }
        this.status = InventoryItemStatus.INACTIVE;
    }

    /**
     * Configura el umbral mínimo de stock.
     * <p>
     * {@code null} deshabilita el monitoreo de stock bajo. Cero es un umbral válido.
     * No modifica el stock actual ni genera movimientos.
     */
    public void updateMinimumStock(BigDecimal minimumStock) {
        this.minimumStock = requireValidMinimumStock(minimumStock);
    }

    /**
     * Configura el costo unitario actual del material.
     * <p>
     * {@code null} indica que la valoración no está configurada.
     * Cero es un costo válido (material sin costo).
     * No modifica stock ni genera movimientos.
     */
    public void updateUnitCost(BigDecimal unitCost) {
        this.unitCost = requireValidUnitCost(unitCost);
    }

    /**
     * Indica si el material está en stock bajo.
     */
    public boolean isLowStock() {
        if (minimumStock == null) {
            return false;
        }
        return stock.compareTo(minimumStock) <= 0;
    }

    public UUID getId() {
        return id;
    }

    public MaterialCode getMaterialCode() {
        return materialCode;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public InventoryMaterialType getMaterialType() {
        return materialType;
    }

    public String getPaperRollNumber() {
        return paperRollNumber;
    }

    /**
     * Indica si el material es un rollo de papel elegible para Plotter.
     */
    public boolean isPlotterPaperRoll() {
        return materialType == InventoryMaterialType.PAPER
                && paperRollNumber != null
                && unitOfMeasure.isCompatibleWith(InventoryUnitOfMeasure.METER);
    }

    public String getDescription() {
        return description;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure.getCode();
    }

    public InventoryUnitOfMeasure getUnitOfMeasureValue() {
        return unitOfMeasure;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
    }

    /**
     * Costo unitario configurado actualmente, o {@code null} si no hay valoración.
     */
    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public InventoryItemStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        InventoryItem that = (InventoryItem) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private BigDecimal applyIn(BigDecimal quantity) {
        requirePositiveQuantity(quantity);
        return this.stock.add(quantity);
    }

    private BigDecimal applyOut(BigDecimal quantity) {
        requirePositiveQuantity(quantity);

        if (this.stock.compareTo(quantity) < 0) {
            throw new InventoryDomainException(
                    "Cannot decrease stock below zero. Available stock: " + this.stock
                            + ", requested quantity: " + quantity
            );
        }

        return this.stock.subtract(quantity);
    }

    private BigDecimal applyAdjustment(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            throw new InventoryDomainException("Adjustment quantity must not be zero");
        }

        BigDecimal nextStock = this.stock.add(quantity);
        if (nextStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new InventoryDomainException(
                    "Adjustment would produce negative stock. Available stock: " + this.stock
                            + ", adjustment quantity: " + quantity
            );
        }

        return nextStock;
    }

    private static void requirePositiveQuantity(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InventoryDomainException("Quantity must be greater than zero");
        }
    }

    private static BigDecimal requireNonNegativeStock(BigDecimal value, String message) {
        Objects.requireNonNull(value, message);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InventoryDomainException(message);
        }
        return value;
    }

    private static BigDecimal requireValidMinimumStock(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InventoryDomainException("Minimum stock must not be negative");
        }
        return value;
    }

    private static BigDecimal requireValidUnitCost(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InventoryDomainException("Unit cost must not be negative");
        }
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private static String normalizePaperRollNumber(String paperRollNumber) {
        if (paperRollNumber == null || paperRollNumber.isBlank()) {
            return null;
        }
        return paperRollNumber.trim();
    }

    private void validatePlotterPaperRollConsistency() {
        if (paperRollNumber == null) {
            return;
        }
        if (materialType != InventoryMaterialType.PAPER) {
            throw new InventoryDomainException(
                    "Paper roll number can only be assigned to PAPER materials"
            );
        }
        if (!unitOfMeasure.isCompatibleWith(InventoryUnitOfMeasure.METER)) {
            throw new InventoryDomainException(
                    "Plotter paper rolls must use METER as unit of measure"
            );
        }
    }
}
