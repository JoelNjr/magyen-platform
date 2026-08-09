package com.magyen.platform.commercial.infrastructure.persistence.entity;

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
 * Modelo relacional de la distribución de tallas de un {@link com.magyen.platform.commercial.domain.OrderItem}.
 */
@Entity
@Table(
        name = "order_item_sizes",
        uniqueConstraints = @UniqueConstraint(
                name = "order_item_sizes_order_item_id_size_key",
                columnNames = {"order_item_id", "size"}
        )
)
public class OrderItemSizeEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItemEntity orderItem;

    @Column(name = "size", nullable = false, length = 50)
    private String size;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public OrderItemSizeEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OrderItemEntity getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItemEntity orderItem) {
        this.orderItem = orderItem;
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
