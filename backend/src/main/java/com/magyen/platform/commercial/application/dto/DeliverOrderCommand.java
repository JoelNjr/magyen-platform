package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DeliverOrderCommand(
        UUID orderId,
        LocalDate deliveryDate
) {
}
