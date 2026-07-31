package com.magyen.platform.commercial.domain;

import com.magyen.platform.shared.domain.Money;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa un producto solicitado por el cliente dentro de una cotización.
 * <p>
 * Solo puede ser creado por el agregado {@link Quotation}.
 */
public class QuotationItem {

    private final UUID id;
    private final String productName;
    private final int quantity;
    private final String fabric;
    private final String color;
    private final Money unitPrice;
    private final Money subtotal;

    QuotationItem(
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

    static QuotationItem create(
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice
    ) {
        return new QuotationItem(UUID.randomUUID(), productName, quantity, fabric, color, unitPrice);
    }

    /**
     * Reconstruye un ítem desde persistencia. No aplica lógica de creación de negocio.
     */
    public static QuotationItem reconstitute(
            UUID id,
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice
    ) {
        return new QuotationItem(id, productName, quantity, fabric, color, unitPrice);
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
        QuotationItem that = (QuotationItem) other;
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
