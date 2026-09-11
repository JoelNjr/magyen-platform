package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.SellerNameResolver;
import com.magyen.platform.commercial.application.dto.GetQuotationsQuery;
import com.magyen.platform.commercial.application.dto.QuotationResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetQuotationsUseCaseTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private SellerNameResolver sellerNameResolver;

    private GetQuotationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetQuotationsUseCase(quotationRepository, sellerNameResolver);
        when(sellerNameResolver.nameLookup(any())).thenReturn(id -> "Seller");
    }

    @Test
    void listsQuotationsByCommercialConsecutiveIncludingCrossingTen() {
        List<Quotation> shuffled = new ArrayList<>();
        IntStream.rangeClosed(1, 12).forEach(number -> shuffled.add(quotation(number, LocalDate.of(2026, 5, 1))));
        Collections.shuffle(shuffled);
        when(quotationRepository.findAll()).thenReturn(shuffled);

        List<Long> numbers = useCase.execute().quotations().stream()
                .map(QuotationResult::quotationNumber)
                .toList();

        assertEquals(
                List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L),
                numbers
        );
    }

    @Test
    void updatedQuotationDoesNotJumpToTheFront() {
        Quotation first = quotation(13, LocalDate.of(2026, 5, 2));
        Quotation second = quotation(14, LocalDate.of(2026, 5, 3));
        Quotation third = quotation(15, LocalDate.of(2026, 5, 1));
        third.updateObservations("Edited after listing");

        when(quotationRepository.findAll()).thenReturn(List.of(third, first, second));

        List<Long> numbers = useCase.execute().quotations().stream()
                .map(QuotationResult::quotationNumber)
                .toList();

        assertEquals(List.of(13L, 14L, 15L), numbers);
    }

    @Test
    void dateFilterStillAppliesAndKeepsConsecutiveOrder() {
        Quotation inRangeHigh = quotation(20, LocalDate.of(2026, 6, 10));
        Quotation inRangeLow = quotation(9, LocalDate.of(2026, 6, 20));
        Quotation outOfRange = quotation(11, LocalDate.of(2026, 7, 1));
        when(quotationRepository.findAll()).thenReturn(List.of(inRangeHigh, outOfRange, inRangeLow));

        List<Long> numbers = useCase.execute(
                new GetQuotationsQuery(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30))
        ).quotations().stream().map(QuotationResult::quotationNumber).toList();

        assertEquals(List.of(9L, 20L), numbers);
    }

    private static Quotation quotation(long number, LocalDate creationDate) {
        return Quotation.create(
                QuotationNumber.of(number),
                UUID.randomUUID(),
                creationDate,
                creationDate.plusDays(7),
                UUID.randomUUID(),
                "Q" + number
        );
    }
}
