package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.UpdateQuotationDeliveryDateCommand;
import com.magyen.platform.commercial.application.dto.UpdateQuotationDeliveryDateResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.QuotationStatus;
import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateQuotationDeliveryDateUseCaseTest {

    @Mock
    private QuotationRepository quotationRepository;

    private UpdateQuotationDeliveryDateUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateQuotationDeliveryDateUseCase(quotationRepository);
    }

    @Test
    void updatesOnlyTheQuotationDeliveryDate() {
        Quotation quotation = quotation();
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(quotation)).thenReturn(quotation);

        UpdateQuotationDeliveryDateResult result = useCase.execute(new UpdateQuotationDeliveryDateCommand(
                quotation.getId(),
                LocalDate.of(2026, 8, 5)
        ));

        assertEquals(LocalDate.of(2026, 8, 5), result.deliveryDate());
        assertEquals(QuotationStatus.DRAFT, result.status());
        assertEquals(quotation.getCreationDate(), result.creationDate());
        verify(quotationRepository).save(quotation);
    }

    @Test
    void rejectsANullDateBeforeLoadingAMissingQuotation() {
        assertThrows(
                QuotationDomainException.class,
                () -> useCase.execute(new UpdateQuotationDeliveryDateCommand(UUID.randomUUID(), null))
        );
        verify(quotationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private static Quotation quotation() {
        return Quotation.create(
                QuotationNumber.of(3L),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 20),
                UUID.randomUUID(),
                null
        );
    }
}
