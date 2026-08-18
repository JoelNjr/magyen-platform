package com.magyen.platform.commercial.domain;

import com.magyen.platform.shared.domain.Money;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa un producto solicitado por el cliente dentro de una cotización.
 * <p>
 * {@code color} es el color de tela / base, no el diseño sublimado completo.
 * En productos sublimados el valor de negocio es {@code Blanco}.
 * <p>
 * Solo puede ser creado por el agregado {@link Quotation}.
 */
public class QuotationItem {

    private final UUID id;
    private final String productName;
    private final int quantity;
    private final String fabric;
    private final String secondaryFabric;
    private final String color;
    private final Money unitPrice;
    private final Money subtotal;
    private final ProductSpecification productSpecification;

    QuotationItem(
            UUID id,
            String productName,
            int quantity,
            String fabric,
            String secondaryFabric,
            String color,
            Money unitPrice,
            ProductSpecification productSpecification
    ) {
        this.id = Objects.requireNonNull(id, "Item id must not be null");
        this.productName = requireNonBlank(productName, "Product name must not be blank");
        this.quantity = quantity;
        this.fabric = requireNonBlank(fabric, "Fabric must not be blank");
        this.secondaryFabric = blankToNull(secondaryFabric);
        this.color = requireNonBlank(color, "Color must not be blank");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price must not be null");
        this.subtotal = unitPrice.multiply(quantity);
        this.productSpecification = productSpecification == null
                ? ProductSpecification.empty()
                : productSpecification;
    }

    static QuotationItem create(
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice
    ) {
        return create(productName, quantity, fabric, color, unitPrice, ProductSpecification.empty());
    }

    static QuotationItem create(
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice,
            ProductSpecification productSpecification
    ) {
        return create(productName, quantity, fabric, null, color, unitPrice, productSpecification);
    }

    static QuotationItem create(
            String productName,
            int quantity,
            String fabric,
            String secondaryFabric,
            String color,
            Money unitPrice,
            ProductSpecification productSpecification
    ) {
        return new QuotationItem(
                UUID.randomUUID(),
                productName,
                quantity,
                fabric,
                secondaryFabric,
                color,
                unitPrice,
                productSpecification
        );
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
            Money unitPrice,
            ProductSpecification productSpecification
    ) {
        return reconstitute(
                id,
                productName,
                quantity,
                fabric,
                null,
                color,
                unitPrice,
                productSpecification
        );
    }

    public static QuotationItem reconstitute(
            UUID id,
            String productName,
            int quantity,
            String fabric,
            String secondaryFabric,
            String color,
            Money unitPrice,
            ProductSpecification productSpecification
    ) {
        return new QuotationItem(
                id,
                productName,
                quantity,
                fabric,
                secondaryFabric,
                color,
                unitPrice,
                productSpecification
        );
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

    public String getSecondaryFabric() {
        return secondaryFabric;
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

    public ProductSpecification getProductSpecification() {
        return productSpecification;
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

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
