package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuotationItemMutationTest {

    @Test
    void updatesItemInDraftAndRecalculatesTotalKeepingIdentity() {
        Quotation quotation = draftQuotationWithTwoItems();
        UUID firstItemId = quotation.getItems().getFirst().getId();

        quotation.updateItem(
                firstItemId,
                "Camiseta actualizada",
                4,
                "Sudáfrica",
                "Perchada",
                "Blanco",
                Money.of(new BigDecimal("25000")),
                ProductSpecification.empty()
        );

        assertEquals(firstItemId, quotation.getItems().getFirst().getId());
        assertEquals("Camiseta actualizada", quotation.getItems().getFirst().getProductName());
        assertEquals(4, quotation.getItems().getFirst().getQuantity());
        assertEquals("Perchada", quotation.getItems().getFirst().getSecondaryFabric());
        assertEquals(new BigDecimal("100000.00"), quotation.getItems().getFirst().getSubtotal().getAmount());
        assertEquals(new BigDecimal("300000.00"), quotation.getTotal().getAmount());
    }

    @Test
    void removesItemInDraftAndRecalculatesTotal() {
        Quotation quotation = draftQuotationWithTwoItems();
        UUID firstItemId = quotation.getItems().getFirst().getId();

        quotation.removeItem(firstItemId);

        assertEquals(1, quotation.getItems().size());
        assertEquals(new BigDecimal("200000.00"), quotation.getTotal().getAmount());
    }

    @Test
    void rejectsUpdateWhenQuotationIsApproved() {
        Quotation quotation = approvedQuotation();
        UUID itemId = quotation.getItems().getFirst().getId();

        QuotationDomainException exception = assertThrows(
                QuotationDomainException.class,
                () -> quotation.updateItem(
                        itemId,
                        "Camiseta",
                        2,
                        "Sudáfrica",
                        null,
                        "Blanco",
                        Money.of(new BigDecimal("15000")),
                        ProductSpecification.empty()
                )
        );

        assertEquals(
                "Items can only be updated while the quotation is draft. Current status: APPROVED",
                exception.getMessage()
        );
        assertEquals(new BigDecimal("400000.00"), quotation.getTotal().getAmount());
    }

    @Test
    void rejectsRemovalWhenQuotationIsApproved() {
        Quotation quotation = approvedQuotation();
        UUID itemId = quotation.getItems().getFirst().getId();

        QuotationDomainException exception = assertThrows(
                QuotationDomainException.class,
                () -> quotation.removeItem(itemId)
        );

        assertEquals(
                "Items can only be removed while the quotation is draft. Current status: APPROVED",
                exception.getMessage()
        );
        assertEquals(1, quotation.getItems().size());
        assertEquals(new BigDecimal("400000.00"), quotation.getTotal().getAmount());
    }

    private static Quotation draftQuotationWithTwoItems() {
        Quotation quotation = Quotation.create(
                QuotationNumber.of(1L),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 25),
                UUID.randomUUID(),
                null
        );
        quotation.addItem(
                "Camiseta",
                10,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("20000"))
        );
        quotation.addItem(
                "Pantaloneta",
                10,
                "Sudáfrica",
                "Negro",
                Money.of(new BigDecimal("20000"))
        );
        return quotation;
    }

    private static Quotation approvedQuotation() {
        Quotation draft = Quotation.create(
                QuotationNumber.of(2L),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 25),
                UUID.randomUUID(),
                null
        );
        draft.addItem(
                "Uniforme",
                10,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("40000"))
        );
        return Quotation.reconstitute(
                draft.getId(),
                draft.getQuotationNumber(),
                draft.getCustomerId(),
                draft.getCreationDate(),
                draft.getDeliveryDate(),
                QuotationStatus.APPROVED,
                draft.getSellerId(),
                draft.getObservations(),
                List.copyOf(draft.getItems())
        );
    }
}
