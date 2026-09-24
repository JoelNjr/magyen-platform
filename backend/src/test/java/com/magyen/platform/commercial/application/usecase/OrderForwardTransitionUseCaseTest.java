package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.MarkOrderReadyForDeliveryCommand;
import com.magyen.platform.commercial.application.dto.StartOrderProductionCommand;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.production.domain.ProductionOrderRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class OrderForwardTransitionUseCaseTest {

    @Autowired
    private StartOrderProductionUseCase startOrderProductionUseCase;

    @Autowired
    private MarkOrderReadyForDeliveryUseCase markOrderReadyForDeliveryUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Test
    void startProductionMovesConfirmedOrderWithoutSideEffects() {
        Order order = save(OrderStatus.CONFIRMED);
        int paymentsBefore = paymentRepository.findAll().size();
        int transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        var result = startOrderProductionUseCase.execute(new StartOrderProductionCommand(order.getId()));

        assertEquals(OrderStatus.IN_PRODUCTION, result.status());
        assertEquals(OrderStatus.IN_PRODUCTION, orderRepository.findById(order.getId()).orElseThrow().getStatus());
        assertEquals(paymentsBefore, paymentRepository.findAll().size());
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());
        assertTrue(productionOrderRepository.findByOrderId(order.getId()).isEmpty());
    }

    @Test
    void startProductionRejectsInProduction() {
        Order order = save(OrderStatus.IN_PRODUCTION);

        assertThrows(
                OrderDomainException.class,
                () -> startOrderProductionUseCase.execute(new StartOrderProductionCommand(order.getId()))
        );
        assertEquals(OrderStatus.IN_PRODUCTION, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void markReadyMovesInProductionWithoutSideEffects() {
        Order order = save(OrderStatus.IN_PRODUCTION);
        int paymentsBefore = paymentRepository.findAll().size();
        int transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        var result = markOrderReadyForDeliveryUseCase.execute(new MarkOrderReadyForDeliveryCommand(order.getId()));

        assertEquals(OrderStatus.READY_FOR_DELIVERY, result.status());
        assertEquals(
                OrderStatus.READY_FOR_DELIVERY,
                orderRepository.findById(order.getId()).orElseThrow().getStatus()
        );
        assertEquals(paymentsBefore, paymentRepository.findAll().size());
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());
        assertTrue(productionOrderRepository.findByOrderId(order.getId()).isEmpty());
    }

    @Test
    void markReadyRejectsConfirmed() {
        Order order = save(OrderStatus.CONFIRMED);

        assertThrows(
                OrderDomainException.class,
                () -> markOrderReadyForDeliveryUseCase.execute(new MarkOrderReadyForDeliveryCommand(order.getId()))
        );
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    private Order save(OrderStatus status) {
        LocalDate confirmation = LocalDate.of(2026, 8, 10);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta avance",
                1,
                "Algodón",
                "Blanco",
                Money.of(new BigDecimal("80000.00")),
                ProductSpecification.empty(),
                List.of()
        );
        Order created = Order.create(
                OrderNumber.of("ORD-FWD-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmation,
                DeliveryCommitment.of(confirmation.plusDays(10)),
                UUID.randomUUID(),
                null,
                List.of(item)
        );
        if (status == OrderStatus.CONFIRMED) {
            return orderRepository.save(created);
        }
        return orderRepository.save(Order.reconstitute(
                created.getId(),
                created.getOrderNumber(),
                created.getCustomerId(),
                created.getQuotationId(),
                created.getConfirmationDate(),
                status,
                created.getDeliveryCommitment(),
                created.getPaymentSummary(),
                created.getSellerId(),
                created.getObservations(),
                created.getDescription(),
                created.getItems(),
                created.getDiscount(),
                null
        ));
    }
}
