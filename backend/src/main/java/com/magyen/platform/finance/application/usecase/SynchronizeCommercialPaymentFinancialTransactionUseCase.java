package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.SynchronizeCommercialPaymentFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.SynchronizeCommercialPaymentFinancialTransactionResult;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Resincroniza de forma idempotente un Payment existente hacia el ledger.
 * <p>
 * No crea Payments. Útil para verificar que un segundo intento no duplica ingresos.
 * No realiza backfill masivo de pagos históricos.
 */
public class SynchronizeCommercialPaymentFinancialTransactionUseCase {

    private final PaymentRepository paymentRepository;
    private final CommercialPaymentLedgerSynchronizer ledgerSynchronizer;

    public SynchronizeCommercialPaymentFinancialTransactionUseCase(
            PaymentRepository paymentRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "Payment repository must not be null");
        this.ledgerSynchronizer = new CommercialPaymentLedgerSynchronizer(
                Objects.requireNonNull(
                        financialTransactionRepository,
                        "Financial transaction repository must not be null"
                )
        );
    }

    @Transactional
    public SynchronizeCommercialPaymentFinancialTransactionResult execute(
            SynchronizeCommercialPaymentFinancialTransactionCommand command
    ) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.paymentId(), "Payment id must not be null");

        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + command.paymentId()
                ));

        FinancialTransaction transaction = ledgerSynchronizer.ensureIncomeTransaction(payment);

        return new SynchronizeCommercialPaymentFinancialTransactionResult(
                payment.getId(),
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount().getValue(),
                transaction.getTransactionDate(),
                transaction.getCategory(),
                transaction.getSourceType(),
                transaction.getSourceId()
        );
    }
}
