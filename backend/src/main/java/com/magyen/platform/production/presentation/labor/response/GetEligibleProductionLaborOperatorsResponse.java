package com.magyen.platform.production.presentation.labor.response;

import java.util.List;

/**
 * Lista de operarios activos PRODUCTION_BASED.
 */
public record GetEligibleProductionLaborOperatorsResponse(
        List<ProductionLaborOperatorResponse> operators
) {
}
