package com.magyen.platform.plotter.infrastructure.persistence.repository;

import com.magyen.platform.plotter.infrastructure.persistence.entity.PlotterMachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para {@link PlotterMachineEntity}.
 */
public interface SpringDataPlotterMachineRepository extends JpaRepository<PlotterMachineEntity, UUID> {

    Optional<PlotterMachineEntity> findFirstByDefaultForRegistrationTrueAndActiveTrue();
}
