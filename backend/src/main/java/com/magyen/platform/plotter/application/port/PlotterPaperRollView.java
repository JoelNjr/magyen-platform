package com.magyen.platform.plotter.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Vista mínima de un rollo de papel Plotter para validación en Plotter.
 */
public record PlotterPaperRollView(
        UUID inventoryItemId,
        String paperRollNumber,
        BigDecimal availableMeters,
        String unitOfMeasure
) {
}
