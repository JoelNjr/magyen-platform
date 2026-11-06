package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GetPlotterCustomerAccountResult(
        UUID customerId,
        String customerName,
        BigDecimal debitTotal,
        BigDecimal paymentTotal,
        BigDecimal outstandingAmount,
        List<PlotterAccountMovement> movements
) {
}
