package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.Payment;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Objects;

/**
 * Sincroniza un {@link Payment} comercial hacia un único ingreso del ledger.
 * <p>
 * Idempotente por {@code sourceType=COMMERCIAL_ORDER} y {@code sourceId=paymentId}.
 * No backfill de pagos históricos: solo se invoca desde el flujo de registro nuevo
 * o desde una resincronización explícita del mismo Payment.
 */
final class CommercialPaymentLedgerSynchronizer {

    static final String DESCRIPTION = "Pago de orden comercial";

    private final FinancialTransactionRepository financialTransactionRepository;

    CommercialPaymentLedgerSynchronizer(FinancialTransactionRepository financialTransactionRepository) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    /**
     * Garantiza exactamente un {@link FinancialTransaction} INCOME para el Payment.
     */
    FinancialTransaction ensureIncomeTransaction(Payment payment) {
        Objects.requireNonNull(payment, "Payment must not be null");

        return financialTransactionRepository
                .findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.COMMERCIAL_ORDER,
                        payment.getId()
                )
                .orElseGet(() -> createIncomeTransaction(payment));
    }

    private FinancialTransaction createIncomeTransaction(Payment payment) {
        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.INCOME,
                FinancialAmount.of(payment.getAmount().getValue()),
                payment.getPaymentDate(),
                FinancialCategory.SALES.name(),
                DESCRIPTION,
                payment.getObservations(),
                FinancialTransactionSourceType.COMMERCIAL_ORDER,
                payment.getId()
        );

        try {
            return financialTransactionRepository.save(transaction);
        } catch (DataIntegrityViolationException exception) {
            return financialTransactionRepository
                    .findBySourceTypeAndSourceId(
                            FinancialTransactionSourceType.COMMERCIAL_ORDER,
                            payment.getId()
                    )
                    .orElseThrow(() -> exception);
        }
    }
}
