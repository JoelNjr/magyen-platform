package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Trazabilidad HTTP de un trabajo interno hacia el pedido.
 */
public record PlotterInternalOrderCostItemResponse(
        UUID plotterJobId,
        LocalDate jobDate,
        BigDecimal printedMeters,
        BigDecimal paperCost,
        boolean paperCostValued,
        UUID orderId,
        String orderNumber,
        String orderDescription,
        String customerName
) {
}
