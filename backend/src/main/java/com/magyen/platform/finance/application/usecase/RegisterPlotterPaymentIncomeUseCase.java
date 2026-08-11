package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.RegisterPlotterPaymentIncomeCommand;
import com.magyen.platform.finance.application.dto.RegisterPlotterPaymentIncomeResult;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Garantiza el ingreso del ledger para un pago de Plotter.
 * <p>
 * Invocado desde el adaptador de Plotter. Idempotente. No crea pagos de Plotter.
 */
public class RegisterPlotterPaymentIncomeUseCase {

    private final PlotterPaymentLedgerSynchronizer ledgerSynchronizer;

    public RegisterPlotterPaymentIncomeUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.ledgerSynchronizer = new PlotterPaymentLedgerSynchronizer(
                Objects.requireNonNull(
                        financialTransactionRepository,
                        "Financial transaction repository must not be null"
                )
        );
    }

    @Transactional
    public RegisterPlotterPaymentIncomeResult execute(RegisterPlotterPaymentIncomeCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.plotterPaymentId(), "Plotter payment id must not be null");
        Objects.requireNonNull(command.amount(), "Amount must not be null");
        Objects.requireNonNull(command.paymentDate(), "Payment date must not be null");

        FinancialTransaction transaction = ledgerSynchronizer.ensureIncomeTransaction(
                command.plotterPaymentId(),
                command.amount(),
                command.paymentDate(),
                command.observation()
        );

        return new RegisterPlotterPaymentIncomeResult(
                command.plotterPaymentId(),
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
