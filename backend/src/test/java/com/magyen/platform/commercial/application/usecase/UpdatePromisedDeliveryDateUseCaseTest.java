package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.UpdatePromisedDeliveryDateCommand;
import com.magyen.platform.commercial.application.dto.UpdatePromisedDeliveryDateResult;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePromisedDeliveryDateUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    private UpdatePromisedDeliveryDateUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePromisedDeliveryDateUseCase(orderRepository);
    }

    @Test
    void updatesThePromisedDateAndLeavesActualDeliveryAndTotalUntouched() {
        Order order = order();
        BigDecimal totalBefore = order.getTotal().getAmount();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        UpdatePromisedDeliveryDateResult result = useCase.execute(new UpdatePromisedDeliveryDateCommand(
                order.getId(),
                LocalDate.of(2026, 8, 11)
        ));

        assertEquals(LocalDate.of(2026, 8, 11), result.promisedDeliveryDate());
        assertNull(result.actualDeliveryDate());
        assertEquals(OrderStatus.CONFIRMED, result.status());
        assertEquals(order.getConfirmationDate(), result.confirmationDate());
        assertEquals(totalBefore, result.totalAmount());
        assertEquals("Ventanilla", order.getDeliveryCommitment().getDeliveryObservations());
        assertEquals("Observación", order.getObservations());
        verify(orderRepository).save(order);
    }

    @Test
    void doesNotSaveAClosedOrder() {
        Order created = order();
        Order closed = Order.reconstitute(
                created.getId(),
                created.getOrderNumber(),
                created.getCustomerId(),
                created.getQuotationId(),
                created.getConfirmationDate(),
                OrderStatus.CLOSED,
                created.getDeliveryCommitment(),
                created.getPaymentSummary(),
                created.getSellerId(),
                created.getObservations(),
                created.getDescription(),
                created.getItems(),
                created.getDiscount()
        );
        when(orderRepository.findById(closed.getId())).thenReturn(Optional.of(closed));

        assertThrows(
                OrderDomainException.class,
                () -> useCase.execute(new UpdatePromisedDeliveryDateCommand(
                        closed.getId(),
                        LocalDate.of(2026, 9, 1)
                ))
        );
        verify(orderRepository, never()).save(any());
        assertEquals(
                LocalDate.of(2026, 8, 20),
                closed.getDeliveryCommitment().getPromisedDeliveryDate()
        );
    }

    private static Order order() {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta",
                1,
                "Hydrotech",
                "Blanco",
                Money.of(new BigDecimal("20000")),
                ProductSpecification.empty(),
                List.of()
        );
        return Order.create(
                OrderNumber.of("ORD-DATE-1"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 10),
                DeliveryCommitment.of(LocalDate.of(2026, 8, 20), "Ventanilla"),
                UUID.randomUUID(),
                "Observación",
                List.of(item)
        );
    }
}
