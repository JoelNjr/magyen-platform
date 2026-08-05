package com.magyen.platform.commercial.domain;

import com.magyen.platform.shared.domain.Money;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa un producto comprometido por Magyen dentro de una Orden.
 * <p>
 * Solo puede ser creado por el agregado {@link Order}.
 */
public class OrderItem {

    private final UUID id;
    private final String productName;
    private final int quantity;
    private final String fabric;
    private final String color;
    private final Money unitPrice;
    private final Money subtotal;

    OrderItem(
            UUID id,
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice
    ) {
        this.id = Objects.requireNonNull(id, "Item id must not be null");
        this.productName = requireNonBlank(productName, "Product name must not be blank");
        this.quantity = quantity;
        this.fabric = requireNonBlank(fabric, "Fabric must not be blank");
        this.color = requireNonBlank(color, "Color must not be blank");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price must not be null");
        this.subtotal = unitPrice.multiply(quantity);
    }

    static OrderItem create(
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice
    ) {
        return new OrderItem(UUID.randomUUID(), productName, quantity, fabric, color, unitPrice);
    }

    /**
     * Reconstruye un ítem desde persistencia. No aplica lógica de creación de negocio.
     */
    public static OrderItem reconstitute(
            UUID id,
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice
    ) {
        return new OrderItem(id, productName, quantity, fabric, color, unitPrice);
    }

    public UUID getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getFabric() {
        return fabric;
    }

    public String getColor() {
        return color;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        OrderItem that = (OrderItem) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
