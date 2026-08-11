package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Objects;

/**
 * Caso de uso que registra un movimiento de ingreso o gasto en el ledger financiero.
 */
public class RegisterFinancialTransactionUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;

    public RegisterFinancialTransactionUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    public RegisterFinancialTransactionResult execute(RegisterFinancialTransactionCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        FinancialTransactionSourceType sourceType = command.sourceType() == null
                ? FinancialTransactionSourceType.MANUAL
                : command.sourceType();

        FinancialTransaction transaction = FinancialTransaction.create(
                command.type(),
                FinancialAmount.of(command.amount()),
                command.transactionDate(),
                command.category(),
                command.description(),
                command.observation(),
                sourceType,
                command.sourceId()
        );

        FinancialTransaction saved = financialTransactionRepository.save(transaction);

        return new RegisterFinancialTransactionResult(
                saved.getId(),
                saved.getType(),
                saved.getAmount().getValue(),
                saved.getTransactionDate(),
                saved.getCategory(),
                saved.getDescription(),
                saved.getObservation(),
                saved.getSourceType(),
                saved.getSourceId()
        );
    }

    private void validateCommand(RegisterFinancialTransactionCommand command) {
        if (command.type() == null) {
            throw new FinanceDomainException("Transaction type must not be null");
        }
        if (command.amount() == null) {
            throw new FinanceDomainException("Amount must not be null");
        }
        if (command.transactionDate() == null) {
            throw new FinanceDomainException("Transaction date must not be null");
        }
        if (command.category() == null || command.category().isBlank()) {
            throw new FinanceDomainException("Category must not be blank");
        }
    }
}
