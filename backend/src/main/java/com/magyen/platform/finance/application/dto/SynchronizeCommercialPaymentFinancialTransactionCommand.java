package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Entrada para resincronizar un Payment comercial hacia el ledger.
 */
public record SynchronizeCommercialPaymentFinancialTransactionCommand(
        UUID paymentId
) {
}
