package com.magyen.platform.finance.application.dto;

import java.util.List;

/**
 * Resultado del listado de períodos de nómina.
 */
public record GetPayrollPeriodsResult(
        List<GetPayrollPeriodResult> periods
) {
}
