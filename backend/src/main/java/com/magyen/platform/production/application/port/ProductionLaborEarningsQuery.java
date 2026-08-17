package com.magyen.platform.production.application.port;

import com.magyen.platform.production.domain.ProductionLaborWork;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Lectura de trabajos de mano de obra por empleado, sin atravesar el agregado de escritura.
 */
public interface ProductionLaborEarningsQuery {

    List<ProductionLaborWork> findByEmployeeAndWorkDateBetween(
            UUID employeeId,
            LocalDate fromDate,
            LocalDate toDate
    );
}
