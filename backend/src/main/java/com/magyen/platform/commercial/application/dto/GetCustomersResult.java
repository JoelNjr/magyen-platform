package com.magyen.platform.commercial.application.dto;

import java.util.List;

/**
 * Resultado del caso de uso que consulta los clientes existentes.
 */
public record GetCustomersResult(
        List<CustomerResult> customers
) {
}
