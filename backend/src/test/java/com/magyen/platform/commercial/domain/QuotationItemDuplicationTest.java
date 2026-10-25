package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuotationItemDuplicationTest {

    @Test
    void insertsANewItemAfterTheSourceAndRecalculatesTheDiscountedTotal() {
        Quotation quotation = quotationWithTwoItems();
        quotation.applyDiscount(Money.of(new BigDecimal("10000")));
        UUID sourceId = quotation.getItems().getFirst().getId();
        UUID secondId = quotation.getItems().get(1).getId();
        ProductSpecification specification = ProductSpecification.of(
                "Camiseta",
                "Redondo",
                "Manga corta sisa",
                true,
                true,
                false,
                false,
                "Frente",
                true,
                false,
                true,
                "Espalda",
                "Logo pequeño"
        );

        UUID duplicateId = quotation.duplicateItem(sourceId, "Hydrotech", "Perchada", specification);

        assertNotEquals(sourceId, duplicateId);
        assertEquals(3, quotation.getItems().size());
        assertEquals(sourceId, quotation.getItems().get(0).getId());
        assertEquals(duplicateId, quotation.getItems().get(1).getId());
        assertEquals(secondId, quotation.getItems().get(2).getId());

        QuotationItem source = quotation.getItems().get(0);
        QuotationItem duplicate = quotation.getItems().get(1);
        assertEquals("Camiseta", source.getProductName());
        assertEquals(10, source.getQuantity());
        assertEquals("Sudáfrica", source.getFabric());
        assertEquals("Camiseta", duplicate.getProductName());
        assertEquals(10, duplicate.getQuantity());
        assertEquals("Hydrotech", duplicate.getFabric());
        assertEquals("Perchada", duplicate.getSecondaryFabric());
        assertEquals("Blanco", duplicate.getColor());
        assertEquals(new BigDecimal("20000.00"), duplicate.getUnitPrice().getAmount());
        assertEquals(new BigDecimal("200000.00"), duplicate.getSubtotal().getAmount());
        assertEquals(specification, duplicate.getProductSpecification());
        assertEquals(ProductSpecification.empty(), source.getProductSpecification());
        assertEquals(new BigDecimal("10000.00"), quotation.getDiscount().getAmount());
        assertEquals(new BigDecimal("590000.00"), quotation.getTotal().getAmount());
        assertEquals(QuotationStatus.DRAFT, quotation.getStatus());
    }

    @Test
    void editingTheDuplicateDoesNotChangeTheOriginal() {
        Quotation quotation = quotationWithTwoItems();
        UUID sourceId = quotation.getItems().getFirst().getId();
        UUID duplicateId = quotation.duplicateItem(
                sourceId,
                "Sudáfrica",
                null,
                ProductSpecification.empty()
        );

        quotation.updateItem(
                duplicateId,
                "Camiseta polo",
                4,
                "Hydrotech",
                null,
                "Negro",
                Money.of(new BigDecimal("25000")),
                ProductSpecification.empty()
        );

        QuotationItem source = quotation.getItems().getFirst();
        QuotationItem duplicate = quotation.getItems().get(1);
        assertEquals(sourceId, source.getId());
        assertEquals("Camiseta", source.getProductName());
        assertEquals(10, source.getQuantity());
        assertEquals("Blanco", source.getColor());
        assertEquals("Camiseta polo", duplicate.getProductName());
        assertEquals(4, duplicate.getQuantity());
        assertEquals(new BigDecimal("500000.00"), quotation.getTotal().getAmount());
    }

    @Test
    void missingItemIsRejected() {
        Quotation quotation = quotationWithTwoItems();
        int sizeBefore = quotation.getItems().size();

        assertThrows(
                QuotationDomainException.class,
                () -> quotation.duplicateItem(UUID.randomUUID(), "Hydrotech", null, ProductSpecification.empty())
        );
        assertEquals(sizeBefore, quotation.getItems().size());
    }

    private static Quotation quotationWithTwoItems() {
        Quotation quotation = Quotation.create(
                QuotationNumber.of(15L),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 20),
                UUID.randomUUID(),
                null
        );
        quotation.addItem("Camiseta", 10, "Sudáfrica", "Blanco", Money.of(new BigDecimal("20000")));
        quotation.addItem("Pantaloneta", 10, "Sudáfrica", "Negro", Money.of(new BigDecimal("20000")));
        return quotation;
    }
}
