package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CommercialCatalogValidator;
import com.magyen.platform.commercial.application.dto.DuplicateQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.DuplicateQuotationItemResult;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationItem;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicateQuotationItemUseCaseTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private CommercialCatalogValidator commercialCatalogValidator;

    private DuplicateQuotationItemUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DuplicateQuotationItemUseCase(quotationRepository, commercialCatalogValidator);
    }

    @Test
    void duplicatesWithValidatedCatalogValuesAndRecalculatedTotal() {
        Quotation quotation = quotation();
        QuotationItem source = quotation.getItems().getFirst();
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(quotation)).thenReturn(quotation);
        when(commercialCatalogValidator.requirePrimaryFabric("Sudáfrica")).thenReturn("Hydrotech");
        when(commercialCatalogValidator.requireSecondaryFabric(null)).thenReturn(null);
        when(commercialCatalogValidator.requireProductSpecification(any()))
                .thenReturn(ProductSpecification.of(
                        "Camiseta", null, null, null,
                        false, false, false, null,
                        false, false, false, null, null
                ));

        DuplicateQuotationItemResult result = useCase.execute(
                new DuplicateQuotationItemCommand(quotation.getId(), source.getId())
        );

        assertEquals(source.getId(), result.sourceItemId());
        assertNotEquals(source.getId(), result.itemId());
        assertEquals("Sudáfrica", source.getFabric());
        assertEquals(ProductSpecification.empty(), source.getProductSpecification());
        QuotationItem duplicate = quotation.getItems().get(1);
        assertEquals(result.itemId(), duplicate.getId());
        assertEquals("Hydrotech", duplicate.getFabric());
        assertEquals("Camiseta", duplicate.getProductSpecification().getGarmentType());
        assertEquals(new BigDecimal("400000.00"), result.totalAmount());
        verify(quotationRepository).save(quotation);
    }

    @Test
    void doesNotSaveWhenTheCatalogRejectsTheCopy() {
        Quotation quotation = quotation();
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(commercialCatalogValidator.requirePrimaryFabric("Sudáfrica"))
                .thenThrow(new IllegalArgumentException("Inactive fabric cannot be selected: Sudáfrica"));

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(new DuplicateQuotationItemCommand(
                        quotation.getId(),
                        quotation.getItems().getFirst().getId()
                ))
        );
        assertEquals(1, quotation.getItems().size());
        verify(quotationRepository, never()).save(any());
    }

    private static Quotation quotation() {
        Quotation quotation = Quotation.create(
                QuotationNumber.of(4L),
                java.util.UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 20),
                java.util.UUID.randomUUID(),
                null
        );
        quotation.addItem("Camiseta", 10, "Sudáfrica", "Blanco", Money.of(new BigDecimal("20000")));
        return quotation;
    }
}
