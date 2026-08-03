package com.magyen.platform.commercial.presentation.quotation.mapper;

import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationResult;
import com.magyen.platform.commercial.presentation.quotation.request.CreateQuotationRequest;
import com.magyen.platform.commercial.presentation.quotation.response.CreateQuotationResponse;

import java.util.Objects;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class QuotationPresentationMapper {

    public CreateQuotationCommand toCommand(CreateQuotationRequest request) {
        Objects.requireNonNull(request, "CreateQuotationRequest must not be null");

        return new CreateQuotationCommand(
                request.customerId(),
                request.deliveryDate(),
                request.salesperson(),
                request.observations()
        );
    }

    public CreateQuotationResponse toResponse(CreateQuotationResult result) {
        Objects.requireNonNull(result, "CreateQuotationResult must not be null");

        return new CreateQuotationResponse(
                result.quotationId(),
                result.status().name(),
                result.creationDate()
        );
    }
}
