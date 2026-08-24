package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.RemoveQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.RemoveQuotationItemResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la eliminación de un producto de una cotización existente.
 * <p>
 * El dominio solo permite mutar ítems en {@code DRAFT}. Recalcula el total del agregado.
 */
public class RemoveQuotationItemUseCase {

    private final QuotationRepository quotationRepository;

    public RemoveQuotationItemUseCase(QuotationRepository quotationRepository) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
    }

    public RemoveQuotationItemResult execute(RemoveQuotationItemCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");
        Objects.requireNonNull(command.itemId(), "Item id must not be null");

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        quotation.removeItem(command.itemId());

        Quotation savedQuotation = quotationRepository.save(quotation);

        return new RemoveQuotationItemResult(
                savedQuotation.getId(),
                savedQuotation.getTotal().getAmount()
        );
    }
}
