package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.RemoveQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.RemoveQuotationItemResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.QuotationStatus;
import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveQuotationItemUseCaseTest {

    @Mock
    private QuotationRepository quotationRepository;

    private RemoveQuotationItemUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RemoveQuotationItemUseCase(quotationRepository);
    }

    @Test
    void removesDraftItemAndReturnsRecalculatedTotal() {
        Quotation quotation = draftQuotationWithTwoItems();
        UUID firstItemId = quotation.getItems().getFirst().getId();
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(quotation)).thenReturn(quotation);

        RemoveQuotationItemResult result = useCase.execute(new RemoveQuotationItemCommand(
                quotation.getId(),
                firstItemId
        ));

        assertEquals(quotation.getId(), result.quotationId());
        assertEquals(new BigDecimal("200000.00"), result.totalAmount());
        assertEquals(1, quotation.getItems().size());
        verify(quotationRepository).save(quotation);
    }

    @Test
    void rejectsRemovalWhenQuotationIsApproved() {
        Quotation quotation = approvedQuotation();
        UUID itemId = quotation.getItems().getFirst().getId();
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));

        assertThrows(
                QuotationDomainException.class,
                () -> useCase.execute(new RemoveQuotationItemCommand(quotation.getId(), itemId))
        );

        verify(quotationRepository, never()).save(any());
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
