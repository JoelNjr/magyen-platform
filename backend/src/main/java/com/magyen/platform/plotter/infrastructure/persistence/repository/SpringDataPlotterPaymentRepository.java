package com.magyen.platform.plotter.infrastructure.persistence.repository;

import com.magyen.platform.plotter.infrastructure.persistence.entity.PlotterPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para pagos de Plotter.
 */
public interface SpringDataPlotterPaymentRepository extends JpaRepository<PlotterPaymentEntity, UUID> {

    List<PlotterPaymentEntity> findByPlotterJobIdOrderByPaymentDateDescIdDesc(UUID plotterJobId);
}
