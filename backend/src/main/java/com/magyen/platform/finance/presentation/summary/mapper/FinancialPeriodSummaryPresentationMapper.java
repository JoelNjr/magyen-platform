package com.magyen.platform.finance.presentation.summary.mapper;

import com.magyen.platform.finance.application.dto.GetFinancialPeriodSummaryQuery;
import com.magyen.platform.finance.application.dto.GetFinancialPeriodSummaryResult;
import com.magyen.platform.finance.presentation.summary.response.GetFinancialPeriodSummaryResponse;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Convierte entre HTTP y Application para el resumen financiero de período.
 */
public class FinancialPeriodSummaryPresentationMapper {

    public GetFinancialPeriodSummaryQuery toQuery(LocalDate fromDate, LocalDate toDate) {
        return new GetFinancialPeriodSummaryQuery(fromDate, toDate);
    }

    public GetFinancialPeriodSummaryResponse toResponse(GetFinancialPeriodSummaryResult result) {
        Objects.requireNonNull(result, "Summary result must not be null");
        return new GetFinancialPeriodSummaryResponse(
                result.fromDate(),
                result.toDate(),
                result.totalIncome(),
                result.totalExpense(),
                result.netResult(),
                result.transactionCount()
        );
    }
}
