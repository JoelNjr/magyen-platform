package com.magyen.platform.production.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root que representa un operario de producción de Magyen.
 * <p>
 * Identifica de forma estable a la persona que ejecuta trabajo de fabricación.
 * No es una identidad de autenticación ni un vendedor comercial.
 * No requiere cuenta de login.
 */
public class ProductionOperator {

    private final UUID id;
    private String name;
    private boolean active;

    private ProductionOperator(UUID id, String name, boolean active) {
        this.id = Objects.requireNonNull(id, "Production operator id must not be null");
        this.name = requireNonBlank(name, "Production operator name must not be blank");
        this.active = active;
    }

    /**
     * Crea un operario activo con identidad nueva.
     */
    public static ProductionOperator create(String name) {
        return new ProductionOperator(UUID.randomUUID(), name, true);
    }

    /**
     * Reconstruye un operario desde persistencia. No aplica lógica de creación.
     */
    public static ProductionOperator reconstitute(UUID id, String name, boolean active) {
        return new ProductionOperator(id, name, active);
    }

    /**
     * Actualiza el nombre preservando la identidad histórica.
     */
    public void rename(String name) {
        this.name = requireNonBlank(name, "Production operator name must not be blank");
    }

    /**
     * Desactiva el operario. No elimina el registro ni rompe atribución histórica.
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
        ProductionOperator operator = (ProductionOperator) other;
        return id.equals(operator.id);
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
