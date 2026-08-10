package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Clasificación tipada de material de inventario.
 * <p>
 * Es independiente de {@code category} (texto libre legado) para no reinterpretar
 * filas históricas.
 */
public enum InventoryMaterialType {
    FABRIC,
    PAPER,
    INK,
    THREAD,
    DTF,
    OTHER;

    public static InventoryMaterialType of(String value) {
        Objects.requireNonNull(value, "Material type must not be null");
        if (value.isBlank()) {
            throw new InventoryDomainException("Material type must not be blank");
        }

        try {
            return InventoryMaterialType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InventoryDomainException("Unsupported material type: " + value.trim());
        }
    }

    /**
     * Reconstitución segura: valores desconocidos o nulos se mapean a {@link #OTHER}.
     */
    public static InventoryMaterialType reconstitute(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }

        try {
            return InventoryMaterialType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return OTHER;
        }
    }
}
