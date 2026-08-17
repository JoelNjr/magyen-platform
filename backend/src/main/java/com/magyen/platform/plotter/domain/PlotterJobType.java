package com.magyen.platform.plotter.domain;

import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

/**
 * Modo de negocio de un trabajo de Plotter.
 * <p>
 * {@code INTERNAL_MAGYEN} es una operación de material de producción, no una venta.
 * {@code EXTERNAL} es un servicio de impresión a un cliente externo.
 */
public enum PlotterJobType {
    INTERNAL_MAGYEN,
    EXTERNAL;

    public static PlotterJobType of(String value) {
        if (value == null || value.isBlank()) {
            throw new PlotterDomainException("Plotter job type must not be blank");
        }
        try {
            return PlotterJobType.valueOf(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new PlotterDomainException("Unsupported plotter job type: " + value);
        }
    }

    public boolean isInternal() {
        return this == INTERNAL_MAGYEN;
    }

    public boolean isExternal() {
        return this == EXTERNAL;
    }
}
