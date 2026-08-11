package com.magyen.platform.finance.application.dto;

import java.util.List;

/**
 * Resultado del listado de movimientos financieros.
 */
public record GetFinancialTransactionsResult(
        List<GetFinancialTransactionResult> transactions
) {
}
