package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CommercialCatalogValidator;
import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.UpdateQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.UpdateQuotationItemResult;
import com.magyen.platform.commercial.domain.ProductSpecification;
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
class UpdateQuotationItemUseCaseTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private CommercialCatalogValidator commercialCatalogValidator;

    private UpdateQuotationItemUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateQuotationItemUseCase(quotationRepository, commercialCatalogValidator);
    }

    @Test
    void updatesDraftItemAndReturnsRecalculatedTotal() {
        Quotation quotation = draftQuotation();
        UUID itemId = quotation.getItems().getFirst().getId();
        stubCatalog();
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(quotation)).thenReturn(quotation);

        UpdateQuotationItemResult result = useCase.execute(new UpdateQuotationItemCommand(
                quotation.getId(),
                itemId,
                "Camiseta polo",
                5,
                "Sudáfrica",
                null,
                "Blanco",
                new BigDecimal("30000"),
                emptySpecificationCommand()
        ));

        assertEquals(itemId, result.itemId());
        assertEquals(new BigDecimal("150000.00"), result.totalAmount());
        verify(quotationRepository).save(quotation);
    }

    @Test
    void rejectsUpdateWhenQuotationIsApproved() {
        Quotation quotation = approvedQuotation();
        UUID itemId = quotation.getItems().getFirst().getId();
        stubCatalog();
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));

        assertThrows(
                QuotationDomainException.class,
                () -> useCase.execute(new UpdateQuotationItemCommand(
                        quotation.getId(),
                        itemId,
                        "Camiseta polo",
                        5,
                        "Sudáfrica",
                        null,
                        "Blanco",
                        new BigDecimal("30000"),
                        emptySpecificationCommand()
                ))
        );

        verify(quotationRepository, never()).save(any());
        assertEquals(new BigDecimal("400000.00"), quotation.getTotal().getAmount());
    }

    private void stubCatalog() {
        when(commercialCatalogValidator.requirePrimaryFabric("Sudáfrica")).thenReturn("Sudáfrica");
        when(commercialCatalogValidator.requireSecondaryFabric(null)).thenReturn(null);
        when(commercialCatalogValidator.requireProductSpecification(any()))
                .thenReturn(ProductSpecification.empty());
    }

    private static ProductSpecificationCommand emptySpecificationCommand() {
        return new ProductSpecificationCommand(
                null, null, null, null,
                false, false, false, null,
                false, false, false, null, null
        );
    }

    private static Quotation draftQuotation() {
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
        return quotation;
    }

    private static Quotation approvedQuotation() {
        Quotation draft = draftQuotation();
        draft.addItem(
                "Pantaloneta",
                10,
                "Sudáfrica",
                "Negro",
                Money.of(new BigDecimal("20000"))
        );
        Quotation withTwoItems = draft;
        return Quotation.reconstitute(
                withTwoItems.getId(),
                withTwoItems.getQuotationNumber(),
                withTwoItems.getCustomerId(),
                withTwoItems.getCreationDate(),
                withTwoItems.getDeliveryDate(),
                QuotationStatus.APPROVED,
                withTwoItems.getSellerId(),
                withTwoItems.getObservations(),
                List.copyOf(withTwoItems.getItems())
        );
    }
}
