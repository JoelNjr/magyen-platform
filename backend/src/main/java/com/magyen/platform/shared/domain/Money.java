package com.magyen.platform.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Representa un valor monetario con precisión decimal.
 * <p>
 * Value Object compartido por toda la plataforma. Inmutable y sin dependencias de framework.
 */
public final class Money {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = normalize(amount);
    }

    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }
        return new Money(amount);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "Other money must not be null");
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must not be negative");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Money money = (Money) other;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }

    private static BigDecimal normalize(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING_MODE);
    }
}
