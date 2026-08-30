package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuotationDiscountTest {

    @Test
    void appliesDiscountOnFinalTotalWithoutChangingUnitPrices() {
        Quotation quotation = draftWithItem();
        Money originalUnitPrice = quotation.getItems().getFirst().getUnitPrice();

        quotation.applyDiscount(Money.of(new BigDecimal("100000.00")));

        assertEquals(new BigDecimal("1000000.00"), quotation.getSubtotal().getAmount());
        assertEquals(new BigDecimal("100000.00"), quotation.getDiscount().getAmount());
        assertEquals(new BigDecimal("900000.00"), quotation.getTotal().getAmount());
        assertEquals(originalUnitPrice, quotation.getItems().getFirst().getUnitPrice());
    }

    @Test
    void rejectsNegativeDiscount() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("-1.00")));
    }

    @Test
    void rejectsDiscountGreaterThanSubtotal() {
        Quotation quotation = draftWithItem();
        assertThrows(
                QuotationDomainException.class,
                () -> quotation.applyDiscount(Money.of(new BigDecimal("1000001.00")))
        );
    }

    @Test
    void emptyQuotationOnlyAllowsZeroDiscount() {
        Quotation quotation = Quotation.create(
                QuotationNumber.of(1),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                UUID.randomUUID(),
                null
        );
        quotation.applyDiscount(Money.zero());
        assertEquals(Money.zero(), quotation.getTotal());
        assertThrows(
                QuotationDomainException.class,
                () -> quotation.applyDiscount(Money.of(new BigDecimal("1.00")))
        );
    }

    @Test
    void approvedQuotationCannotReceiveDiscount() {
        Quotation quotation = draftWithItem();
        quotation.approve();
        assertThrows(
                QuotationDomainException.class,
                () -> quotation.applyDiscount(Money.of(new BigDecimal("10000.00")))
        );
    }

    @Test
    void removingItemsCannotLeaveDiscountGreaterThanSubtotal() {
        Quotation quotation = draftWithItem();
        quotation.applyDiscount(Money.of(new BigDecimal("100000.00")));
        UUID itemId = quotation.getItems().getFirst().getId();
        assertThrows(QuotationDomainException.class, () -> quotation.removeItem(itemId));
    }

    @Test
    void fullDiscountPreventsApprovalBecauseTotalMustBePositive() {
        Quotation quotation = draftWithItem();
        quotation.applyDiscount(Money.of(new BigDecimal("1000000.00")));
        assertThrows(QuotationDomainException.class, quotation::approve);
    }

    private static Quotation draftWithItem() {
        Quotation quotation = Quotation.create(
                QuotationNumber.of(14),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                UUID.randomUUID(),
                null
        );
        quotation.addItem(
                "Camiseta",
                10,
                "Algodón",
                "Blanco",
                Money.of(new BigDecimal("100000.00"))
        );
        return quotation;
    }
}
