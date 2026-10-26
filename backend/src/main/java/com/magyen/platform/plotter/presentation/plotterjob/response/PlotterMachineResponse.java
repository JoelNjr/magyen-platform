package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.util.UUID;

/**
 * Máquina de Plotter expuesta a la interfaz.
 */
public record PlotterMachineResponse(
        UUID plotterMachineId,
        String name,
        boolean active,
        boolean defaultForRegistration
) {
}
