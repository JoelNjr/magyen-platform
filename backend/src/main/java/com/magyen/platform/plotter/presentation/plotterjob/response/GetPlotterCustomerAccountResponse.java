package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GetPlotterCustomerAccountResponse(
        UUID customerId,
        String customerName,
        BigDecimal debitTotal,
        BigDecimal paymentTotal,
        BigDecimal outstandingAmount,
        List<PlotterAccountMovementResponse> movements
) {
}
