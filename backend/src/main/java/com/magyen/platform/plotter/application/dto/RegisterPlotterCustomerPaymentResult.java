package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterPlotterCustomerPaymentResult(
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
