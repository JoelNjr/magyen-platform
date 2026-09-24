package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SellerCommissionPolicyTest {

    @Test
    void commissionUsesOrderTotalAtFivePercentHalfUpScaleTwo() {
        assertEquals(new BigDecimal("5.00"), SellerCommissionPolicy.commissionForOrder(new BigDecimal("100.00")));
        assertEquals(new BigDecimal("25000.00"), SellerCommissionPolicy.commissionForOrder(new BigDecimal("500000.00")));
        assertEquals(new BigDecimal("1.67"), SellerCommissionPolicy.commissionForOrder(new BigDecimal("33.33")));
        assertEquals(new BigDecimal("1.67"), SellerCommissionPolicy.commissionOnSales(new BigDecimal("33.33")));
        assertEquals(new BigDecimal("0.00"), SellerCommissionPolicy.commissionForOrder(BigDecimal.ZERO));
        assertEquals(new BigDecimal("0.00"), SellerCommissionPolicy.commissionForOrder(null));
    }

    @Test
    void confirmationMonthIsInclusiveAndDoesNotSpillIntoTheNextMonth() {
        LocalDate septemberStart = LocalDate.of(2026, 9, 1);
        LocalDate septemberEnd = LocalDate.of(2026, 9, 30);
        LocalDate octoberStart = LocalDate.of(2026, 10, 1);
        LocalDate octoberEnd = LocalDate.of(2026, 10, 31);

        assertTrue(SellerCommissionPolicy.confirmationDateInRange(
                LocalDate.of(2026, 9, 1), septemberStart, septemberEnd
        ));
        assertTrue(SellerCommissionPolicy.confirmationDateInRange(
                LocalDate.of(2026, 9, 30), septemberStart, septemberEnd
        ));
        assertFalse(SellerCommissionPolicy.confirmationDateInRange(
                LocalDate.of(2026, 9, 30), octoberStart, octoberEnd
        ));
        assertFalse(SellerCommissionPolicy.confirmationDateInRange(
                LocalDate.of(2026, 8, 31), septemberStart, septemberEnd
        ));
    }

    @Test
    void periodMustBeExactlyOneCalendarMonth() {
        SellerCommissionPolicy.requireCalendarMonth(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));
        assertThrows(OrderDomainException.class, () ->
                SellerCommissionPolicy.requireCalendarMonth(null, null));
        assertThrows(OrderDomainException.class, () ->
                SellerCommissionPolicy.requireCalendarMonth(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31)));
        assertThrows(OrderDomainException.class, () ->
                SellerCommissionPolicy.requireCalendarMonth(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 30)));
    }

    @Test
    void augustIsHistoricalAndSeptemberIsCalculated() {
        assertEquals(
                SellerCommissionReadStatus.HISTORICAL,
                SellerCommissionPolicy.readStatus(LocalDate.of(2026, 8, 1))
        );
        assertEquals(
                SellerCommissionReadStatus.CALCULATED,
                SellerCommissionPolicy.readStatus(LocalDate.of(2026, 9, 1))
        );
    }
}
