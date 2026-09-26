package com.magyen.platform.commercial.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root que representa un cliente comercial.
 * <p>
 * Mantiene la identidad y el nombre utilizado en procesos comerciales.
 */
public class Customer {

    private final UUID id;
    private String name;
    private final CustomerCategory category;

    private Customer(UUID id, String name, CustomerCategory category) {
        this.id = Objects.requireNonNull(id, "Customer id must not be null");
        this.name = requireNonBlank(name, "Customer name must not be blank");
        this.category = Objects.requireNonNull(category, "Customer category must not be null");
    }

    /**
     * Crea un cliente Magyen. Los flujos de cotización usan este grupo.
     */
    public static Customer create(String name) {
        return create(name, CustomerCategory.MAGYEN);
    }

    /**
     * Crea un cliente con identidad nueva y un único grupo comercial.
     */
    public static Customer create(String name, CustomerCategory category) {
        return new Customer(UUID.randomUUID(), name, category);
    }

    /**
     * Reconstruye un cliente desde persistencia. No aplica lógica de creación de negocio.
     */
    public static Customer reconstitute(UUID id, String name, CustomerCategory category) {
        return new Customer(id, name, category);
    }

    /**
     * Actualiza el nombre del cliente preservando su identidad.
     */
    public void rename(String name) {
        this.name = requireNonBlank(name, "Customer name must not be blank");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CustomerCategory getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Customer customer = (Customer) other;
        return id.equals(customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
