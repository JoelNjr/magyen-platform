package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollEmployeeSalesParticipationTest {

    @Test
    void fixedPayrollIsNotASellerUntilDesignated() {
        PayrollEmployee employee = PayrollEmployee.createFixed(
                "Empleado fijo",
                FinancialAmount.of(new BigDecimal("1500000.00")),
                LocalDate.of(2026, 8, 1),
                null
        );

        assertFalse(employee.canSell());
        assertFalse(employee.isEligibleAsSeller());
        assertFalse(employee.isSalesParticipant());

        employee.changeSalesParticipation(true);
        assertTrue(employee.canSell());
        assertTrue(employee.isEligibleAsSeller());

        employee.deactivate();
        assertTrue(employee.canSell());
        assertFalse(employee.isEligibleAsSeller());
    }

    @Test
    void productionBasedEmployeeCannotBeASalesParticipant() {
        PayrollEmployee operator = PayrollEmployee.createProductionBased("Operario");
        assertFalse(operator.canSell());
        assertThrows(FinanceDomainException.class, () -> operator.changeSalesParticipation(true));
    }
}
