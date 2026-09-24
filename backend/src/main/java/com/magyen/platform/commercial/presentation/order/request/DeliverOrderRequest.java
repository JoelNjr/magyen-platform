package com.magyen.platform.commercial.presentation.order.request;

import java.time.LocalDate;

public record DeliverOrderRequest(
        LocalDate deliveryDate
) {
}
