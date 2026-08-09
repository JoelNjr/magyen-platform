package com.magyen.platform.commercial.presentation.customer.response;

import java.util.List;

/**
 * Respuesta HTTP con los clientes existentes.
 */
public record GetCustomersResponse(
        List<CustomerResponse> customers
) {
}
