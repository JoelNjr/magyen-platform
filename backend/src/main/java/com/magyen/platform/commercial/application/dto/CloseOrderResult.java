package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.OrderStatus;

import java.util.UUID;

public record CloseOrderResult(
        UUID orderId,
        OrderStatus status,
        boolean finalPaymentAcknowledged
) {
}
