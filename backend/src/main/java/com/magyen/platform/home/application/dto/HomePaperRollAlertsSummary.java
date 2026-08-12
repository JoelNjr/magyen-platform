package com.magyen.platform.home.application.dto;

import java.util.List;

/**
 * Sección de alertas de rollos de papel Plotter del Dashboard Home.
 */
public record HomePaperRollAlertsSummary(
        int lowStockCount,
        List<HomePaperRollAlertItem> items
) {
}
