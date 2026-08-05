package com.magyen.platform.commercial.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Compromiso de entrega aceptado en una Orden.
 * <p>
 * Value Object inmutable. No modela logística ni tracking de envíos.
 */
public final class DeliveryCommitment {

    private final LocalDate promisedDeliveryDate;
    private final String deliveryObservations;

    private DeliveryCommitment(LocalDate promisedDeliveryDate, String deliveryObservations) {
        this.promisedDeliveryDate = promisedDeliveryDate;
        this.deliveryObservations = deliveryObservations;
    }

    public static DeliveryCommitment of(LocalDate promisedDeliveryDate, String deliveryObservations) {
        Objects.requireNonNull(promisedDeliveryDate, "Promised delivery date must not be null");
        return new DeliveryCommitment(promisedDeliveryDate, deliveryObservations);
    }

    public static DeliveryCommitment of(LocalDate promisedDeliveryDate) {
        return of(promisedDeliveryDate, null);
    }

    public LocalDate getPromisedDeliveryDate() {
        return promisedDeliveryDate;
    }

    public String getDeliveryObservations() {
        return deliveryObservations;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DeliveryCommitment that = (DeliveryCommitment) other;
        return promisedDeliveryDate.equals(that.promisedDeliveryDate)
                && Objects.equals(deliveryObservations, that.deliveryObservations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promisedDeliveryDate, deliveryObservations);
    }

    @Override
    public String toString() {
        return "DeliveryCommitment{"
                + "promisedDeliveryDate=" + promisedDeliveryDate
                + ", deliveryObservations='" + deliveryObservations + '\''
                + '}';
    }
}
