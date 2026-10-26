package com.magyen.platform.plotter.application.dto;

import java.util.UUID;

/**
 * Máquina de Plotter disponible para selección y reportes.
 */
public record PlotterMachineResult(
        UUID plotterMachineId,
        String name,
        boolean active,
        boolean defaultForRegistration
) {
}
