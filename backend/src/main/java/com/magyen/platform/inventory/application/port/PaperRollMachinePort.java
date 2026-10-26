package com.magyen.platform.inventory.application.port;

import java.util.UUID;

/**
 * Valida una máquina de Plotter antes de fijarla en un rollo de papel.
 * <p>
 * Inventory no conoce el agregado de Plotter. El adaptador vive en Plotter.
 */
public interface PaperRollMachinePort {

    UUID requireActiveMachine(UUID machineId);

    UUID requireDefaultActiveMachine();
}
