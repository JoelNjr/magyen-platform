package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetFinancialPeriodSummaryQuery;
import com.magyen.platform.finance.application.dto.GetFinancialPeriodSummaryResult;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Resume ingresos, egresos y neto del ledger para un período.
 * <p>
 * Solo usa {@code FinancialTransaction}. No considera obligaciones ni ocurrencias.
 */
public class GetFinancialPeriodSummaryUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;

    public GetFinancialPeriodSummaryUseCase(FinancialTransactionRepository financialTransactionRepository) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    public GetFinancialPeriodSummaryResult execute(GetFinancialPeriodSummaryQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        validateQuery(query);

        BigDecimal totalIncome = financialTransactionRepository.sumAmountByTypeBetween(
                FinancialTransactionType.INCOME,
                query.fromDate(),
                query.toDate()
        );
        BigDecimal totalExpense = financialTransactionRepository.sumAmountByTypeBetween(
                FinancialTransactionType.EXPENSE,
                query.fromDate(),
                query.toDate()
        );
        BigDecimal netResult = totalIncome.subtract(totalExpense);
        long transactionCount = financialTransactionRepository.countByTransactionDateBetween(
                query.fromDate(),
                query.toDate()
        );

        return new GetFinancialPeriodSummaryResult(
                query.fromDate(),
                query.toDate(),
                totalIncome,
                totalExpense,
                netResult,
                transactionCount
        );
    }

    private void validateQuery(GetFinancialPeriodSummaryQuery query) {
        if (query.fromDate() == null) {
            throw new FinanceDomainException("From date must not be null");
        }
        if (query.toDate() == null) {
            throw new FinanceDomainException("To date must not be null");
        }
        if (query.toDate().isBefore(query.fromDate())) {
            throw new FinanceDomainException("From date must not be after to date");
        }
    }
}
