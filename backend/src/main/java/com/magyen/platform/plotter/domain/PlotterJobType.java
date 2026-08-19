package com.magyen.platform.plotter.domain;

import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

/**
 * Modo de negocio de un trabajo de Plotter.
 * <p>
 * {@code INTERNAL_MAGYEN} es un servicio interno Magyen (valor por metro variable).
 * {@code EXTERNAL} es un servicio de impresión a un cliente externo.
 * {@code WASTE} es merma operativa (muestras, pruebas, errores) sin ingreso ni cobro.
 */
public enum PlotterJobType {
    INTERNAL_MAGYEN,
    EXTERNAL,
    WASTE;

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

    public boolean isWaste() {
        return this == WASTE;
    }
}
