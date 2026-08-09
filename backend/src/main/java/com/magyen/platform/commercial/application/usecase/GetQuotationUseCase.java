package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetQuotationResult;
import com.magyen.platform.commercial.application.dto.QuotationItemResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationItem;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consulta una cotización completa por identificador.
 */
public class GetQuotationUseCase {

    private final QuotationRepository quotationRepository;

    public GetQuotationUseCase(QuotationRepository quotationRepository) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
    }

    public GetQuotationResult execute(GetQuotationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        return toResult(quotation);
    }

    private void validateCommand(GetQuotationCommand command) {
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");
    }

    private GetQuotationResult toResult(Quotation quotation) {
        List<QuotationItemResult> items = quotation.getItems().stream()
                .map(this::toItemResult)
                .toList();

        return new GetQuotationResult(
                quotation.getId(),
                toQuotationNumberValue(quotation.getQuotationNumber()),
                quotation.getCustomerId(),
                quotation.getCreationDate(),
                quotation.getDeliveryDate(),
                quotation.getStatus(),
                quotation.getSalesperson(),
                quotation.getObservations(),
                items,
                quotation.getTotal().getAmount()
        );
    }

    private Long toQuotationNumberValue(QuotationNumber quotationNumber) {
        if (quotationNumber == null) {
            return null;
        }
        return quotationNumber.getValue();
    }

    private QuotationItemResult toItemResult(QuotationItem item) {
        return new QuotationItemResult(
                item.getId(),
                item.getProductName(),
                item.getQuantity(),
                item.getFabric(),
                item.getColor(),
                item.getUnitPrice().getAmount(),
                item.getSubtotal().getAmount()
        );
    }
}
