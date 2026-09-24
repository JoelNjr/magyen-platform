package com.magyen.platform.commercial.presentation.order.response;

import java.util.UUID;

public record StartOrderProductionResponse(
        UUID orderId,
        String status
) {
}
