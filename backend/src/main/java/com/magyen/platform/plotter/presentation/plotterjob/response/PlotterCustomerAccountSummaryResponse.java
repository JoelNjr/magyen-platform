package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PlotterCustomerAccountSummaryResponse(
        UUID customerId,
        String customerName,
        BigDecimal debitTotal,
        BigDecimal paymentTotal,
        BigDecimal outstandingAmount,
        int jobCount
) {
}
