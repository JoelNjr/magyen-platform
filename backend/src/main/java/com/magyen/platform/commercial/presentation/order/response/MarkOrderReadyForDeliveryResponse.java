package com.magyen.platform.commercial.presentation.order.response;

import java.util.UUID;

public record MarkOrderReadyForDeliveryResponse(
        UUID orderId,
        String status
) {
}
