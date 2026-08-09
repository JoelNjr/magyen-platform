package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Snapshot productivo de un ítem comercial a fabricar.
 * <p>
 * Pertenece al agregado {@link ProductionOrder}. Conserva la información
 * operativa relevante sin depender del estado posterior del OrderItem comercial.
 */
public class ProductionItem {

    private final UUID id;
    private final String productName;
    private final int quantity;
    private final ProductSpecification productSpecification;
    private final List<SizeBreakdown> sizeBreakdowns;

    ProductionItem(
            UUID id,
            String productName,
            int quantity,
            ProductSpecification productSpecification,
            List<SizeBreakdown> sizeBreakdowns
    ) {
        this.id = Objects.requireNonNull(id, "Production item id must not be null");
        this.productName = requireNonBlank(productName, "Product name must not be blank");
        if (quantity <= 0) {
            throw new ProductionDomainException("Production item quantity must be greater than zero");
        }
        this.quantity = quantity;
        this.productSpecification = productSpecification == null
                ? ProductSpecification.empty()
                : productSpecification;
        this.sizeBreakdowns = new ArrayList<>(
                sizeBreakdowns == null ? List.of() : sizeBreakdowns
        );
        validateSizeBreakdowns(this.sizeBreakdowns);
    }

    /**
     * Crea un snapshot productivo independiente del ítem comercial de origen.
     */
    public static ProductionItem create(
            String productName,
            int quantity,
            ProductSpecification productSpecification,
            List<SizeBreakdown> sizeBreakdowns
    ) {
        return new ProductionItem(
                UUID.randomUUID(),
                productName,
                quantity,
                productSpecification,
                sizeBreakdowns
        );
    }

    /**
     * Reconstruye un ítem productivo desde persistencia. No aplica lógica de creación de negocio.
     */
    public static ProductionItem reconstitute(
            UUID id,
            String productName,
            int quantity,
            ProductSpecification productSpecification,
            List<SizeBreakdown> sizeBreakdowns
    ) {
        return new ProductionItem(
                id,
                productName,
                quantity,
                productSpecification,
                sizeBreakdowns
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
        ProductionItem that = (ProductionItem) other;
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
                throw new ProductionDomainException(
                        "Duplicate size is not allowed for the same production item: " + breakdown.getSize()
                );
            }

            totalSizeQuantity += breakdown.getQuantity();
        }

        if (totalSizeQuantity > quantity) {
            throw new ProductionDomainException(
                    "Total size quantity must not exceed production item quantity. "
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
