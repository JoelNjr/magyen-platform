package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPaymentQuery;
import com.magyen.platform.finance.application.dto.GetPaymentResult;
import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la consulta de un pago existente por identidad.
 */
public class GetPaymentUseCase {

    private final PaymentRepository paymentRepository;

    public GetPaymentUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = Objects.requireNonNull(
                paymentRepository,
                "Payment repository must not be null"
        );
    }

    public GetPaymentResult execute(GetPaymentQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.paymentId(), "Payment id must not be null");

        Payment payment = paymentRepository.findById(query.paymentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + query.paymentId()
                ));

        return new GetPaymentResult(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount().getValue(),
                payment.getPaymentDate(),
                payment.getObservations()
        );
    }
}
