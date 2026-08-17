package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetEmployeeProductionEarningsQuery;
import com.magyen.platform.production.application.dto.GetEmployeeProductionEarningsResult;
import com.magyen.platform.production.application.port.ProductionLaborEarningsQuery;
import com.magyen.platform.production.domain.ProductionLaborWork;
import com.magyen.platform.production.domain.ProductionLaborWorkStatus;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Acumula mano de obra histórica de un empleado. No calcula nómina, impuestos ni comisiones.
 */
public class GetEmployeeProductionEarningsUseCase {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO_QUANTITY = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);

    private final ProductionLaborEarningsQuery productionLaborEarningsQuery;

    public GetEmployeeProductionEarningsUseCase(ProductionLaborEarningsQuery productionLaborEarningsQuery) {
        this.productionLaborEarningsQuery = Objects.requireNonNull(
                productionLaborEarningsQuery,
                "Production labor earnings query must not be null"
        );
    }

    @Transactional(readOnly = true)
    public GetEmployeeProductionEarningsResult execute(GetEmployeeProductionEarningsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.employeeId(), "Employee id must not be null");
        Objects.requireNonNull(query.fromDate(), "From date must not be null");
        Objects.requireNonNull(query.toDate(), "To date must not be null");
        if (query.toDate().isBefore(query.fromDate())) {
            throw new ProductionDomainException("Earnings to date must not be before from date");
        }

        List<ProductionLaborWork> works = productionLaborEarningsQuery.findByEmployeeAndWorkDateBetween(
                query.employeeId(),
                query.fromDate(),
                query.toDate()
        );

        int laborWorkCount = 0;
        BigDecimal totalQuantity = ZERO_QUANTITY;
        BigDecimal totalCalculatedAmount = ZERO_MONEY;
        BigDecimal totalPaidAmount = ZERO_MONEY;
        BigDecimal totalPendingAmount = ZERO_MONEY;

        for (ProductionLaborWork work : works) {
            if (work.getStatus() == ProductionLaborWorkStatus.CANCELLED) {
                continue;
            }
            laborWorkCount++;
            totalQuantity = totalQuantity.add(work.getQuantity());
            totalCalculatedAmount = totalCalculatedAmount.add(work.getCalculatedAmount());
            if (work.getStatus() == ProductionLaborWorkStatus.PAID) {
                totalPaidAmount = totalPaidAmount.add(work.getCalculatedAmount());
            } else if (work.getStatus() == ProductionLaborWorkStatus.PENDING) {
                totalPendingAmount = totalPendingAmount.add(work.getCalculatedAmount());
            }
        }

        return new GetEmployeeProductionEarningsResult(
                query.employeeId(),
                query.fromDate(),
                query.toDate(),
                laborWorkCount,
                totalQuantity,
                totalCalculatedAmount.setScale(2, RoundingMode.HALF_UP),
                totalPaidAmount.setScale(2, RoundingMode.HALF_UP),
                totalPendingAmount.setScale(2, RoundingMode.HALF_UP)
        );
    }
}
