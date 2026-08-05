package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la aprobación de una cotización existente.
 */
public class ApproveQuotationUseCase {

    private final QuotationRepository quotationRepository;

    public ApproveQuotationUseCase(QuotationRepository quotationRepository) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
    }

    public ApproveQuotationResult execute(ApproveQuotationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        quotation.approve();

        Quotation savedQuotation = quotationRepository.save(quotation);

        return new ApproveQuotationResult(
                savedQuotation.getId(),
                savedQuotation.getStatus()
        );
    }

    private void validateCommand(ApproveQuotationCommand command) {
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");
    }
}
