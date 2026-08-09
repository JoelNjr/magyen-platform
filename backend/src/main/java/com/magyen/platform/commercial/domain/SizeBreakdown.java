package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.OrderDomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Distribución de cantidad por talla dentro de un {@link OrderItem}.
 * <p>
 * Value Object / entidad hija del ítem comercial. No modifica la cantidad comprometida del ítem.
 */
public final class SizeBreakdown {

    private final UUID id;
    private final String size;
    private final int quantity;

    SizeBreakdown(UUID id, String size, int quantity) {
        this.id = Objects.requireNonNull(id, "Size breakdown id must not be null");
        this.size = requireNonBlank(size, "Size must not be blank");
        if (quantity <= 0) {
            throw new OrderDomainException("Size quantity must be greater than zero");
        }
        this.quantity = quantity;
    }

    public static SizeBreakdown create(String size, int quantity) {
        return new SizeBreakdown(UUID.randomUUID(), size, quantity);
    }

    /**
     * Reconstruye una distribución de talla desde persistencia.
     */
    public static SizeBreakdown reconstitute(UUID id, String size, int quantity) {
        return new SizeBreakdown(id, size, quantity);
    }

    public UUID getId() {
        return id;
    }

    public String getSize() {
        return size;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        SizeBreakdown that = (SizeBreakdown) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new OrderDomainException(message);
        }
        return trimmed;
    }
}
