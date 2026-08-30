package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterProductionAdditionalCostExpenseResult(
        UUID financialTransactionId,
        BigDecimal amount
) {
}
