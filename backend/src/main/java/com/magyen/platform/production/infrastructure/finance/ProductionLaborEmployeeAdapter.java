package com.magyen.platform.production.infrastructure.finance;

import com.magyen.platform.finance.application.dto.GetPayrollEmployeeQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeesQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeesResult;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeesUseCase;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.production.application.port.ProductionLaborEmployeePort;
import com.magyen.platform.production.application.port.ProductionLaborOperatorInfo;
import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador Production → Finance para elegibilidad de operarios PRODUCTION_BASED.
 */
public class ProductionLaborEmployeeAdapter implements ProductionLaborEmployeePort {

    private final GetPayrollEmployeeUseCase getPayrollEmployeeUseCase;
    private final GetPayrollEmployeesUseCase getPayrollEmployeesUseCase;

    public ProductionLaborEmployeeAdapter(
            GetPayrollEmployeeUseCase getPayrollEmployeeUseCase,
            GetPayrollEmployeesUseCase getPayrollEmployeesUseCase
    ) {
        this.getPayrollEmployeeUseCase = Objects.requireNonNull(
                getPayrollEmployeeUseCase,
                "Get payroll employee use case must not be null"
        );
        this.getPayrollEmployeesUseCase = Objects.requireNonNull(
                getPayrollEmployeesUseCase,
                "Get payroll employees use case must not be null"
        );
    }

    @Override
    public ProductionLaborOperatorInfo requireEligibleProductionOperator(UUID operatorEmployeeId) {
        Objects.requireNonNull(operatorEmployeeId, "Operator employee id must not be null");

        GetPayrollEmployeeResult employee;
        try {
            employee = getPayrollEmployeeUseCase.execute(new GetPayrollEmployeeQuery(operatorEmployeeId));
        } catch (IllegalArgumentException exception) {
            throw new ProductionDomainException("Payroll employee not found: " + operatorEmployeeId);
        }

        if (!employee.active()) {
            throw new ProductionDomainException(
                    "Only active PRODUCTION_BASED employees can receive production labor work"
            );
        }
        if (employee.compensationType() != PayrollCompensationType.PRODUCTION_BASED) {
            throw new ProductionDomainException(
                    "Only PRODUCTION_BASED employees can receive production labor work. Actual type: "
                            + employee.compensationType()
            );
        }

        return new ProductionLaborOperatorInfo(employee.employeeId(), employee.displayName());
    }

    @Override
    public List<ProductionLaborOperatorInfo> listActiveProductionBasedOperators() {
        GetPayrollEmployeesResult result = getPayrollEmployeesUseCase.execute(
                new GetPayrollEmployeesQuery(true)
        );

        return result.employees().stream()
                .filter(employee -> employee.active())
                .filter(employee -> employee.compensationType() == PayrollCompensationType.PRODUCTION_BASED)
                .map(employee -> new ProductionLaborOperatorInfo(employee.employeeId(), employee.displayName()))
                .toList();
    }

    @Override
    public Optional<String> findOperatorDisplayName(UUID operatorEmployeeId) {
        Objects.requireNonNull(operatorEmployeeId, "Operator employee id must not be null");
        try {
            GetPayrollEmployeeResult employee = getPayrollEmployeeUseCase.execute(
                    new GetPayrollEmployeeQuery(operatorEmployeeId)
            );
            return Optional.ofNullable(employee.displayName());
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
