package com.magyen.platform.production.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

/**
 * Modelo relacional de la distribución de tallas de un {@link com.magyen.platform.production.domain.ProductionItem}.
 */
@Entity
@Table(
        name = "production_item_sizes",
        uniqueConstraints = @UniqueConstraint(
                name = "production_item_sizes_production_item_id_size_key",
                columnNames = {"production_item_id", "size"}
        )
)
public class ProductionItemSizeEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_item_id", nullable = false)
    private ProductionItemEntity productionItem;

    @Column(name = "size", nullable = false, length = 50)
    private String size;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public ProductionItemSizeEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProductionItemEntity getProductionItem() {
        return productionItem;
    }

    public void setProductionItem(ProductionItemEntity productionItem) {
        this.productionItem = productionItem;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
