package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetQuotationsResult;
import com.magyen.platform.commercial.application.dto.QuotationResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consulta las cotizaciones existentes.
 */
public class GetQuotationsUseCase {

    private final QuotationRepository quotationRepository;

    public GetQuotationsUseCase(QuotationRepository quotationRepository) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
    }

    public GetQuotationsResult execute() {
        List<QuotationResult> quotations = quotationRepository.findAll().stream()
                .map(this::toQuotationResult)
                .toList();

        return new GetQuotationsResult(quotations);
    }

    private QuotationResult toQuotationResult(Quotation quotation) {
        return new QuotationResult(
                quotation.getId(),
                quotation.getCustomerId(),
                quotation.getCreationDate(),
                quotation.getDeliveryDate(),
                quotation.getStatus(),
                quotation.getSalesperson(),
                quotation.getObservations(),
                quotation.getTotal().getAmount()
        );
    }
}
