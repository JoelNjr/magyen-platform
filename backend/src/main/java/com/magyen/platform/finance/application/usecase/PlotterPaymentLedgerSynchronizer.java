package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Sincroniza un pago de Plotter hacia un único ingreso del ledger.
 * <p>
 * Idempotente por {@code sourceType=PLOTTER} y {@code sourceId=plotterPaymentId}.
 */
final class PlotterPaymentLedgerSynchronizer {

    static final String DESCRIPTION = "Pago de trabajo de plotter";

    private final FinancialTransactionRepository financialTransactionRepository;

    PlotterPaymentLedgerSynchronizer(FinancialTransactionRepository financialTransactionRepository) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    FinancialTransaction ensureIncomeTransaction(
            UUID plotterPaymentId,
            BigDecimal amount,
            LocalDate paymentDate,
            String observation
    ) {
        Objects.requireNonNull(plotterPaymentId, "Plotter payment id must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(paymentDate, "Payment date must not be null");

        return financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.PLOTTER, plotterPaymentId)
                .orElseGet(() -> createIncomeTransaction(plotterPaymentId, amount, paymentDate, observation));
    }

    private FinancialTransaction createIncomeTransaction(
            UUID plotterPaymentId,
            BigDecimal amount,
            LocalDate paymentDate,
            String observation
    ) {
        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.INCOME,
                FinancialAmount.of(amount),
                paymentDate,
                FinancialCategory.PLOTTER_REVENUE.name(),
                DESCRIPTION,
                observation,
                FinancialTransactionSourceType.PLOTTER,
                plotterPaymentId
        );

        try {
            return financialTransactionRepository.save(transaction);
        } catch (DataIntegrityViolationException exception) {
            return financialTransactionRepository
                    .findBySourceTypeAndSourceId(FinancialTransactionSourceType.PLOTTER, plotterPaymentId)
                    .orElseThrow(() -> exception);
        }
    }
}
