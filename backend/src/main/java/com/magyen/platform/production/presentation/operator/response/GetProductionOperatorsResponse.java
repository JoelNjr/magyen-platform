package com.magyen.platform.production.presentation.operator.response;

import java.util.List;

/**
 * Respuesta HTTP con el listado de operarios de producción.
 */
public record GetProductionOperatorsResponse(
        List<ProductionOperatorResponse> operators
) {
}
