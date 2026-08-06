package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del módulo de inventario.
 * <p>
 * Representa un material físico almacenado y mantiene la consistencia de su stock.
 */
public class InventoryItem {

    private final UUID id;
    private final MaterialCode materialCode;
    private final String name;
    private final String category;
    private final String unitOfMeasure;
    private BigDecimal stock;
    private final BigDecimal minimumStock;
    private InventoryItemStatus status;

    private InventoryItem(
            UUID id,
            MaterialCode materialCode,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            InventoryItemStatus status
    ) {
        this.id = Objects.requireNonNull(id, "Inventory item id must not be null");
        this.materialCode = Objects.requireNonNull(materialCode, "Material code must not be null");
        this.name = requireNonBlank(name, "Name must not be blank");
        this.category = requireNonBlank(category, "Category must not be blank");
        this.unitOfMeasure = requireNonBlank(unitOfMeasure, "Unit of measure must not be blank");
        this.stock = requireNonNegativeStock(stock, "Stock must not be negative");
        this.minimumStock = requireNonNegativeStock(minimumStock, "Minimum stock must not be negative");
        this.status = Objects.requireNonNull(status, "Status must not be null");
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
        return new InventoryItem(
                UUID.randomUUID(),
                materialCode,
                name,
                category,
                unitOfMeasure,
                stock,
                minimumStock,
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
        return new InventoryItem(
                id,
                materialCode,
                name,
                category,
                unitOfMeasure,
                stock,
                minimumStock,
                status
        );
    }

    /**
     * Incrementa el stock disponible del material.
     * <p>
     * La cantidad debe ser mayor que cero.
     */
    public void increaseStock(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantity must not be null");
        requirePositiveQuantity(quantity);

        this.stock = this.stock.add(quantity);
    }

    /**
     * Disminuye el stock disponible del material.
     * <p>
     * La cantidad debe ser mayor que cero y no puede superar el stock actual.
     */
    public void decreaseStock(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantity must not be null");
        requirePositiveQuantity(quantity);

        if (this.stock.compareTo(quantity) < 0) {
            throw new InventoryDomainException(
                    "Cannot decrease stock below zero. Available stock: " + this.stock
                            + ", requested quantity: " + quantity
            );
        }

        this.stock = this.stock.subtract(quantity);
    }

    /**
     * Activa el material de inventario.
     * <p>
     * Transición: cualquier estado → {@link InventoryItemStatus#ACTIVE}.
     */
    public void activate() {
        if (status == InventoryItemStatus.ACTIVE) {
            return;
        }
        this.status = InventoryItemStatus.ACTIVE;
    }

    /**
     * Desactiva el material de inventario.
     * <p>
     * Transición: cualquier estado → {@link InventoryItemStatus#INACTIVE}.
     */
    public void deactivate() {
        if (status == InventoryItemStatus.INACTIVE) {
            return;
        }
        this.status = InventoryItemStatus.INACTIVE;
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

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
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

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
