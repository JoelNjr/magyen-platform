package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CommercialCatalogValidator;
import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.AddQuotationItemResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationItem;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.shared.domain.Money;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que coordina la adición de un producto a una cotización existente.
 */
public class AddQuotationItemUseCase {

    private final QuotationRepository quotationRepository;
    private final CommercialCatalogValidator commercialCatalogValidator;

    public AddQuotationItemUseCase(
            QuotationRepository quotationRepository,
            CommercialCatalogValidator commercialCatalogValidator
    ) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
        this.commercialCatalogValidator = Objects.requireNonNull(
                commercialCatalogValidator,
                "Commercial catalog validator must not be null"
        );
    }

    public AddQuotationItemResult execute(AddQuotationItemCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        String fabric = commercialCatalogValidator.requirePrimaryFabric(command.fabric());
        String secondaryFabric = commercialCatalogValidator.requireSecondaryFabric(command.secondaryFabric());

        quotation.addItem(
                command.productName(),
                command.quantity(),
                fabric,
                secondaryFabric,
                command.color(),
                Money.of(command.unitPrice()),
                commercialCatalogValidator.requireProductSpecification(command.productSpecification())
        );

        UUID itemId = lastCreatedItemId(quotation);

        Quotation savedQuotation = quotationRepository.save(quotation);

        return new AddQuotationItemResult(
                savedQuotation.getId(),
                itemId,
                savedQuotation.getTotal().getAmount()
        );
    }

    private void validateCommand(AddQuotationItemCommand command) {
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");
    }

    private UUID lastCreatedItemId(Quotation quotation) {
        List<QuotationItem> items = quotation.getItems();
        return items.get(items.size() - 1).getId();
    }
}
