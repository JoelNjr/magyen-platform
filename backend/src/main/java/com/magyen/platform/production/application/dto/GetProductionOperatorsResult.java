package com.magyen.platform.production.application.dto;

import java.util.List;

/**
 * Resultado de consultar los operarios de producción existentes.
 */
public record GetProductionOperatorsResult(
        List<ProductionOperatorResult> operators
) {
}
