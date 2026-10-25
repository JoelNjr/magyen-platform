package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.UpdateQuotationDeliveryDateCommand;
import com.magyen.platform.commercial.application.dto.UpdateQuotationDeliveryDateResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.exception.QuotationDomainException;

import java.util.Objects;

/**
 * Corrige la fecha de entrega de una cotización.
 * <p>
 * La cotización es la fuente de esa fecha mientras no exista una orden, y sigue siéndola
 * para el documento de cotización después. No busca ni modifica la orden asociada:
 * la sincronización comercial es explícita y no incluye la fecha de entrega.
 * Una fecha pasada es válida si no es anterior a la fecha de creación.
 */
public class UpdateQuotationDeliveryDateUseCase {

    private final QuotationRepository quotationRepository;

    public UpdateQuotationDeliveryDateUseCase(QuotationRepository quotationRepository) {
        this.quotationRepository = Objects.requireNonNull(
                quotationRepository,
                "Quotation repository must not be null"
        );
    }

    public UpdateQuotationDeliveryDateResult execute(UpdateQuotationDeliveryDateCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");
        if (command.deliveryDate() == null) {
            throw new QuotationDomainException("Delivery date must not be null");
        }

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        quotation.updateDeliveryDate(command.deliveryDate());
        Quotation saved = quotationRepository.save(quotation);

        return new UpdateQuotationDeliveryDateResult(
                saved.getId(),
                saved.getCreationDate(),
                saved.getDeliveryDate(),
                saved.getStatus(),
                saved.getTotal().getAmount()
        );
    }
}
