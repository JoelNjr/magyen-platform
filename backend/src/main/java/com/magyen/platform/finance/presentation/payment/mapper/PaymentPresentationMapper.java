package com.magyen.platform.finance.presentation.payment.mapper;

import com.magyen.platform.finance.application.dto.GetPaymentQuery;
import com.magyen.platform.finance.application.dto.GetPaymentResult;
import com.magyen.platform.finance.application.dto.GetPaymentsByOrderQuery;
import com.magyen.platform.finance.application.dto.GetPaymentsByOrderResult;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.dto.RegisterPaymentResult;
import com.magyen.platform.finance.presentation.payment.request.RegisterPaymentRequest;
import com.magyen.platform.finance.presentation.payment.response.GetPaymentResponse;
import com.magyen.platform.finance.presentation.payment.response.GetPaymentsByOrderResponse;
import com.magyen.platform.finance.presentation.payment.response.RegisterPaymentResponse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class PaymentPresentationMapper {

    public RegisterPaymentCommand toCommand(RegisterPaymentRequest request) {
        Objects.requireNonNull(request, "RegisterPaymentRequest must not be null");

        return new RegisterPaymentCommand(
                request.orderId(),
                request.amount(),
                request.paymentDate(),
                request.observations()
        );
    }

    public RegisterPaymentResponse toResponse(RegisterPaymentResult result) {
        Objects.requireNonNull(result, "RegisterPaymentResult must not be null");

        return new RegisterPaymentResponse(
                result.paymentId(),
                result.orderId(),
                result.amount(),
                result.paymentDate(),
                result.observations()
        );
    }

    public GetPaymentQuery toGetPaymentQuery(UUID paymentId) {
        Objects.requireNonNull(paymentId, "Payment id must not be null");

        return new GetPaymentQuery(paymentId);
    }

    public GetPaymentResponse toResponse(GetPaymentResult result) {
        Objects.requireNonNull(result, "GetPaymentResult must not be null");

        return new GetPaymentResponse(
                result.paymentId(),
                result.orderId(),
                result.amount(),
                result.paymentDate(),
                result.observations()
        );
    }

    public GetPaymentsByOrderQuery toGetPaymentsByOrderQuery(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");

        return new GetPaymentsByOrderQuery(orderId);
    }

    public GetPaymentsByOrderResponse toResponse(GetPaymentsByOrderResult result) {
        Objects.requireNonNull(result, "GetPaymentsByOrderResult must not be null");

        List<GetPaymentResponse> payments = result.payments().stream()
                .map(this::toResponse)
                .toList();

        return new GetPaymentsByOrderResponse(
                payments,
                result.totalPaid(),
                result.remainingBalance()
        );
    }
}
