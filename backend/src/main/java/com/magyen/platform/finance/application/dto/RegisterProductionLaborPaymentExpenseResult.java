package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado del registro del gasto de mano de obra por producción.
 */
public record RegisterProductionLaborPaymentExpenseResult(
        UUID financialTransactionId,
        BigDecimal amount
) {
}
