package com.magyen.platform.plotter.application.port;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Vista mínima de una Orden comercial para atribución de Plotter.
 */
public record PlotterCommercialOrderView(
        UUID orderId,
        String orderNumber,
        UUID customerId,
        LocalDate confirmationDate,
        LocalDate deliveryDate
) {
}
