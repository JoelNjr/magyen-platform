package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Monto monetario de un movimiento del ledger financiero.
 * <p>
 * Value Object inmutable. Escala monetaria fija de 2 decimales.
 * Distinto de {@link PaymentAmount}, que modela pagos sobre una Orden.
 */
public final class FinancialAmount {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final BigDecimal value;

    private FinancialAmount(BigDecimal value) {
        this.value = normalize(value);
    }

    /**
     * Crea un monto financiero válido.
     * <p>
     * El monto debe ser mayor que cero.
     */
    public static FinancialAmount of(BigDecimal value) {
        Objects.requireNonNull(value, "Financial amount must not be null");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceDomainException("Financial amount must be greater than zero");
        }
        return new FinancialAmount(value);
    }

    public BigDecimal getValue() {
        return value;
    }

    public FinancialAmount add(FinancialAmount other) {
        Objects.requireNonNull(other, "Other financial amount must not be null");
        return new FinancialAmount(this.value.add(other.value));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        FinancialAmount that = (FinancialAmount) other;
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }

    private static BigDecimal normalize(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING_MODE);
    }
}
