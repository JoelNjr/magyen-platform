package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.shared.domain.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Representa un producto comprometido por Magyen dentro de una Orden.
 * <p>
 * {@code color} es el color de tela / base, no el diseño sublimado completo.
 * En productos sublimados el valor de negocio es {@code Blanco}.
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
    private ProductSpecification productSpecification;
    private final List<SizeBreakdown> sizeBreakdowns;

    OrderItem(
            UUID id,
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice,
            ProductSpecification productSpecification,
            List<SizeBreakdown> sizeBreakdowns
    ) {
        this.id = Objects.requireNonNull(id, "Item id must not be null");
        this.productName = requireNonBlank(productName, "Product name must not be blank");
        this.quantity = quantity;
        this.fabric = requireNonBlank(fabric, "Fabric must not be blank");
        this.color = requireNonBlank(color, "Color must not be blank");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price must not be null");
        this.subtotal = unitPrice.multiply(quantity);
        this.productSpecification = productSpecification == null
                ? ProductSpecification.empty()
                : productSpecification;
        this.sizeBreakdowns = new ArrayList<>(
                sizeBreakdowns == null ? List.of() : sizeBreakdowns
        );
        validateSizeBreakdowns(this.sizeBreakdowns);
    }

    static OrderItem create(
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice
    ) {
        return create(
                productName,
                quantity,
                fabric,
                color,
                unitPrice,
                ProductSpecification.empty(),
                List.of()
        );
    }

    static OrderItem create(
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice,
            ProductSpecification productSpecification
    ) {
        return create(
                productName,
                quantity,
                fabric,
                color,
                unitPrice,
                productSpecification,
                List.of()
        );
    }

    static OrderItem create(
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice,
            ProductSpecification productSpecification,
            List<SizeBreakdown> sizeBreakdowns
    ) {
        return new OrderItem(
                UUID.randomUUID(),
                productName,
                quantity,
                CommercialFabric.canonicalize(fabric),
                color,
                unitPrice,
                productSpecification,
                sizeBreakdowns
        );
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
            Money unitPrice,
            ProductSpecification productSpecification,
            List<SizeBreakdown> sizeBreakdowns
    ) {
        return new OrderItem(
                id,
                productName,
                quantity,
                fabric,
                color,
                unitPrice,
                productSpecification,
                sizeBreakdowns
        );
    }

    /**
     * Asigna o reemplaza la especificación comercial del producto.
     */
    public void assignProductSpecification(ProductSpecification productSpecification) {
        this.productSpecification = productSpecification == null
                ? ProductSpecification.empty()
                : productSpecification;
    }

    /**
     * Reemplaza la distribución de tallas del ítem.
     * <p>
     * La suma de cantidades por talla no puede exceder {@link #quantity}.
     * No modifica la cantidad comercial comprometida del ítem.
     */
    public void replaceSizeBreakdowns(List<SizeBreakdown> sizeBreakdowns) {
        Objects.requireNonNull(sizeBreakdowns, "Size breakdowns must not be null");
        validateSizeBreakdowns(sizeBreakdowns);
        this.sizeBreakdowns.clear();
        this.sizeBreakdowns.addAll(sizeBreakdowns);
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

    public ProductSpecification getProductSpecification() {
        return productSpecification;
    }

    public List<SizeBreakdown> getSizeBreakdowns() {
        return Collections.unmodifiableList(sizeBreakdowns);
    }

    public int getAssignedSizeQuantity() {
        return sizeBreakdowns.stream()
                .mapToInt(SizeBreakdown::getQuantity)
                .sum();
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

    private void validateSizeBreakdowns(List<SizeBreakdown> breakdowns) {
        Set<String> sizes = new HashSet<>();
        int totalSizeQuantity = 0;

        for (SizeBreakdown breakdown : breakdowns) {
            Objects.requireNonNull(breakdown, "Size breakdown must not be null");

            if (!sizes.add(breakdown.getSize())) {
                throw new OrderDomainException(
                        "Duplicate size is not allowed for the same order item: " + breakdown.getSize()
                );
            }

            totalSizeQuantity += breakdown.getQuantity();
        }

        if (totalSizeQuantity > quantity) {
            throw new OrderDomainException(
                    "Total size quantity must not exceed order item quantity. "
                            + "Assigned: " + totalSizeQuantity + ", item quantity: " + quantity
            );
        }
    }

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
