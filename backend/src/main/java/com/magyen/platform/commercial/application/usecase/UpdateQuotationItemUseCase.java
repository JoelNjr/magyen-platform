package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CommercialCatalogValidator;
import com.magyen.platform.commercial.application.dto.UpdateQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.UpdateQuotationItemResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.shared.domain.Money;

import java.util.Objects;

/**
 * Caso de uso que coordina la edición de un producto en una cotización existente.
 * <p>
 * Reutiliza las mismas validaciones de catálogo, cantidad y precio que la adición.
 * El dominio solo permite mutar ítems en {@code DRAFT}.
 */
public class UpdateQuotationItemUseCase {

    private final QuotationRepository quotationRepository;
    private final CommercialCatalogValidator commercialCatalogValidator;

    public UpdateQuotationItemUseCase(
            QuotationRepository quotationRepository,
            CommercialCatalogValidator commercialCatalogValidator
    ) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
        this.commercialCatalogValidator = Objects.requireNonNull(
                commercialCatalogValidator,
                "Commercial catalog validator must not be null"
        );
    }

    public UpdateQuotationItemResult execute(UpdateQuotationItemCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");
        Objects.requireNonNull(command.itemId(), "Item id must not be null");

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        String fabric = commercialCatalogValidator.requirePrimaryFabric(command.fabric());
        String secondaryFabric = commercialCatalogValidator.requireSecondaryFabric(command.secondaryFabric());

        quotation.updateItem(
                command.itemId(),
                command.productName(),
                command.quantity(),
                fabric,
                secondaryFabric,
                command.color(),
                Money.of(command.unitPrice()),
                commercialCatalogValidator.requireProductSpecification(command.productSpecification())
        );

        Quotation savedQuotation = quotationRepository.save(quotation);

        return new UpdateQuotationItemResult(
                savedQuotation.getId(),
                command.itemId(),
                savedQuotation.getTotal().getAmount()
        );
    }
}
