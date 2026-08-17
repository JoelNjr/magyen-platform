package com.magyen.platform.commercial.infrastructure.finance;

import com.magyen.platform.commercial.application.port.CommercialSellerEmployeeInfo;
import com.magyen.platform.commercial.application.port.CommercialSellerEmployeePort;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeesQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeesResult;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeesUseCase;
import com.magyen.platform.finance.domain.PayrollCompensationType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador Commercial → Finance para elegibilidad de vendedores FIXED_PAYROLL.
 */
public class CommercialSellerEmployeeAdapter implements CommercialSellerEmployeePort {

    private final GetPayrollEmployeeUseCase getPayrollEmployeeUseCase;
    private final GetPayrollEmployeesUseCase getPayrollEmployeesUseCase;

    public CommercialSellerEmployeeAdapter(
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
    public CommercialSellerEmployeeInfo requireEligibleSeller(UUID sellerEmployeeId) {
        Objects.requireNonNull(sellerEmployeeId, "Seller employee id must not be null");

        GetPayrollEmployeeResult employee;
        try {
            employee = getPayrollEmployeeUseCase.execute(new GetPayrollEmployeeQuery(sellerEmployeeId));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Seller employee not found: " + sellerEmployeeId);
        }

        if (!employee.active() || employee.compensationType() != PayrollCompensationType.FIXED_PAYROLL) {
            throw new IllegalArgumentException(
                    "Only active FIXED_PAYROLL employees can be selected as seller"
            );
        }

        return new CommercialSellerEmployeeInfo(
                employee.employeeId(),
                employee.displayName(),
                employee.active()
        );
    }

    @Override
    public List<CommercialSellerEmployeeInfo> listActiveFixedSellers() {
        GetPayrollEmployeesResult result = getPayrollEmployeesUseCase.execute(
                new GetPayrollEmployeesQuery(true)
        );

        return result.employees().stream()
                .filter(GetPayrollEmployeeResult::active)
                .filter(employee -> employee.compensationType() == PayrollCompensationType.FIXED_PAYROLL)
                .map(employee -> new CommercialSellerEmployeeInfo(
                        employee.employeeId(),
                        employee.displayName(),
                        employee.active()
                ))
                .toList();
    }

    @Override
    public Optional<String> findEmployeeDisplayName(UUID sellerEmployeeId) {
        Objects.requireNonNull(sellerEmployeeId, "Seller employee id must not be null");
        try {
            GetPayrollEmployeeResult employee = getPayrollEmployeeUseCase.execute(
                    new GetPayrollEmployeeQuery(sellerEmployeeId)
            );
            return Optional.ofNullable(employee.displayName());
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Map<UUID, String> findEmployeeDisplayNames(Collection<UUID> sellerEmployeeIds) {
        if (sellerEmployeeIds == null || sellerEmployeeIds.isEmpty()) {
            return Map.of();
        }

        GetPayrollEmployeesResult result = getPayrollEmployeesUseCase.execute(GetPayrollEmployeesQuery.all());
        return result.employees().stream()
                .filter(employee -> sellerEmployeeIds.contains(employee.employeeId()))
                .collect(Collectors.toMap(
                        GetPayrollEmployeeResult::employeeId,
                        GetPayrollEmployeeResult::displayName,
                        (left, right) -> left
                ));
    }
}
