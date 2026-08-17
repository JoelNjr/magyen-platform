package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CancelPayrollDeductionResult;
import com.magyen.platform.finance.application.dto.CreatePayrollDeductionResult;
import com.magyen.platform.finance.application.dto.PayrollDeductionResult;
import com.magyen.platform.finance.domain.PayrollDeduction;

/**
 * Traduce el agregado de descuento a resultados de aplicación.
 */
final class PayrollDeductionReadMapper {

    private PayrollDeductionReadMapper() {
    }

    static PayrollDeductionResult toResult(PayrollDeduction deduction) {
        return new PayrollDeductionResult(
                deduction.getId(),
                deduction.getEmployeeId(),
                deduction.getType(),
                deduction.getAmount().getValue(),
                deduction.getDeductionDate(),
                deduction.getDescription(),
                deduction.getStatus(),
                deduction.getCreatedAt()
        );
    }

    static CreatePayrollDeductionResult toCreateResult(PayrollDeduction deduction) {
        return new CreatePayrollDeductionResult(
                deduction.getId(),
                deduction.getEmployeeId(),
                deduction.getType(),
                deduction.getAmount().getValue(),
                deduction.getDeductionDate(),
                deduction.getDescription(),
                deduction.getStatus(),
                deduction.getCreatedAt()
        );
    }

    static CancelPayrollDeductionResult toCancelResult(PayrollDeduction deduction) {
        return new CancelPayrollDeductionResult(
                deduction.getId(),
                deduction.getEmployeeId(),
                deduction.getStatus()
        );
    }
}
