package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetFinancialTransactionQuery;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionResult;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;

import java.util.Objects;

/**
 * Caso de uso que consulta un movimiento financiero por identidad.
 */
public class GetFinancialTransactionUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;

    public GetFinancialTransactionUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    public GetFinancialTransactionResult execute(GetFinancialTransactionQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.transactionId(), "Transaction id must not be null");

        FinancialTransaction transaction = financialTransactionRepository.findById(query.transactionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Financial transaction not found: " + query.transactionId()
                ));

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
