package com.magyen.platform.plotter.infrastructure.persistence.repository;

import com.magyen.platform.plotter.infrastructure.persistence.entity.PlotterJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio Spring Data para {@link PlotterJobEntity}.
 */
public interface SpringDataPlotterJobRepository extends JpaRepository<PlotterJobEntity, UUID> {
}
