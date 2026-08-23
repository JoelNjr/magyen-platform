package com.magyen.platform.commercial.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SellerCommissionPolicyTest {

    @Test
    void allOrderStatusesAreEligible() {
        assertTrue(SellerCommissionPolicy.includes(OrderStatus.CONFIRMED));
        assertTrue(SellerCommissionPolicy.includes(OrderStatus.IN_PRODUCTION));
        assertTrue(SellerCommissionPolicy.includes(OrderStatus.READY_FOR_DELIVERY));
        assertTrue(SellerCommissionPolicy.includes(OrderStatus.DELIVERED));
        assertTrue(SellerCommissionPolicy.includes(OrderStatus.CLOSED));
        assertFalse(SellerCommissionPolicy.includes(null));
    }

    @Test
    void commissionIsFivePercentRoundedHalfUpToScaleTwo() {
        assertEquals(new BigDecimal("5.00"), SellerCommissionPolicy.commissionOnSales(new BigDecimal("100.00")));
        assertEquals(new BigDecimal("25000.00"), SellerCommissionPolicy.commissionOnSales(new BigDecimal("500000.00")));
        assertEquals(new BigDecimal("1.67"), SellerCommissionPolicy.commissionOnSales(new BigDecimal("33.33")));
        assertEquals(new BigDecimal("0.00"), SellerCommissionPolicy.commissionOnSales(BigDecimal.ZERO));
    }
}
