package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Consulta de costos de papel de trabajos internos atribuibles a una Orden comercial.
 */
public record GetInternalPlotterOrderCostsQuery(UUID orderId) {
}
