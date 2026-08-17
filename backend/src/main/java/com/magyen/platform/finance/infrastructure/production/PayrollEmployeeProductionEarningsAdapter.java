package com.magyen.platform.finance.infrastructure.production;

import com.magyen.platform.finance.application.port.EmployeeProductionEarningsPort;
import com.magyen.platform.production.application.dto.GetEmployeeProductionEarningsQuery;
import com.magyen.platform.production.application.dto.GetEmployeeProductionEarningsResult;
import com.magyen.platform.production.application.usecase.GetEmployeeProductionEarningsUseCase;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Finance → Production para acumulación de mano de obra.
 */
public class PayrollEmployeeProductionEarningsAdapter implements EmployeeProductionEarningsPort {

    private final GetEmployeeProductionEarningsUseCase getEmployeeProductionEarningsUseCase;

    public PayrollEmployeeProductionEarningsAdapter(
            GetEmployeeProductionEarningsUseCase getEmployeeProductionEarningsUseCase
    ) {
        this.getEmployeeProductionEarningsUseCase = Objects.requireNonNull(
                getEmployeeProductionEarningsUseCase,
                "Get employee production earnings use case must not be null"
        );
    }

    @Override
    public EmployeeProductionEarningsSnapshot findEarnings(
            UUID employeeId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        GetEmployeeProductionEarningsResult result = getEmployeeProductionEarningsUseCase.execute(
                new GetEmployeeProductionEarningsQuery(employeeId, fromDate, toDate)
        );

        return new EmployeeProductionEarningsSnapshot(
                result.employeeId(),
                result.fromDate(),
                result.toDate(),
                result.laborWorkCount(),
                result.totalQuantity(),
                result.totalCalculatedAmount(),
                result.totalPaidAmount(),
                result.totalPendingAmount()
        );
    }
}
