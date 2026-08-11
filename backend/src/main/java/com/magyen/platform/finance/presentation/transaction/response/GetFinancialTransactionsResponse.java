package com.magyen.platform.finance.presentation.transaction.response;

import java.util.List;

/**
 * Respuesta HTTP del listado de movimientos financieros.
 */
public record GetFinancialTransactionsResponse(
        List<FinancialTransactionResponse> transactions
) {
}
