package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollCompensationType;

import java.util.UUID;

/**
 * Datos mínimos del empleado de nómina usados por Production para mano de obra.
 */
public record ResolveProductionLaborOperatorResult(
        UUID employeeId,
        String displayName,
        boolean active,
        PayrollCompensationType compensationType
) {
}
