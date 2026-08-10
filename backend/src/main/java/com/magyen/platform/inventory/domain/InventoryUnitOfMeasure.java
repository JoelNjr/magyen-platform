package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Unidad de medida controlada de un material de inventario.
 * <p>
 * Persiste como texto. Los alias legados (por ejemplo {@code METRO}) se normalizan
 * a códigos canónicos cuando son reconocidos; valores desconocidos se preservan
 * en reconstitución para no romper datos existentes.
 */
public final class InventoryUnitOfMeasure {

    public static final InventoryUnitOfMeasure UNIT = canonical("UNIT");
    public static final InventoryUnitOfMeasure METER = canonical("METER");
    public static final InventoryUnitOfMeasure KILOGRAM = canonical("KILOGRAM");
    public static final InventoryUnitOfMeasure LITER = canonical("LITER");
    public static final InventoryUnitOfMeasure ROLL = canonical("ROLL");

    private static final Map<String, InventoryUnitOfMeasure> CANONICAL_BY_ALIAS = Map.ofEntries(
            Map.entry("UNIT", UNIT),
            Map.entry("UNIDAD", UNIT),
            Map.entry("UNIDADES", UNIT),
            Map.entry("U", UNIT),
            Map.entry("METER", METER),
            Map.entry("METERS", METER),
            Map.entry("METRO", METER),
            Map.entry("METROS", METER),
            Map.entry("M", METER),
            Map.entry("KILOGRAM", KILOGRAM),
            Map.entry("KILOGRAMS", KILOGRAM),
            Map.entry("KILOGRAMO", KILOGRAM),
            Map.entry("KILOGRAMOS", KILOGRAM),
            Map.entry("KG", KILOGRAM),
            Map.entry("LITER", LITER),
            Map.entry("LITERS", LITER),
            Map.entry("LITRO", LITER),
            Map.entry("LITROS", LITER),
            Map.entry("L", LITER),
            Map.entry("ROLL", ROLL),
            Map.entry("ROLLS", ROLL),
            Map.entry("ROLLO", ROLL),
            Map.entry("ROLLOS", ROLL)
    );

    private final String code;
    private final boolean canonical;

    private InventoryUnitOfMeasure(String code, boolean canonical) {
        this.code = code;
        this.canonical = canonical;
    }

    private static InventoryUnitOfMeasure canonical(String code) {
        return new InventoryUnitOfMeasure(code, true);
    }

    /**
     * Crea una unidad reconocida a partir de entrada de negocio.
     * <p>
     * Rechaza valores en blanco o no soportados.
     */
    public static InventoryUnitOfMeasure of(String value) {
        Objects.requireNonNull(value, "Unit of measure must not be null");
        if (value.isBlank()) {
            throw new InventoryDomainException("Unit of measure must not be blank");
        }

        return findCanonical(value).orElseThrow(() -> new InventoryDomainException(
                "Unsupported unit of measure: " + value.trim()
        ));
    }

    /**
     * Reconstruye desde persistencia sin destruir valores legados desconocidos.
     */
    public static InventoryUnitOfMeasure reconstitute(String value) {
        Objects.requireNonNull(value, "Unit of measure must not be null");
        if (value.isBlank()) {
            throw new InventoryDomainException("Unit of measure must not be blank");
        }

        return findCanonical(value).orElseGet(() -> new InventoryUnitOfMeasure(value.trim(), false));
    }

    public String getCode() {
        return code;
    }

    public boolean isCanonical() {
        return canonical;
    }

    public boolean isCompatibleWith(InventoryUnitOfMeasure other) {
        Objects.requireNonNull(other, "Unit of measure must not be null");
        return code.equalsIgnoreCase(other.code);
    }

    private static Optional<InventoryUnitOfMeasure> findCanonical(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(CANONICAL_BY_ALIAS.get(normalized));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        InventoryUnitOfMeasure that = (InventoryUnitOfMeasure) other;
        return code.equalsIgnoreCase(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code.toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return code;
    }
}
