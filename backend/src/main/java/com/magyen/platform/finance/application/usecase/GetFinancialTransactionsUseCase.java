package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetFinancialTransactionResult;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionsResult;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;

import java.util.Objects;

/**
 * Caso de uso que lista los movimientos del ledger financiero (más recientes primero).
 */
public class GetFinancialTransactionsUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;

    public GetFinancialTransactionsUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    public GetFinancialTransactionsResult execute() {
        return new GetFinancialTransactionsResult(
                financialTransactionRepository.findAllNewestFirst().stream()
                        .map(this::toResult)
                        .toList()
        );
    }

    private GetFinancialTransactionResult toResult(FinancialTransaction transaction) {
        return new GetFinancialTransactionResult(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount().getValue(),
                transaction.getTransactionDate(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getObservation(),
                transaction.getSourceType(),
                transaction.getSourceId()
        );
    }
}
