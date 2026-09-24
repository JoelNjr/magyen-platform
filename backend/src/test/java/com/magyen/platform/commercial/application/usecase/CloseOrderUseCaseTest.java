package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CloseOrderCommand;
import com.magyen.platform.commercial.application.dto.CloseOrderResult;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentAmount;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class CloseOrderUseCaseTest {

    @Autowired
    private CloseOrderUseCase closeOrderUseCase;

    @Autowired
    private RegisterPaymentUseCase registerPaymentUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void insufficientCollectedPaymentsRejectCloseWithoutSideEffects() {
        Order order = saveDelivered();
        int paymentsBefore = paymentRepository.findAll().size();
        int transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> closeOrderUseCase.execute(new CloseOrderCommand(order.getId()))
        );

        assertTrue(exception.getMessage().contains("do not cover"));
        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, reloaded.getStatus());
        assertFalse(reloaded.getPaymentSummary().isFinalPaymentAcknowledged());
        assertEquals(paymentsBefore, paymentRepository.findAll().size());
        assertTrue(paymentRepository.findByOrderId(order.getId()).isEmpty());
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());
    }

    @Test
    void partialCollectedPaymentsRejectClose() {
        Order order = saveDelivered();
        paymentRepository.save(Payment.create(
                order.getId(),
                PaymentAmount.of(new BigDecimal("1000.00")),
                LocalDate.of(2026, 9, 12),
                "partial"
        ));

        assertThrows(
                OrderDomainException.class,
                () -> closeOrderUseCase.execute(new CloseOrderCommand(order.getId()))
        );

        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, reloaded.getStatus());
        assertEquals(1, paymentRepository.findByOrderId(order.getId()).size());
    }

    @Test
    void sufficientCollectedPaymentsAcknowledgeAndCloseWithoutCreatingMoney() {
        Order order = saveDelivered();
        paymentRepository.save(Payment.create(
                order.getId(),
                PaymentAmount.of(order.getTotal().getAmount()),
                LocalDate.of(2026, 9, 12),
                "covered"
        ));
        int paymentsBefore = paymentRepository.findByOrderId(order.getId()).size();
        int transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        CloseOrderResult result = closeOrderUseCase.execute(new CloseOrderCommand(order.getId()));

        assertEquals(OrderStatus.CLOSED, result.status());
        assertTrue(result.finalPaymentAcknowledged());
        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CLOSED, reloaded.getStatus());
        assertTrue(reloaded.getPaymentSummary().isFinalPaymentAcknowledged());
        assertEquals(paymentsBefore, paymentRepository.findByOrderId(order.getId()).size());
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());
    }

    @Test
    void registeringACoveringPaymentDoesNotCloseTheOrder() {
        Order order = saveDelivered();

        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                order.getId(),
                order.getTotal().getAmount(),
                LocalDate.of(2026, 9, 12),
                "full payment"
        ));

        Order afterPayment = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, afterPayment.getStatus());
        assertFalse(afterPayment.getPaymentSummary().isFinalPaymentAcknowledged());
    }

    @Test
    void closeBeforeDeliveryIsRejected() {
        Order created = saveDelivered();
        Order confirmed = orderRepository.save(Order.reconstitute(
                created.getId(),
                created.getOrderNumber(),
                created.getCustomerId(),
                created.getQuotationId(),
                created.getConfirmationDate(),
                OrderStatus.CONFIRMED,
                created.getDeliveryCommitment(),
                created.getPaymentSummary(),
                created.getSellerId(),
                created.getObservations(),
                created.getDescription(),
                created.getItems(),
                created.getDiscount(),
                null
        ));

        assertThrows(
                OrderDomainException.class,
                () -> closeOrderUseCase.execute(new CloseOrderCommand(confirmed.getId()))
        );
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(confirmed.getId()).orElseThrow().getStatus());
    }

    private Order saveDelivered() {
        LocalDate confirmation = LocalDate.of(2026, 8, 10);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta cierre",
                1,
                "Algodón",
                "Blanco",
                Money.of(new BigDecimal("150000.00")),
                ProductSpecification.empty(),
                List.of()
        );
        Order created = Order.create(
                OrderNumber.of("ORD-CLS-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmation,
                DeliveryCommitment.of(confirmation.plusDays(14)),
                UUID.randomUUID(),
                null,
                List.of(item)
        );
        return orderRepository.save(Order.reconstitute(
                created.getId(),
                created.getOrderNumber(),
                created.getCustomerId(),
                created.getQuotationId(),
                created.getConfirmationDate(),
                OrderStatus.DELIVERED,
                created.getDeliveryCommitment(),
                created.getPaymentSummary(),
                created.getSellerId(),
                created.getObservations(),
                created.getDescription(),
                created.getItems(),
                created.getDiscount(),
                LocalDate.of(2026, 9, 12)
        ));
    }
}
