package com.magyen.platform.inventory.domain;

import java.util.Objects;

/**
 * Identificador de negocio legible de un material de inventario.
 * <p>
 * Value Object inmutable. Distinto de la identidad técnica (UUID) del agregado.
 */
public final class MaterialCode {

    private final String value;

    private MaterialCode(String value) {
        this.value = value;
    }

    public static MaterialCode of(String value) {
        Objects.requireNonNull(value, "Material code must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Material code must not be blank");
        }
        return new MaterialCode(value.trim());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        MaterialCode that = (MaterialCode) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
