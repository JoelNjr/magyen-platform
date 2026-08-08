package com.magyen.platform.commercial.application.dto;

import java.util.List;

/**
 * Resultado del caso de uso que consulta las cotizaciones existentes.
 */
public record GetQuotationsResult(
        List<QuotationResult> quotations
) {
}
