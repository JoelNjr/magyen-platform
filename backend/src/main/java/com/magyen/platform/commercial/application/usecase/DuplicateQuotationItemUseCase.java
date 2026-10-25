package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CommercialCatalogValidator;
import com.magyen.platform.commercial.application.dto.DuplicateQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.DuplicateQuotationItemResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationItem;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.exception.QuotationDomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Duplica un ítem de cotización como una fila nueva.
 * <p>
 * Revalida tela y especificación contra los catálogos activos, igual que un alta.
 * Recalcula el total con el descuento ya aplicado. No crea una orden ni movimientos financieros.
 */
public class DuplicateQuotationItemUseCase {

    private final QuotationRepository quotationRepository;
    private final CommercialCatalogValidator commercialCatalogValidator;

    public DuplicateQuotationItemUseCase(
            QuotationRepository quotationRepository,
            CommercialCatalogValidator commercialCatalogValidator
    ) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
        this.commercialCatalogValidator = Objects.requireNonNull(
                commercialCatalogValidator,
                "Commercial catalog validator must not be null"
        );
    }

    public DuplicateQuotationItemResult execute(DuplicateQuotationItemCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");
        Objects.requireNonNull(command.itemId(), "Item id must not be null");

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        QuotationItem source = quotation.getItems().stream()
                .filter(item -> item.getId().equals(command.itemId()))
                .findFirst()
                .orElseThrow(() -> new QuotationDomainException(
                        "Quotation item not found: " + command.itemId()
                ));

        String fabric = commercialCatalogValidator.requirePrimaryFabric(source.getFabric());
        String secondaryFabric = commercialCatalogValidator.requireSecondaryFabric(source.getSecondaryFabric());
        ProductSpecification specification = commercialCatalogValidator.requireProductSpecification(
                toSpecificationCommand(source.getProductSpecification())
        );

        UUID duplicatedItemId = quotation.duplicateItem(
                source.getId(),
                fabric,
                secondaryFabric,
                specification
        );
        Quotation saved = quotationRepository.save(quotation);

        return new DuplicateQuotationItemResult(
                saved.getId(),
                source.getId(),
                duplicatedItemId,
                saved.getSubtotal().getAmount(),
                saved.getDiscount().getAmount(),
                saved.getTotal().getAmount()
        );
    }

    private static ProductSpecificationCommand toSpecificationCommand(ProductSpecification specification) {
        ProductSpecification source = specification == null ? ProductSpecification.empty() : specification;
        return new ProductSpecificationCommand(
                source.getGarmentType(),
                source.getCollarType(),
                source.getSleeveType(),
                source.getCuffRequired(),
                source.isSublimationRequired(),
                source.isEmbroideryRequired(),
                source.isDtfRequired(),
                source.getDecorationNotes(),
                source.isIncludesNames(),
                source.isIncludesNumbers(),
                source.isIncludesLogos(),
                source.getPersonalizationNotes(),
                source.getItemObservations()
        );
    }
}
