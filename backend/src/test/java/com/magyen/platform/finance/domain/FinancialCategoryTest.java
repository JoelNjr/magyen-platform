package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialCategoryTest {

    @Test
    void recognizesKnownIncomeAndExpenseCodes() {
        assertEquals(FinancialTransactionType.INCOME, FinancialCategory.SALES.getTransactionType());
        assertEquals(FinancialTransactionType.EXPENSE, FinancialCategory.SERVICES.getTransactionType());
        assertEquals(FinancialCategory.PLOTTER_REVENUE, FinancialCategory.of("plotter_revenue"));
    }

    @Test
    void tryParseLeavesFreeTextHistoricalCategoriesUnreadAsEnum() {
        assertTrue(FinancialCategory.tryParse("Servicios").isEmpty());
        assertTrue(FinancialCategory.tryParse("MATERIALS").isPresent());
        assertThrows(FinanceDomainException.class, () -> FinancialCategory.of("Servicios"));
    }
}
