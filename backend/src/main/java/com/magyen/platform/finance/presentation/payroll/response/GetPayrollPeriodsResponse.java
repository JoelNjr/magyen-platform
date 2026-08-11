package com.magyen.platform.finance.presentation.payroll.response;

import java.util.List;

/**
 * Respuesta HTTP del listado de períodos de nómina.
 */
public record GetPayrollPeriodsResponse(
        List<PayrollPeriodResponse> periods
) {
}
