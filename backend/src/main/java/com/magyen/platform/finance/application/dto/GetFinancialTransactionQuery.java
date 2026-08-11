package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Consulta de un movimiento financiero por identidad.
 */
public record GetFinancialTransactionQuery(
        UUID transactionId
) {
}
