package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.dto.RegisterPaymentResult;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentAmount;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que coordina el registro de un pago sobre una Orden comercial.
 * <p>
 * Calcula el saldo restante a partir del total de la Orden y los pagos existentes.
 * Referencia la Orden únicamente por identidad; no modifica el estado comercial.
 * <p>
 * Tras persistir el Payment, sincroniza exactamente un {@code FinancialTransaction}
 * INCOME (fuente {@code COMMERCIAL_ORDER}, {@code sourceId = paymentId}) en la misma
 * transacción de base de datos.
 */
public class RegisterPaymentUseCase {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CommercialPaymentLedgerSynchronizer ledgerSynchronizer;

    public RegisterPaymentUseCase(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.paymentRepository = Objects.requireNonNull(
                paymentRepository,
                "Payment repository must not be null"
        );
        this.ledgerSynchronizer = new CommercialPaymentLedgerSynchronizer(
                Objects.requireNonNull(
                        financialTransactionRepository,
                        "Financial transaction repository must not be null"
                )
        );
    }

    @Transactional
    public RegisterPaymentResult execute(RegisterPaymentCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + command.orderId()
                ));

        List<Payment> existingPayments = paymentRepository.findByOrderId(order.getId());
        BigDecimal totalPaid = calculateTotalPaid(existingPayments);
        BigDecimal remainingBalance = order.getTotal().getAmount().subtract(totalPaid);

        PaymentAmount paymentAmount = PaymentAmount.of(command.amount());

        if (paymentAmount.getValue().compareTo(remainingBalance) > 0) {
            throw new FinanceDomainException(
                    "Payment amount exceeds remaining balance. Remaining balance: " + remainingBalance
                            + ", requested amount: " + paymentAmount.getValue()
            );
        }

        Payment payment = Payment.create(
                order.getId(),
                paymentAmount,
                command.paymentDate(),
                command.observations()
        );

        Payment savedPayment = paymentRepository.save(payment);
        ledgerSynchronizer.ensureIncomeTransaction(savedPayment);

        return new RegisterPaymentResult(
                savedPayment.getId(),
                savedPayment.getOrderId(),
                savedPayment.getAmount().getValue(),
                savedPayment.getPaymentDate(),
                savedPayment.getObservations()
        );
    }

    private void validateCommand(RegisterPaymentCommand command) {
        Objects.requireNonNull(command.orderId(), "Order id must not be null");
        Objects.requireNonNull(command.amount(), "Amount must not be null");
        Objects.requireNonNull(command.paymentDate(), "Payment date must not be null");
    }

    private BigDecimal calculateTotalPaid(List<Payment> payments) {
        return payments.stream()
                .map(payment -> payment.getAmount().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
