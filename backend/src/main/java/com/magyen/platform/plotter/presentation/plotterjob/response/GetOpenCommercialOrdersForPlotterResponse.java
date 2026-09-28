package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.util.List;
import java.util.UUID;

/**
 * Órdenes abiertas que pueden recibir un trabajo INTERNAL_MAGYEN.
 */
public record GetOpenCommercialOrdersForPlotterResponse(
        List<OpenCommercialOrderResponse> orders
) {
    public record OpenCommercialOrderResponse(
            UUID orderId,
            String orderNumber,
            String description,
            UUID customerId,
            String customerName,
            String status
    ) {
    }
}
