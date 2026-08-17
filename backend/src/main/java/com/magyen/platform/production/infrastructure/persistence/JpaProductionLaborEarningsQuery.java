package com.magyen.platform.production.infrastructure.persistence;

import com.magyen.platform.production.application.port.ProductionLaborEarningsQuery;
import com.magyen.platform.production.domain.ProductionLaborWork;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionLaborWorkEntity;
import com.magyen.platform.production.infrastructure.persistence.repository.SpringDataProductionLaborWorkQueryJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Lectura de mano de obra por empleado desde {@code production_labor_work}.
 */
@Component
public class JpaProductionLaborEarningsQuery implements ProductionLaborEarningsQuery {

    private final SpringDataProductionLaborWorkQueryJpaRepository springDataRepository;

    public JpaProductionLaborEarningsQuery(
            SpringDataProductionLaborWorkQueryJpaRepository springDataRepository
    ) {
        this.springDataRepository = Objects.requireNonNull(
                springDataRepository,
                "Production labor work query repository must not be null"
        );
    }

    @Override
    public List<ProductionLaborWork> findByEmployeeAndWorkDateBetween(
            UUID employeeId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");

        return springDataRepository
                .findByOperatorEmployeeIdAndWorkDateBetweenOrderByWorkDateAscIdAsc(
                        employeeId,
                        fromDate,
                        toDate
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ProductionLaborWork toDomain(ProductionLaborWorkEntity entity) {
        UUID productionOrderId = entity.getProductionOrder() == null
                ? null
                : entity.getProductionOrder().getId();

        return ProductionLaborWork.reconstitute(
                entity.getId(),
                productionOrderId,
                entity.getOperatorEmployeeId(),
                entity.getWorkDate(),
                entity.getOperation(),
                entity.getQuantity(),
                entity.getUnitOfMeasure(),
                entity.getUnitRate(),
                entity.getCalculatedAmount(),
                entity.getObservation(),
                entity.getStatus(),
                entity.getPaidAt(),
                entity.getFinancialTransactionId()
        );
    }
}
