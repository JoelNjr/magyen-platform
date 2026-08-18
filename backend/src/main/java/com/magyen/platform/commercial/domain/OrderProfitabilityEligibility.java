package com.magyen.platform.commercial.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Órdenes que entran al resumen y a la vista individual de rentabilidad V1.
 * <p>
 * Conserva la semántica de Home: CONFIRMED, IN_PRODUCTION, READY_FOR_DELIVERY, DELIVERED.
 * CLOSED queda fuera. No existe DRAFT en {@link OrderStatus}.
 */
public final class OrderProfitabilityEligibility {

    public static final Set<OrderStatus> STATUSES = EnumSet.of(
            OrderStatus.CONFIRMED,
            OrderStatus.IN_PRODUCTION,
            OrderStatus.READY_FOR_DELIVERY,
            OrderStatus.DELIVERED
    );

    private OrderProfitabilityEligibility() {
    }

    public static boolean includes(OrderStatus status) {
        return status != null && STATUSES.contains(status);
    }
}
