package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.OrderStatus;

import java.time.LocalDate;
import java.util.UUID;

public record DeliverOrderResult(
        UUID orderId,
        OrderStatus status,
        LocalDate actualDeliveryDate
) {
}
