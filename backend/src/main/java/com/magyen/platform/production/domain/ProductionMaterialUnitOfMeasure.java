package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Unidad de medida en la que Production registra un consumo de material.
 * <p>
 * Comparte el vocabulario canónico de Inventory (UNIT, METER, KILOGRAM, LITER, ROLL)
 * sin depender de clases del módulo Inventory.
 */
public enum ProductionMaterialUnitOfMeasure {

    UNIT,
    METER,
    KILOGRAM,
    LITER,
    ROLL;

    public static ProductionMaterialUnitOfMeasure of(String value) {
        Objects.requireNonNull(value, "Unit of measure must not be null");
        if (value.isBlank()) {
            throw new ProductionDomainException("Unit of measure must not be blank");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "UNIT", "UNIDAD", "UNIDADES", "U" -> UNIT;
            case "METER", "METERS", "METRO", "METROS", "M" -> METER;
            case "KILOGRAM", "KILOGRAMS", "KILOGRAMO", "KILOGRAMOS", "KG" -> KILOGRAM;
            case "LITER", "LITERS", "LITRO", "LITROS", "L" -> LITER;
            case "ROLL", "ROLLS", "ROLLO", "ROLLOS" -> ROLL;
            default -> throw new ProductionDomainException("Unsupported unit of measure: " + value.trim());
        };
    }
}
