package com.magyen.platform.commercial.presentation.order.response;

import java.util.UUID;

public record CloseOrderResponse(
        UUID orderId,
        String status,
        boolean finalPaymentAcknowledged
) {
}
