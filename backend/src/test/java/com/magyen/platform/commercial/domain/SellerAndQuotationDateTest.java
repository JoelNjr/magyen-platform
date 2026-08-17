package com.magyen.platform.commercial.domain;

import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SellerAndQuotationDateTest {

    @Test
    void sellerKeepsStableIdAndTrimsName() {
        Seller seller = Seller.create("  David  ");

        assertEquals("David", seller.getName());
        assertTrue(seller.isActive());

        UUID originalId = seller.getId();
        seller.rename("David Vásquez");
        seller.deactivate();

        assertEquals(originalId, seller.getId());
        assertEquals("David Vásquez", seller.getName());
        assertEquals(false, seller.isActive());
    }

    @Test
    void quotationStoresHistoricalDateAndSellerId() {
        UUID sellerId = UUID.randomUUID();
        LocalDate quotationDate = LocalDate.of(2026, 7, 27);
        LocalDate deliveryDate = LocalDate.of(2026, 8, 6);

        Quotation quotation = Quotation.create(
                QuotationNumber.of(1L),
                UUID.randomUUID(),
                quotationDate,
                deliveryDate,
                sellerId,
                "Histórica"
        );

        assertEquals(quotationDate, quotation.getCreationDate());
        assertEquals(sellerId, quotation.getSellerId());
        assertEquals(1L, quotation.getQuotationNumber().getValue());
    }

    @Test
    void quotationLineCalculatesQuantityTimesUnitPriceAndAcceptsBlanco() {
        Quotation quotation = Quotation.create(
                QuotationNumber.of(1L),
                UUID.randomUUID(),
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 8, 6),
                UUID.randomUUID(),
                null
        );

        quotation.addItem(
                "Volleyball shirt",
                10,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("40000"))
        );

        assertEquals(new BigDecimal("400000.00"), quotation.getTotal().getAmount());
        assertEquals("Blanco", quotation.getItems().getFirst().getColor());
        assertEquals(new BigDecimal("400000.00"), quotation.getItems().getFirst().getSubtotal().getAmount());
    }

    @Test
    void quotationRejectsNullSellerId() {
        assertThrows(NullPointerException.class, () -> Quotation.create(
                QuotationNumber.of(1L),
                UUID.randomUUID(),
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 8, 6),
                null,
                null
        ));
    }
}
