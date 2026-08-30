package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetFinancialTransactionResult;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionsQuery;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionsResult;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.List;
import java.util.Objects;

/**
 * Lista los movimientos del ledger. Si hay rango de fechas, filtra por transactionDate.
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
        return execute(new GetFinancialTransactionsQuery(null, null));
    }

    public GetFinancialTransactionsResult execute(GetFinancialTransactionsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        validateQuery(query);

        List<FinancialTransaction> transactions;
        if (query.fromDate() == null && query.toDate() == null) {
            transactions = financialTransactionRepository.findAllNewestFirst();
        } else {
            transactions = financialTransactionRepository.findByTransactionDateBetweenNewestFirst(
                    query.fromDate(),
                    query.toDate()
            );
        }

        return new GetFinancialTransactionsResult(
                transactions.stream().map(this::toResult).toList()
        );
    }

    private void validateQuery(GetFinancialTransactionsQuery query) {
        if (query.fromDate() == null && query.toDate() == null) {
            return;
        }
        if (query.fromDate() == null || query.toDate() == null) {
            throw new FinanceDomainException("From date and to date must be provided together");
        }
        if (query.toDate().isBefore(query.fromDate())) {
            throw new FinanceDomainException("From date must not be after to date");
        }
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
