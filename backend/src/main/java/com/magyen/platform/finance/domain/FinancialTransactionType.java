package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Tipo de movimiento del ledger financiero.
 */
public enum FinancialTransactionType {

    INCOME,
    EXPENSE;

    /**
     * Interpreta el tipo de transacción desde entrada de negocio.
     */
    public static FinancialTransactionType of(String value) {
        Objects.requireNonNull(value, "Transaction type must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Transaction type must not be blank");
        }

        try {
            return FinancialTransactionType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid transaction type: " + value);
        }
    }

    /**
     * Reconstruye desde persistencia.
     */
    public static FinancialTransactionType reconstitute(String value) {
        return of(value);
    }
}
