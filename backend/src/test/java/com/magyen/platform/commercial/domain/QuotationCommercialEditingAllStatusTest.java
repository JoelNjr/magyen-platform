package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuotationCommercialEditingAllStatusTest {

    @Test
    void addUpdateRemoveAndDiscountWorkInEveryStatus() {
        for (QuotationStatus status : QuotationStatus.values()) {
            Quotation quotation = quotationWithTwoItems(status);
            UUID firstItemId = quotation.getItems().getFirst().getId();
            Long quotationNumber = quotation.getQuotationNumber().getValue();

            quotation.addItem(
                    "Pantalón extra",
                    2,
                    "Hydrotech",
                    "Negro",
                    Money.of(new BigDecimal("30000"))
            );
            quotation.updateItem(
                    firstItemId,
                    "Camiseta",
                    12,
                    "Sudáfrica",
                    "Perchada",
                    "Blanco",
                    Money.of(new BigDecimal("20000")),
                    ProductSpecification.empty()
            );
            quotation.applyDiscount(Money.of(new BigDecimal("10000")));
            UUID extraId = quotation.getItems().getLast().getId();
            quotation.removeItem(extraId);

            assertEquals(status, quotation.getStatus());
            assertEquals(quotationNumber, quotation.getQuotationNumber().getValue());
            assertEquals(firstItemId, quotation.getItems().getFirst().getId());
            assertEquals(12, quotation.getItems().getFirst().getQuantity());
            assertEquals(2, quotation.getItems().size());
            assertEquals(new BigDecimal("430000.00"), quotation.getTotal().getAmount());
        }
    }

    @Test
    void draftMayBecomeEmptyButNonDraftCannotLoseLastItem() {
        Quotation draft = quotationWithOneItem(QuotationStatus.DRAFT);
        UUID draftItemId = draft.getItems().getFirst().getId();
        draft.removeItem(draftItemId);
        assertTrue(draft.getItems().isEmpty());
        assertEquals(Money.zero(), draft.getTotal());

        for (QuotationStatus status : List.of(
                QuotationStatus.SENT,
                QuotationStatus.APPROVED,
                QuotationStatus.REJECTED,
                QuotationStatus.EXPIRED,
                QuotationStatus.CLOSED
        )) {
            Quotation quotation = quotationWithOneItem(status);
            UUID itemId = quotation.getItems().getFirst().getId();
            QuotationDomainException exception = assertThrows(
                    QuotationDomainException.class,
                    () -> quotation.removeItem(itemId)
            );
            assertTrue(exception.getMessage().contains("last product"));
            assertEquals(1, quotation.getItems().size());
            assertEquals(itemId, quotation.getItems().getFirst().getId());
        }
    }

    private static Quotation quotationWithOneItem(QuotationStatus status) {
        Quotation draft = Quotation.create(
                QuotationNumber.of(40L),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                UUID.randomUUID(),
                null
        );
        draft.addItem("Camiseta", 10, "Sudáfrica", "Blanco", Money.of(new BigDecimal("20000")));
        return withStatus(draft, status);
    }

    private static Quotation quotationWithTwoItems(QuotationStatus status) {
        Quotation draft = Quotation.create(
                QuotationNumber.of(41L),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                UUID.randomUUID(),
                null
        );
        draft.addItem("Camiseta", 10, "Sudáfrica", "Blanco", Money.of(new BigDecimal("20000")));
        draft.addItem("Pantaloneta", 10, "Sudáfrica", "Negro", Money.of(new BigDecimal("20000")));
        return withStatus(draft, status);
    }

    private static Quotation withStatus(Quotation source, QuotationStatus status) {
        if (status == QuotationStatus.DRAFT) {
            return source;
        }
        return Quotation.reconstitute(
                source.getId(),
                source.getQuotationNumber(),
                source.getCustomerId(),
                source.getCreationDate(),
                source.getDeliveryDate(),
                status,
                source.getSellerId(),
                source.getObservations(),
                new ArrayList<>(source.getItems()),
                source.getDiscount()
        );
    }
}
