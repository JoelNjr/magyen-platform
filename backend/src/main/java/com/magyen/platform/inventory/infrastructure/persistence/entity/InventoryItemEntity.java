package com.magyen.platform.inventory.infrastructure.persistence.entity;

import com.magyen.platform.inventory.domain.InventoryItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Modelo relacional del agregado {@link com.magyen.platform.inventory.domain.InventoryItem}.
 */
@Entity
@Table(name = "inventory_items")
public class InventoryItemEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "material_code", nullable = false, unique = true, length = 100)
    private String materialCode;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "category", nullable = false, length = 255)
    private String category;

    @Column(name = "unit_of_measure", nullable = false, length = 50)
    private String unitOfMeasure;

    @Column(name = "stock", nullable = false, precision = 19, scale = 4)
    private BigDecimal stock;

    @Column(name = "minimum_stock", nullable = false, precision = 19, scale = 4)
    private BigDecimal minimumStock;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InventoryItemStatus status;

    public InventoryItemEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(BigDecimal minimumStock) {
        this.minimumStock = minimumStock;
    }

    public InventoryItemStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryItemStatus status) {
        this.status = status;
    }
}
