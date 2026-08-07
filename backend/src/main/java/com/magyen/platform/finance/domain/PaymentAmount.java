package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Monto de un pago registrado en el módulo financiero.
 * <p>
 * Value Object inmutable propio de Finance. Distinto del Money compartido de la plataforma.
 */
public final class PaymentAmount {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final BigDecimal value;

    private PaymentAmount(BigDecimal value) {
        this.value = normalize(value);
    }

    /**
     * Crea un monto de pago válido.
     * <p>
     * El monto debe ser mayor que cero.
     */
    public static PaymentAmount of(BigDecimal value) {
        Objects.requireNonNull(value, "Payment amount must not be null");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceDomainException("Payment amount must be greater than zero");
        }
        return new PaymentAmount(value);
    }

    public BigDecimal getValue() {
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
        PaymentAmount that = (PaymentAmount) other;
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
