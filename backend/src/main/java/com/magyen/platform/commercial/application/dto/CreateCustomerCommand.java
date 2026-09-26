package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.CustomerCategory;

/**
 * Entrada del caso de uso para crear un cliente.
 * <p>
 * El constructor de un solo argumento conserva clientes Magyen para los flujos
 * comerciales existentes. Plotter debe pasar {@link CustomerCategory#PLOTTER}.
 */
public record CreateCustomerCommand(
        String name,
        CustomerCategory category
) {
    public CreateCustomerCommand(String name) {
        this(name, CustomerCategory.MAGYEN);
    }
}
