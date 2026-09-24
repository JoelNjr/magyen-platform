package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.OrderStatus;

import java.util.UUID;

public record StartOrderProductionResult(
        UUID orderId,
        OrderStatus status
) {
}
