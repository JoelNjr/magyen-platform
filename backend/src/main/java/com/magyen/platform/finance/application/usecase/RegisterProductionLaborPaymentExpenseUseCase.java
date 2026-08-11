package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.RegisterProductionLaborPaymentExpenseCommand;
import com.magyen.platform.finance.application.dto.RegisterProductionLaborPaymentExpenseResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Registra el EXPENSE de caja por pago de mano de obra por producción.
 * <p>
 * {@code sourceType = PAYROLL}, {@code sourceId = laborWorkId}.
 * Reutiliza {@code uq_financial_transactions_payroll_source}.
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

        Optional<FinancialTransaction> existing = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PAYROLL,
                command.laborWorkId()
        );
        if (existing.isPresent()) {
            throw new FinanceDomainException("A PAYROLL financial transaction already exists for this labor work");
        }

        String description = buildDescription(command.operatorDisplayName());

        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                FinancialAmount.of(command.amount()),
                command.paymentDate(),
                FinancialCategory.PAYROLL.name(),
                description,
                command.observation(),
                FinancialTransactionSourceType.PAYROLL,
                command.laborWorkId()
        );

        FinancialTransaction saved = financialTransactionRepository.save(transaction);
        return new RegisterProductionLaborPaymentExpenseResult(
                saved.getId(),
                saved.getAmount().getValue()
        );
    }

    private static String buildDescription(String operatorDisplayName) {
        if (operatorDisplayName == null || operatorDisplayName.isBlank()) {
            return "Pago de mano de obra";
        }
        return "Pago de mano de obra - " + operatorDisplayName.trim();
    }
}
