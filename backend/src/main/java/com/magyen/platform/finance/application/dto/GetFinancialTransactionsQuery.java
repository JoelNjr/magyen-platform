package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;

public record GetFinancialTransactionsQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
