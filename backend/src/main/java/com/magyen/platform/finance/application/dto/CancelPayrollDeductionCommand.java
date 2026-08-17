package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Comando para cancelar un descuento de nómina sin borrarlo.
 */
public record CancelPayrollDeductionCommand(
        UUID employeeId,
        UUID deductionId
) {
}
