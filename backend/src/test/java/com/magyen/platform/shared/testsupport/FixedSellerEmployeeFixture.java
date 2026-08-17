package com.magyen.platform.shared.testsupport;

import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.domain.PayrollCompensationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Crea empleados FIXED_PAYROLL para tests que necesitan un vendedor elegible.
 */
public final class FixedSellerEmployeeFixture {

    private FixedSellerEmployeeFixture() {
    }

    public static UUID create(CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase, String displayName) {
        return createPayrollEmployeeUseCase.execute(
                new CreatePayrollEmployeeCommand(
                        displayName,
                        PayrollCompensationType.FIXED_PAYROLL,
                        new BigDecimal("1500000.00"),
                        LocalDate.of(2026, 8, 1),
                        null
                )
        ).employeeId();
    }
}
