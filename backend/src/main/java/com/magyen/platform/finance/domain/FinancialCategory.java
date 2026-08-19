package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Vocabulario tipado de categorías financieras para reportes futuros.
 * <p>
 * No reemplaza {@link FinancialTransaction#getCategory()}, que permanece como
 * texto libre para preservar compatibilidad con categorías históricas arbitrarias
 * (por ejemplo {@code "Servicios"}).
 */
public enum FinancialCategory {

    SALES(FinancialTransactionType.INCOME),
    PLOTTER_REVENUE(FinancialTransactionType.INCOME),
    INTERNAL_PLOTTER_SERVICE_INCOME(FinancialTransactionType.INCOME),
    OTHER_INCOME(FinancialTransactionType.INCOME),

    MATERIALS(FinancialTransactionType.EXPENSE),
    PAPER(FinancialTransactionType.EXPENSE),
    INK(FinancialTransactionType.EXPENSE),
    DTF(FinancialTransactionType.EXPENSE),
    EMBROIDERY(FinancialTransactionType.EXPENSE),
    SERVICES(FinancialTransactionType.EXPENSE),
    INTERNAL_PLOTTER_SERVICE_EXPENSE(FinancialTransactionType.EXPENSE),
    PAYROLL(FinancialTransactionType.EXPENSE),
    PRODUCTION_PAYMENT(FinancialTransactionType.EXPENSE),
    CREDIT_PAYMENT(FinancialTransactionType.EXPENSE),
    TRANSPORT(FinancialTransactionType.EXPENSE),
    MAINTENANCE(FinancialTransactionType.EXPENSE),
    SOFTWARE(FinancialTransactionType.EXPENSE),
    ADVERTISING(FinancialTransactionType.EXPENSE),
    OTHER_EXPENSE(FinancialTransactionType.EXPENSE);

    private final FinancialTransactionType transactionType;

    FinancialCategory(FinancialTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public FinancialTransactionType getTransactionType() {
        return transactionType;
    }

    /**
     * Interpreta un código de categoría conocido.
     */
    public static FinancialCategory of(String value) {
        Objects.requireNonNull(value, "Financial category must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Financial category must not be blank");
        }

        try {
            return FinancialCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid financial category: " + value);
        }
    }

    /**
     * Intenta reconocer un código tipado sin fallar ante texto libre histórico.
     */
    public static Optional<FinancialCategory> tryParse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(FinancialCategory.valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
