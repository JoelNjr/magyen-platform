package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Pedidos entregados o cerrados elegibles para un trabajo INTERNAL_MAGYEN atrasado.
 */
public record GetHistoricalCommercialOrdersForPlotterResponse(
        List<HistoricalCommercialOrderResponse> orders
) {
    public record HistoricalCommercialOrderResponse(
            UUID orderId,
            String orderNumber,
            String description,
            UUID customerId,
            String customerName,
            LocalDate confirmationDate,
            LocalDate deliveryDate,
            String status
    ) {
    }
}
