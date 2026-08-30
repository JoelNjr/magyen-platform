package com.magyen.platform.finance.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link FinancialTransaction}.
 * <p>
 * La implementación concreta vive en la capa de infraestructura.
 */
public interface FinancialTransactionRepository {

    FinancialTransaction save(FinancialTransaction financialTransaction);

    Optional<FinancialTransaction> findById(UUID id);

    /**
     * Busca un movimiento por origen de negocio (trazabilidad / idempotencia).
     */
    Optional<FinancialTransaction> findBySourceTypeAndSourceId(
            FinancialTransactionSourceType sourceType,
            UUID sourceId
    );

    /**
     * Lista todos los movimientos ordenados del más reciente al más antiguo.
     */
    List<FinancialTransaction> findAllNewestFirst();

    /**
     * Lista movimientos con {@code transactionDate} en {@code [fromDate, toDate]} (inclusive).
     */
    List<FinancialTransaction> findByTransactionDateBetweenNewestFirst(LocalDate fromDate, LocalDate toDate);

    /**
     * Suma montos del ledger por tipo en {@code [fromDate, toDate]} (inclusive).
     */
    BigDecimal sumAmountByTypeBetween(
            FinancialTransactionType type,
            LocalDate fromDate,
            LocalDate toDate
    );

    /**
     * Cuenta movimientos del ledger con transactionDate en {@code [fromDate, toDate]}.
     */
    long countByTransactionDateBetween(LocalDate fromDate, LocalDate toDate);
}
