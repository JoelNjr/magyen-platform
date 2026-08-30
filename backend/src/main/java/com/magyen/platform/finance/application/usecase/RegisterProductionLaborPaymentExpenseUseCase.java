package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.RegisterProductionLaborPaymentExpenseCommand;
import com.magyen.platform.finance.application.dto.RegisterProductionLaborPaymentExpenseResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.LaborPaymentWeek;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Acumula el EXPENSE de caja de mano de obra en un movimiento semanal.
 * <p>
 * Semana ISO lunes-domingo según la fecha real de pago.
 * {@code sourceType = PAYROLL}, {@code sourceId = LaborPaymentWeek.sourceId()}.
 * Reutiliza {@code uq_financial_transactions_payroll_source}: un movimiento por semana.
 * El detalle por empleado permanece en Production ({@code ProductionLaborWork}).
 * No crea una fila nueva si la semana ya tiene movimiento: suma el monto.
 */
public class RegisterProductionLaborPaymentExpenseUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;

    public RegisterProductionLaborPaymentExpenseUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    @Transactional
    public RegisterProductionLaborPaymentExpenseResult execute(
            RegisterProductionLaborPaymentExpenseCommand command
    ) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.laborWorkId(), "Labor work id must not be null");
        Objects.requireNonNull(command.amount(), "Amount must not be null");
        Objects.requireNonNull(command.paymentDate(), "Payment date must not be null");

        LaborPaymentWeek week = LaborPaymentWeek.of(command.paymentDate());
        FinancialAmount paymentAmount = FinancialAmount.of(command.amount());

        Optional<FinancialTransaction> existing = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PAYROLL,
                week.sourceId()
        );

        FinancialTransaction saved;
        if (existing.isPresent()) {
            FinancialTransaction current = existing.get();
            int nextCount = LaborPaymentWeek.parsePaymentCount(current.getObservation()) + 1;
            saved = financialTransactionRepository.save(
                    current.withAmountDescriptionAndObservation(
                            current.getAmount().add(paymentAmount),
                            week.description(nextCount),
                            week.observation(nextCount)
                    )
            );
        } else {
            saved = financialTransactionRepository.save(FinancialTransaction.create(
                    FinancialTransactionType.EXPENSE,
                    paymentAmount,
                    week.getWeekStart(),
                    FinancialCategory.PAYROLL.name(),
                    week.description(1),
                    week.observation(1),
                    FinancialTransactionSourceType.PAYROLL,
                    week.sourceId()
            ));
        }

        return new RegisterProductionLaborPaymentExpenseResult(
                saved.getId(),
                saved.getAmount().getValue()
        );
    }
}
