package com.magyen.platform.home.presentation.dashboard.response;

import java.util.List;

/**
 * Sección HTTP de alertas de rollos de papel Plotter.
 */
public record HomePaperRollAlertsResponse(
        int lowStockCount,
        List<HomePaperRollAlertItemResponse> items
) {
}
