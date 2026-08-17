package com.magyen.platform.plotter.application.port;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Vista mínima de una Orden comercial para atribución de Plotter.
 */
public record PlotterCommercialOrderView(
        UUID orderId,
        String orderNumber,
        String description,
        UUID customerId,
        String customerName,
        LocalDate confirmationDate,
        LocalDate deliveryDate
) {
}
