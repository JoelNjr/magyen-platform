package com.magyen.platform.finance.application.dto;

/**
 * Consulta de listado de empleados de nómina.
 * <p>
 * {@code active} opcional filtra por estado activo/inactivo.
 * {@code null} lista todos.
 */
public record GetPayrollEmployeesQuery(
        Boolean active
) {
    public static GetPayrollEmployeesQuery all() {
        return new GetPayrollEmployeesQuery(null);
    }
}
