package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.RegisterProductionAdditionalCostExpenseCommand;
import com.magyen.platform.finance.application.dto.RegisterProductionAdditionalCostExpenseResult;
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
 * Registra el EXPENSE de un costo directo adicional de producción.
 * <p>
 * {@code sourceType = PRODUCTION}, {@code sourceId = additionalCostId}.
 * Reutiliza {@code uq_financial_transactions_production_source} para evitar doble contabilización.
 */
public class RegisterProductionAdditionalCostExpenseUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;

    public RegisterProductionAdditionalCostExpenseUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    @Transactional
    public RegisterProductionAdditionalCostExpenseResult execute(
            RegisterProductionAdditionalCostExpenseCommand command
    ) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.additionalCostId(), "Additional cost id must not be null");
        Objects.requireNonNull(command.amount(), "Amount must not be null");
        Objects.requireNonNull(command.incurredDate(), "Incurred date must not be null");

        Optional<FinancialTransaction> existing = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PRODUCTION,
                command.additionalCostId()
        );
        if (existing.isPresent()) {
            throw new FinanceDomainException(
                    "A PRODUCTION financial transaction already exists for this additional cost"
            );
        }

        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                FinancialAmount.of(command.amount()),
                command.incurredDate(),
                FinancialCategory.OTHER_EXPENSE.name(),
                command.description(),
                null,
                FinancialTransactionSourceType.PRODUCTION,
                command.additionalCostId()
        );

        FinancialTransaction saved = financialTransactionRepository.save(transaction);
        return new RegisterProductionAdditionalCostExpenseResult(
                saved.getId(),
                saved.getAmount().getValue()
        );
    }
}
