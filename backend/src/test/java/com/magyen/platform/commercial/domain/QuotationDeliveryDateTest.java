package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuotationDeliveryDateTest {

    @Test
    void updatesDeliveryDateWithoutChangingStatusOrTotal() {
        Quotation quotation = quotation();
        quotation.addItem("Camiseta", 2, "Hydrotech", "Blanco", Money.of(new BigDecimal("10000")));
        Money totalBefore = quotation.getTotal();

        quotation.updateDeliveryDate(LocalDate.of(2026, 8, 2));

        assertEquals(LocalDate.of(2026, 8, 2), quotation.getDeliveryDate());
        assertEquals(QuotationStatus.DRAFT, quotation.getStatus());
        assertEquals(totalBefore, quotation.getTotal());
    }

    @Test
    void acceptsAPastDateThatIsNotBeforeCreation() {
        Quotation quotation = quotation();

        quotation.updateDeliveryDate(quotation.getCreationDate());

        assertEquals(quotation.getCreationDate(), quotation.getDeliveryDate());
    }

    @Test
    void rejectsADateBeforeCreationAndKeepsThePreviousDate() {
        Quotation quotation = quotation();

        QuotationDomainException exception = assertThrows(
                QuotationDomainException.class,
                () -> quotation.updateDeliveryDate(LocalDate.of(2026, 7, 31))
        );

        assertEquals("Delivery date must not be before creation date", exception.getMessage());
        assertEquals(LocalDate.of(2026, 8, 20), quotation.getDeliveryDate());
    }

    @Test
    void rejectsANullDate() {
        Quotation quotation = quotation();

        assertThrows(NullPointerException.class, () -> quotation.updateDeliveryDate(null));
        assertEquals(LocalDate.of(2026, 8, 20), quotation.getDeliveryDate());
    }

    private static Quotation quotation() {
        return Quotation.create(
                QuotationNumber.of(12L),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 20),
                UUID.randomUUID(),
                null
        );
    }
}
