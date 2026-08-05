package com.magyen.platform.commercial.presentation.order.mapper;

import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.presentation.order.request.CreateOrderRequest;
import com.magyen.platform.commercial.presentation.order.response.CreateOrderResponse;

import java.util.Objects;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class OrderPresentationMapper {

    public CreateOrderFromQuotationCommand toCommand(CreateOrderRequest request) {
        Objects.requireNonNull(request, "CreateOrderRequest must not be null");

        return new CreateOrderFromQuotationCommand(
                request.quotationId(),
                request.orderNumber(),
                request.deliveryDate(),
                request.salesperson(),
                request.observations()
        );
    }

    public CreateOrderResponse toResponse(CreateOrderFromQuotationResult result) {
        Objects.requireNonNull(result, "CreateOrderFromQuotationResult must not be null");

        return new CreateOrderResponse(
                result.orderId(),
                result.orderNumber(),
                result.status().name(),
                result.confirmationDate()
        );
    }
}
