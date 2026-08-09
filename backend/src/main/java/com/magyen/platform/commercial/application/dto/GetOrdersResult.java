package com.magyen.platform.commercial.application.dto;

import java.util.List;

/**
 * Resultado del caso de uso que consulta las Órdenes existentes.
 */
public record GetOrdersResult(
        List<OrderResult> orders
) {
}
