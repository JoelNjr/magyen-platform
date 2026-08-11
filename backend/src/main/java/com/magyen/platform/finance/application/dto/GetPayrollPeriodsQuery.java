package com.magyen.platform.finance.application.dto;

/**
 * Consulta de listado de períodos de nómina (más recientes primero).
 */
public record GetPayrollPeriodsQuery() {
    public static GetPayrollPeriodsQuery all() {
        return new GetPayrollPeriodsQuery();
    }
}
