package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollDeductionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando para registrar un descuento de nómina.
 * <p>
 * No crea un movimiento del ledger.
 */
public record CreatePayrollDeductionCommand(
        UUID employeeId,
        PayrollDeductionType type,
        BigDecimal amount,
        LocalDate deductionDate,
        String description
) {
}
