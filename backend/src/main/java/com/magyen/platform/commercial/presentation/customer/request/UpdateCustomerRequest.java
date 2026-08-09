package com.magyen.platform.commercial.presentation.customer.request;

/**
 * Payload HTTP para actualizar el nombre de un cliente.
 */
public record UpdateCustomerRequest(
        String name
) {
}
