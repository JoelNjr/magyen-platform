package com.magyen.platform.plotter.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de salida de Plotter hacia Inventory para validar rollos y consumir metros.
 * <p>
 * Plotter no conoce persistencia Inventory.
 */
public interface PlotterJobInventoryPort {

    /**
     * Valida que el material sea un rollo de papel Plotter elegible y retorna su estado.
     */
    PlotterPaperRollView requirePlotterPaperRoll(UUID paperInventoryItemId);

    /**
     * Descuenta metros del rollo mediante OUT con
     * {@code sourceType = PLOTTER} y {@code sourceId = plotterJobId}.
     * <p>
     * Idempotente por plotterJobId.
     */
    PlotterJobInventoryConsumeResult consumePaperMeters(
            UUID paperInventoryItemId,
            BigDecimal printedMeters,
            UUID plotterJobId,
            String observation
    );
}
