package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

public record RemoveOrderItemCommand(
        UUID orderId,
        UUID itemId
) {
}
