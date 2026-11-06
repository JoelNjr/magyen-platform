package com.magyen.platform.plotter.domain.exception;

/**
 * La clave de registro ya identifica otro pago y no puede reutilizarse.
 */
public class PlotterRegistrationConflictException extends RuntimeException {

    public PlotterRegistrationConflictException(String message) {
        super(message);
    }
}
