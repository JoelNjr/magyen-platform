package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Fila de trazabilidad de un trabajo INTERNAL_MAGYEN hacia el pedido comercial.
 */
public record PlotterInternalOrderCostItem(
        UUID plotterJobId,
        LocalDate jobDate,
        BigDecimal printedMeters,
        BigDecimal paperCost,
        boolean paperCostValued,
        BigDecimal serviceValue,
        UUID orderId,
        String orderNumber,
        String orderDescription,
        String customerName
) {
}
