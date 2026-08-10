package com.magyen.platform.inventory.domain;

/**
 * Port para generar el siguiente número operacional de rollo de papel Plotter.
 */
public interface PaperRollNumberGenerator {

    /**
     * Obtiene el siguiente número con formato {@code RP-%03d}.
     * Puede dejar huecos si una transacción falla tras reservar la secuencia.
     */
    String nextPaperRollNumber();
}
