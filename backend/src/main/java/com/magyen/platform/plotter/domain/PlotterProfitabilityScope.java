package com.magyen.platform.plotter.domain;

import java.util.Locale;

/**
 * Alcance analítico de rentabilidad de Plotter. No es un estado persistido.
 */
public enum PlotterProfitabilityScope {
    ALL,
    EXTERNAL,
    INTERNAL,
    WASTE;

    public static PlotterProfitabilityScope of(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ALL", "TODOS" -> ALL;
            case "EXTERNAL", "EXTERNOS" -> EXTERNAL;
            case "INTERNAL", "INTERNOS", "INTERNAL_MAGYEN" -> INTERNAL;
            case "WASTE", "MERMA", "MERMAS" -> WASTE;
            default -> throw new IllegalArgumentException("Unsupported plotter profitability scope: " + value.trim());
        };
    }
}
