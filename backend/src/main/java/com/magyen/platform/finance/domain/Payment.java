package com.magyen.platform.finance.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del módulo financiero.
 * <p>
 * Representa un pago realizado por un cliente sobre una Orden.
 * Referencia la Orden comercial únicamente por identidad ({@code orderId}).
 */
public class Payment {

    private final UUID id;
    private final UUID orderId;
    private final PaymentAmount amount;
    private final LocalDate paymentDate;
    private final String observations;

    private Payment(
            UUID id,
            UUID orderId,
            PaymentAmount amount,
            LocalDate paymentDate,
            String observations
    ) {
        this.id = Objects.requireNonNull(id, "Payment id must not be null");
        this.orderId = Objects.requireNonNull(orderId, "Order id must not be null");
        this.amount = Objects.requireNonNull(amount, "Payment amount must not be null");
        this.paymentDate = Objects.requireNonNull(paymentDate, "Payment date must not be null");
        this.observations = observations;
    }

    /**
     * Crea un pago válido asociado a una Orden por identidad.
     * <p>
     * El monto debe ser mayor que cero. Las observaciones son opcionales.
     */
    public static Payment create(
            UUID orderId,
            PaymentAmount amount,
            LocalDate paymentDate,
            String observations
    ) {
        return new Payment(
                UUID.randomUUID(),
                orderId,
                amount,
                paymentDate,
                observations
        );
    }

    /**
     * Reconstruye un pago desde persistencia. No aplica lógica de creación de negocio.
     */
    public static Payment reconstitute(
            UUID id,
            UUID orderId,
            PaymentAmount amount,
            LocalDate paymentDate,
            String observations
    ) {
        return new Payment(
                id,
                orderId,
                amount,
                paymentDate,
                observations
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public PaymentAmount getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getObservations() {
        return observations;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Payment that = (Payment) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
