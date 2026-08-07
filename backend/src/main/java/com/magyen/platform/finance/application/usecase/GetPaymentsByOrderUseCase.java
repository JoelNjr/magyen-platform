package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.finance.application.dto.GetPaymentResult;
import com.magyen.platform.finance.application.dto.GetPaymentsByOrderQuery;
import com.magyen.platform.finance.application.dto.GetPaymentsByOrderResult;
import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que coordina la consulta de los pagos de una Orden
 * y el cálculo del total pagado y del saldo restante.
 * <p>
 * Referencia la Orden únicamente por identidad.
 */
public class GetPaymentsByOrderUseCase {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public GetPaymentsByOrderUseCase(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.paymentRepository = Objects.requireNonNull(
                paymentRepository,
                "Payment repository must not be null"
        );
    }

    public GetPaymentsByOrderResult execute(GetPaymentsByOrderQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.orderId(), "Order id must not be null");

        Order order = orderRepository.findById(query.orderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + query.orderId()
                ));

        List<Payment> payments = paymentRepository.findByOrderId(order.getId());
        BigDecimal totalPaid = calculateTotalPaid(payments);
        BigDecimal remainingBalance = order.getTotal().getAmount().subtract(totalPaid);

        List<GetPaymentResult> paymentResults = payments.stream()
                .map(this::toPaymentResult)
                .toList();

        return new GetPaymentsByOrderResult(paymentResults, totalPaid, remainingBalance);
    }

    private BigDecimal calculateTotalPaid(List<Payment> payments) {
        return payments.stream()
                .map(payment -> payment.getAmount().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private GetPaymentResult toPaymentResult(Payment payment) {
        return new GetPaymentResult(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount().getValue(),
                payment.getPaymentDate(),
                payment.getObservations()
        );
    }
}
