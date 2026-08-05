package com.magyen.platform.commercial.domain;

import com.magyen.platform.shared.domain.Money;

import java.util.Objects;

/**
 * Estado ligero de pago del compromiso comercial de una Orden.
 * <p>
 * Value Object inmutable. No representa el ledger financiero ni movimientos de dinero.
 */
public final class PaymentSummary {

    private final boolean advanceAcknowledged;
    private final boolean finalPaymentAcknowledged;
    private final Money committedTotal;
    private final Money remainingBalance;

    private PaymentSummary(
            boolean advanceAcknowledged,
            boolean finalPaymentAcknowledged,
            Money committedTotal,
            Money remainingBalance
    ) {
        this.advanceAcknowledged = advanceAcknowledged;
        this.finalPaymentAcknowledged = finalPaymentAcknowledged;
        this.committedTotal = committedTotal;
        this.remainingBalance = remainingBalance;
    }

    /**
     * Crea el resumen de pago de una Orden.
     * <p>
     * El saldo restante se deriva del estado de reconocimiento de pagos:
     * si el pago final está reconocido el saldo es cero; en caso contrario es el total comprometido.
     */
    public static PaymentSummary of(
            boolean advanceAcknowledged,
            boolean finalPaymentAcknowledged,
            Money committedTotal
    ) {
        Objects.requireNonNull(committedTotal, "Committed total must not be null");

        if (finalPaymentAcknowledged && !advanceAcknowledged) {
            throw new IllegalArgumentException(
                    "Final payment cannot be acknowledged without advance acknowledgment"
            );
        }

        Money remainingBalance = finalPaymentAcknowledged ? Money.zero() : committedTotal;

        return new PaymentSummary(
                advanceAcknowledged,
                finalPaymentAcknowledged,
                committedTotal,
                remainingBalance
        );
    }

    /**
     * Crea el resumen inicial de una Orden confirmada con anticipo reconocido.
     */
    public static PaymentSummary forConfirmedOrder(Money committedTotal) {
        return of(true, false, committedTotal);
    }

    /**
     * Devuelve un nuevo resumen con el pago final reconocido.
     */
    public PaymentSummary acknowledgeFinalPayment() {
        if (!advanceAcknowledged) {
            throw new IllegalArgumentException(
                    "Final payment cannot be acknowledged without advance acknowledgment"
            );
        }
        if (finalPaymentAcknowledged) {
            return this;
        }
        return of(true, true, committedTotal);
    }

    public boolean isAdvanceAcknowledged() {
        return advanceAcknowledged;
    }

    public boolean isFinalPaymentAcknowledged() {
        return finalPaymentAcknowledged;
    }

    public Money getCommittedTotal() {
        return committedTotal;
    }

    public Money getRemainingBalance() {
        return remainingBalance;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PaymentSummary that = (PaymentSummary) other;
        return advanceAcknowledged == that.advanceAcknowledged
                && finalPaymentAcknowledged == that.finalPaymentAcknowledged
                && committedTotal.equals(that.committedTotal)
                && remainingBalance.equals(that.remainingBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                advanceAcknowledged,
                finalPaymentAcknowledged,
                committedTotal,
                remainingBalance
        );
    }

    @Override
    public String toString() {
        return "PaymentSummary{"
                + "advanceAcknowledged=" + advanceAcknowledged
                + ", finalPaymentAcknowledged=" + finalPaymentAcknowledged
                + ", committedTotal=" + committedTotal
                + ", remainingBalance=" + remainingBalance
                + '}';
    }
}
