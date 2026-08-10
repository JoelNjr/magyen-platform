package com.magyen.platform.plotter.domain.exception;

/**
 * Excepción base para violaciones de reglas de negocio del módulo de plotter.
 */
public class PlotterDomainException extends RuntimeException {

    public PlotterDomainException(String message) {
        super(message);
    }
}
