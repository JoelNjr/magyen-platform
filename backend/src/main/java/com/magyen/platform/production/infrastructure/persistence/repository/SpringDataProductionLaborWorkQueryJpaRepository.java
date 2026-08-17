package com.magyen.platform.production.infrastructure.persistence.repository;

import com.magyen.platform.production.infrastructure.persistence.entity.ProductionLaborWorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Lectura de {@code production_labor_work} por empleado. No se usa para escribir el agregado.
 */
public interface SpringDataProductionLaborWorkQueryJpaRepository
        extends JpaRepository<ProductionLaborWorkEntity, UUID> {

    List<ProductionLaborWorkEntity> findByOperatorEmployeeIdAndWorkDateBetweenOrderByWorkDateAscIdAsc(
            UUID operatorEmployeeId,
            LocalDate fromDate,
            LocalDate toDate
    );
}
