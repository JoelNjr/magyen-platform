package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.UpdatePayrollEmployeeCompensationResult;
import com.magyen.platform.finance.domain.PayrollEmployee;

/**
 * Traduce el agregado de dominio a resultados de aplicación.
 */
final class PayrollEmployeeReadMapper {

    private PayrollEmployeeReadMapper() {
    }

    static GetPayrollEmployeeResult toGetResult(PayrollEmployee employee) {
        return new GetPayrollEmployeeResult(
                employee.getId(),
                employee.getDisplayName(),
                employee.isActive(),
                employee.getCompensationType(),
                employee.getFixedAmount() == null ? null : employee.getFixedAmount().getValue(),
                employee.getFrequency(),
                employee.getEffectiveFrom(),
                employee.getEffectiveTo()
        );
    }

    static CreatePayrollEmployeeResult toCreateResult(PayrollEmployee employee) {
        return new CreatePayrollEmployeeResult(
                employee.getId(),
                employee.getDisplayName(),
                employee.isActive(),
                employee.getCompensationType(),
                employee.getFixedAmount() == null ? null : employee.getFixedAmount().getValue(),
                employee.getFrequency(),
                employee.getEffectiveFrom(),
                employee.getEffectiveTo()
        );
    }

    static UpdatePayrollEmployeeCompensationResult toUpdateResult(PayrollEmployee employee) {
        return new UpdatePayrollEmployeeCompensationResult(
                employee.getId(),
                employee.getDisplayName(),
                employee.isActive(),
                employee.getCompensationType(),
                employee.getFixedAmount() == null ? null : employee.getFixedAmount().getValue(),
                employee.getFrequency(),
                employee.getEffectiveFrom(),
                employee.getEffectiveTo()
        );
    }
}
