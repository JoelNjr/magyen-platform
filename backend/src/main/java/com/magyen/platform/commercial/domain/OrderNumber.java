package com.magyen.platform.commercial.domain;

import java.util.Objects;

/**
 * Identificador comercial legible de una Orden.
 * <p>
 * Value Object inmutable. Distinto de la identidad técnica (UUID) del agregado.
 */
public final class OrderNumber {

    private final String value;

    private OrderNumber(String value) {
        this.value = value;
    }

    public static OrderNumber of(String value) {
        Objects.requireNonNull(value, "Order number must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Order number must not be blank");
        }
        return new OrderNumber(value.trim());
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
        OrderNumber that = (OrderNumber) other;
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
