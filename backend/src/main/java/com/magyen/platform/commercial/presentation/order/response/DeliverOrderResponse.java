package com.magyen.platform.commercial.presentation.order.response;

import java.time.LocalDate;
import java.util.UUID;

public record DeliverOrderResponse(
        UUID orderId,
        String status,
        LocalDate actualDeliveryDate
) {
}
