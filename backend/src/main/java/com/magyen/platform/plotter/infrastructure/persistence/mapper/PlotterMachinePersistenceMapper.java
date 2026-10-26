package com.magyen.platform.plotter.infrastructure.persistence.mapper;

import com.magyen.platform.plotter.domain.PlotterMachine;
import com.magyen.platform.plotter.infrastructure.persistence.entity.PlotterMachineEntity;

import java.util.Objects;

/**
 * Traduce entre la máquina de dominio y su entidad JPA.
 */
public class PlotterMachinePersistenceMapper {

    public PlotterMachineEntity toEntity(PlotterMachine plotterMachine) {
        Objects.requireNonNull(plotterMachine, "Plotter machine must not be null");

        PlotterMachineEntity entity = new PlotterMachineEntity();
        entity.setId(plotterMachine.getId());
        entity.setName(plotterMachine.getName());
        entity.setActive(plotterMachine.isActive());
        entity.setDefaultForRegistration(plotterMachine.isDefaultForRegistration());
        return entity;
    }

    public PlotterMachine toDomain(PlotterMachineEntity entity) {
        Objects.requireNonNull(entity, "Plotter machine entity must not be null");
        return PlotterMachine.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.isActive(),
                entity.isDefaultForRegistration()
        );
    }
}
