package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada para actualizar cantidad y precio unitario de un OrderItem.
 */
public record UpdateOrderItemCommand(
        UUID orderId,
        UUID itemId,
        int quantity,
        BigDecimal unitPrice
) {
}
