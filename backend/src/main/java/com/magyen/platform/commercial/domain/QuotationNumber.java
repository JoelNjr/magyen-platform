package com.magyen.platform.commercial.domain;

import java.util.Objects;

/**
 * Identificador comercial consecutivo de una Cotización.
 * <p>
 * Value Object inmutable numérico. Distinto de la identidad técnica (UUID) del agregado.
 */
public final class QuotationNumber {

    private final long value;

    private QuotationNumber(long value) {
        this.value = value;
    }

    public static QuotationNumber of(long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Quotation number must be positive");
        }
        return new QuotationNumber(value);
    }

    public long getValue() {
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
        QuotationNumber that = (QuotationNumber) other;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
