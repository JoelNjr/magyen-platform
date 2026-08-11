package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollPeriodResult;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollPeriod;

import java.util.Map;
import java.util.UUID;

/**
 * Traduce períodos de dominio a resultados de aplicación.
 */
final class PayrollPeriodReadMapper {

    private PayrollPeriodReadMapper() {
    }

    static GetPayrollPeriodResult toGetResult(PayrollPeriod period, String employeeDisplayName) {
        return new GetPayrollPeriodResult(
                period.getId(),
                period.getEmployeeId(),
                employeeDisplayName,
                period.getPeriodStart(),
                period.getPeriodEnd(),
                period.getExpectedPaymentDate(),
                period.getAmountSnapshot().getValue(),
                period.getStatus(),
                period.getActualPaymentDate(),
                period.getPaidAt(),
                period.getFinancialTransactionId()
        );
    }

    static GetPayrollPeriodResult toGetResult(
            PayrollPeriod period,
            Map<UUID, PayrollEmployee> employeesById
    ) {
        PayrollEmployee employee = employeesById.get(period.getEmployeeId());
        String displayName = employee != null ? employee.getDisplayName() : "Unknown employee";
        return toGetResult(period, displayName);
    }
}
