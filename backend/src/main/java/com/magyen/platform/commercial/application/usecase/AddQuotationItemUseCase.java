package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.AddQuotationItemResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.domain.ProductSpecification;
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

    public AddQuotationItemUseCase(QuotationRepository quotationRepository) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
    }

    public AddQuotationItemResult execute(AddQuotationItemCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        Money unitPrice = Money.of(command.unitPrice());
        ProductSpecification productSpecification = toProductSpecification(command.productSpecification());

        quotation.addItem(
                command.productName(),
                command.quantity(),
                command.fabric(),
                command.color(),
                unitPrice,
                productSpecification
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

    private ProductSpecification toProductSpecification(ProductSpecificationCommand command) {
        if (command == null) {
            return ProductSpecification.empty();
        }

        return ProductSpecification.of(
                command.garmentType(),
                command.collarType(),
                command.sleeveType(),
                command.cuffRequired(),
                command.sublimationRequired(),
                command.embroideryRequired(),
                command.dtfRequired(),
                command.decorationNotes(),
                command.includesNames(),
                command.includesNumbers(),
                command.includesLogos(),
                command.personalizationNotes(),
                command.itemObservations()
        );
    }

    private UUID lastCreatedItemId(Quotation quotation) {
        List<QuotationItem> items = quotation.getItems();
        return items.get(items.size() - 1).getId();
    }
}
