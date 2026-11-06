package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterPlotterCustomerPaymentResponse(
        UUID paymentId,
        UUID customerId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations,
        BigDecimal debitTotal,
        BigDecimal paymentTotal,
        BigDecimal outstandingAmount,
        boolean alreadyRegistered
) {
}
