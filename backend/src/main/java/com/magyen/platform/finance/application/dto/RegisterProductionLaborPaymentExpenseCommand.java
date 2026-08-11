package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando para registrar el gasto de caja de un pago de mano de obra por producción.
 */
public record RegisterProductionLaborPaymentExpenseCommand(
        UUID laborWorkId,
        BigDecimal amount,
        LocalDate paymentDate,
        String operatorDisplayName,
        String observation
) {
}
