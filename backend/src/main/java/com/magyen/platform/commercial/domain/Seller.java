package com.magyen.platform.commercial.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root que representa un vendedor interno de Magyen.
 * <p>
 * Identifica de forma estable a la persona que vendió una cotización u orden.
 * No es una identidad de autenticación y no calcula comisiones.
 */
public class Seller {

    private final UUID id;
    private String name;
    private boolean active;

    private Seller(UUID id, String name, boolean active) {
        this.id = Objects.requireNonNull(id, "Seller id must not be null");
        this.name = requireNonBlank(name, "Seller name must not be blank");
        this.active = active;
    }

    /**
     * Crea un vendedor activo con identidad nueva.
     */
    public static Seller create(String name) {
        return new Seller(UUID.randomUUID(), name, true);
    }

    /**
     * Reconstruye un vendedor desde persistencia. No aplica lógica de creación.
     */
    public static Seller reconstitute(UUID id, String name, boolean active) {
        return new Seller(id, name, active);
    }

    /**
     * Actualiza el nombre preservando la identidad histórica.
     */
    public void rename(String name) {
        this.name = requireNonBlank(name, "Seller name must not be blank");
    }

    /**
     * Desactiva el vendedor. No elimina el registro ni rompe atribución histórica.
     */
    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Seller seller = (Seller) other;
        return id.equals(seller.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        String normalized = value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }
}
