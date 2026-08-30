package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Categoría de costo directo adicional atribuible a una Orden de Producción.
 * <p>
 * Distinta de material (consumo de inventario) y de mano de obra.
 * {@link #OTHER} exige descripción libre en el costo.
 */
public enum ProductionDirectCostCategory {

    OTHER;

    public static ProductionDirectCostCategory of(String value) {
        Objects.requireNonNull(value, "Direct cost category must not be null");
        if (value.isBlank()) {
            throw new ProductionDomainException("Direct cost category must not be blank");
        }
        try {
            return ProductionDirectCostCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ProductionDomainException("Invalid direct cost category: " + value);
        }
    }
}
