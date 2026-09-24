package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.DeliverOrderCommand;
import com.magyen.platform.commercial.application.dto.DeliverOrderResult;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.exception.OrderDeliveryDateConflictException;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class DeliverOrderUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 14);

    @MockitoBean
    private Clock clock;

    @Autowired
    private DeliverOrderUseCase deliverOrderUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @BeforeEach
    void setFixedClock() {
        ZoneId zone = ZoneId.systemDefault();
        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(zone).toInstant());
    }

    @Test
    void readyForDeliveryUsesTheCommandDateAndIsSaved() {
        Order order = saveOrder(OrderStatus.READY_FOR_DELIVERY);
        LocalDate deliveryDate = LocalDate.of(2026, 9, 10);

        DeliverOrderResult result = deliverOrderUseCase.execute(
                new DeliverOrderCommand(order.getId(), deliveryDate)
        );

        assertEquals(OrderStatus.DELIVERED, result.status());
        assertEquals(deliveryDate, result.actualDeliveryDate());
        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, reloaded.getStatus());
        assertEquals(deliveryDate, reloaded.getActualDeliveryDate());
    }

    @Test
    void confirmedCannotBeDelivered() {
        Order order = saveOrder(OrderStatus.CONFIRMED);
        assertThrows(
                OrderDomainException.class,
                () -> deliverOrderUseCase.execute(new DeliverOrderCommand(order.getId(), TODAY))
        );
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void inProductionCannotBeDelivered() {
        Order order = saveOrder(OrderStatus.IN_PRODUCTION);
        assertThrows(
                OrderDomainException.class,
                () -> deliverOrderUseCase.execute(new DeliverOrderCommand(order.getId(), TODAY))
        );
        assertEquals(OrderStatus.IN_PRODUCTION, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void sameStoredDateIsIdempotentAndDoesNotSaveAgain() {
        Order order = saveDelivered(LocalDate.of(2026, 8, 28));

        DeliverOrderResult result = deliverOrderUseCase.execute(
                new DeliverOrderCommand(order.getId(), LocalDate.of(2026, 8, 28))
        );

        assertEquals(LocalDate.of(2026, 8, 28), result.actualDeliveryDate());
        assertEquals(OrderStatus.DELIVERED, result.status());
        assertEquals(
                LocalDate.of(2026, 8, 28),
                orderRepository.findById(order.getId()).orElseThrow().getActualDeliveryDate()
        );
    }

    @Test
    void differentStoredDateIsRejectedWithoutOverwrite() {
        Order order = saveDelivered(LocalDate.of(2026, 8, 28));

        assertThrows(
                OrderDeliveryDateConflictException.class,
                () -> deliverOrderUseCase.execute(new DeliverOrderCommand(order.getId(), TODAY))
        );

        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, reloaded.getStatus());
        assertEquals(LocalDate.of(2026, 8, 28), reloaded.getActualDeliveryDate());
    }

    @Test
    void futureDeliveryDateIsRejected() {
        Order order = saveOrder(OrderStatus.READY_FOR_DELIVERY);

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> deliverOrderUseCase.execute(
                        new DeliverOrderCommand(order.getId(), TODAY.plusDays(1))
                )
        );

        assertTrue(exception.getMessage().contains("current business date"));
        assertEquals(OrderStatus.READY_FOR_DELIVERY, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void deliveryDateBeforeConfirmationIsRejected() {
        Order order = saveOrder(OrderStatus.READY_FOR_DELIVERY);

        assertThrows(
                OrderDomainException.class,
                () -> deliverOrderUseCase.execute(
                        new DeliverOrderCommand(order.getId(), LocalDate.of(2026, 8, 9))
                )
        );
        assertNullDate(order.getId());
    }

    @Test
    void closedOrderCannotBeDelivered() {
        Order order = save(OrderStatus.CLOSED, LocalDate.of(2026, 8, 20));

        assertThrows(
                OrderDomainException.class,
                () -> deliverOrderUseCase.execute(new DeliverOrderCommand(order.getId(), TODAY))
        );
        assertEquals(OrderStatus.CLOSED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void deliveryDoesNotCreatePaymentFinancialTransactionOrMutateProduction() {
        Order order = saveOrder(OrderStatus.READY_FOR_DELIVERY);
        ProductionOrder productionOrder = productionOrderRepository.save(ProductionOrder.create(
                order.getId(),
                LocalDate.of(2026, 9, 1),
                ProductionPriority.NORMAL,
                null,
                null,
                "isolation"
        ));
        int paymentsBefore = paymentRepository.findAll().size();
        int transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();
        ProductionStatus productionStatusBefore = productionOrder.getStatus();

        deliverOrderUseCase.execute(new DeliverOrderCommand(order.getId(), LocalDate.of(2026, 9, 12)));

        assertEquals(paymentsBefore, paymentRepository.findAll().size());
        assertTrue(paymentRepository.findByOrderId(order.getId()).isEmpty());
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());
        ProductionOrder reloadedProduction = productionOrderRepository.findById(productionOrder.getId()).orElseThrow();
        assertEquals(productionStatusBefore, reloadedProduction.getStatus());
        assertEquals(productionOrder.getCreationDate(), reloadedProduction.getCreationDate());
    }

    private void assertNullDate(UUID orderId) {
        assertNull(orderRepository.findById(orderId).orElseThrow().getActualDeliveryDate());
    }

    private Order saveOrder(OrderStatus status) {
        return save(status, null);
    }

    private Order saveDelivered(LocalDate actualDeliveryDate) {
        return save(OrderStatus.DELIVERED, actualDeliveryDate);
    }

    private Order save(OrderStatus status, LocalDate actualDeliveryDate) {
        LocalDate confirmation = LocalDate.of(2026, 8, 10);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta entrega",
                1,
                "Algodón",
                "Blanco",
                Money.of(new BigDecimal("150000.00")),
                ProductSpecification.empty(),
                List.of()
        );
        Order created = Order.create(
                OrderNumber.of("ORD-DLV-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmation,
                DeliveryCommitment.of(confirmation.plusDays(14)),
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
                actualDeliveryDate
        ));
    }
}
