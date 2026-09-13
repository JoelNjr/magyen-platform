package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationItem;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.QuotationStatus;
import com.magyen.platform.commercial.domain.exception.OrderAlreadyExistsForQuotationException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderFromQuotationUseCaseTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private OrderRepository orderRepository;

    private CreateOrderFromQuotationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateOrderFromQuotationUseCase(quotationRepository, orderRepository);
    }

    @Test
    void assignsOrderNumberFromReservedQuotationNumber() {
        Quotation quotation = approvedQuotation(QuotationNumber.of(14L));
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(orderRepository.findByQuotationId(quotation.getId())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderFromQuotationResult result = useCase.execute(new CreateOrderFromQuotationCommand(
                quotation.getId(),
                "Pedido de uniformes",
                LocalDate.of(2026, 8, 23),
                quotation.getDeliveryDate(),
                quotation.getObservations()
        ));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        assertEquals("14", orderCaptor.getValue().getOrderNumber().getValue());
        assertEquals("14", result.orderNumber());
    }

    @Test
    void storesQuotationItemIdOnEachNewOrderItemAndPreservesCommercialValues() {
        ProductSpecification specification = ProductSpecification.of(
                "Camiseta",
                "Redondo",
                "Manga corta sisa",
                true,
                true,
                false,
                false,
                "Full print",
                true,
                false,
                false,
                null,
                "Prioridad"
        );
        Quotation quotation = approvedQuotationWithItems(
                QuotationNumber.of(21L),
                Money.of(new BigDecimal("10000")),
                new QuotedProduct("Camiseta local", 10, "Sudáfrica", "Blanco", "96000", specification),
                new QuotedProduct("Pantaloneta", 5, "Microfibra", "Negro", "40000", ProductSpecification.empty())
        );
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(orderRepository.findByQuotationId(quotation.getId())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new CreateOrderFromQuotationCommand(
                quotation.getId(),
                "Pedido de uniformes",
                LocalDate.of(2026, 8, 23),
                quotation.getDeliveryDate(),
                quotation.getObservations()
        ));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order order = orderCaptor.getValue();

        assertEquals(quotation.getId(), order.getQuotationId());
        assertEquals(Long.toString(quotation.getQuotationNumber().getValue()), order.getOrderNumber().getValue());
        assertEquals(Money.of(new BigDecimal("10000")), order.getDiscount());
        assertEquals(quotation.getTotal(), order.getTotal());
        assertEquals(2, order.getItems().size());

        List<UUID> quotationItemIds = quotation.getItems().stream().map(QuotationItem::getId).toList();
        List<UUID> mappedQuotationItemIds = order.getItems().stream().map(OrderItem::getQuotationItemId).toList();
        assertEquals(quotationItemIds, mappedQuotationItemIds);
        assertEquals(2, mappedQuotationItemIds.stream().distinct().count());

        OrderItem first = findItemByQuotationItemId(order, quotation.getItems().get(0).getId());
        assertEquals("Camiseta local", first.getProductName());
        assertEquals(10, first.getQuantity());
        assertEquals("Sudáfrica", first.getFabric());
        assertEquals("Blanco", first.getColor());
        assertEquals(Money.of(new BigDecimal("96000")), first.getUnitPrice());
        assertEquals("Camiseta", first.getProductSpecification().getGarmentType());
        assertEquals("Redondo", first.getProductSpecification().getCollarType());
        assertTrue(first.getSizeBreakdowns().isEmpty());

        OrderItem second = findItemByQuotationItemId(order, quotation.getItems().get(1).getId());
        assertEquals("Pantaloneta", second.getProductName());
        assertEquals(5, second.getQuantity());
        assertEquals(Money.of(new BigDecimal("40000")), second.getUnitPrice());
        assertTrue(second.getProductSpecification().isEmpty());
        assertTrue(second.getSizeBreakdowns().isEmpty());
    }

    @Test
    void rejectsSecondOrderForTheSameQuotation() {
        Quotation quotation = approvedQuotation(QuotationNumber.of(22L));
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(orderRepository.findByQuotationId(quotation.getId())).thenReturn(Optional.of(
                Order.create(
                        OrderNumber.of("22"),
                        quotation.getCustomerId(),
                        quotation.getId(),
                        LocalDate.of(2026, 8, 23),
                        DeliveryCommitment.of(quotation.getDeliveryDate()),
                        quotation.getSellerId(),
                        null,
                        List.of(OrderItem.reconstitute(
                                UUID.randomUUID(),
                                "Uniformes de futbol",
                                10,
                                "Sudáfrica",
                                "Blanco",
                                Money.of(new BigDecimal("96000")),
                                ProductSpecification.empty(),
                                List.of(),
                                quotation.getItems().getFirst().getId()
                        ))
                )
        ));

        assertThrows(
                OrderAlreadyExistsForQuotationException.class,
                () -> useCase.execute(new CreateOrderFromQuotationCommand(
                        quotation.getId(),
                        null,
                        LocalDate.of(2026, 8, 24),
                        quotation.getDeliveryDate(),
                        null
                ))
        );
    }

    @Test
    void rejectsOrderWhenQuotationHasNoReservedNumber() {
        Quotation quotation = historicalApprovedQuotationWithoutNumber();
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(orderRepository.findByQuotationId(quotation.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(new CreateOrderFromQuotationCommand(
                        quotation.getId(),
                        null,
                        LocalDate.of(2026, 8, 23),
                        quotation.getDeliveryDate(),
                        null
                ))
        );

        assertEquals(
                "Quotation has no commercial number reserved; an order cannot be created",
                exception.getMessage()
        );
    }

    private static Quotation approvedQuotation(QuotationNumber quotationNumber) {
        return approvedQuotationWithItems(
                quotationNumber,
                Money.zero(),
                new QuotedProduct(
                        "Uniformes de futbol",
                        10,
                        "Sudáfrica",
                        "Blanco",
                        "96000",
                        ProductSpecification.empty()
                )
        );
    }

    private static Quotation approvedQuotationWithItems(
            QuotationNumber quotationNumber,
            Money discount,
            QuotedProduct... products
    ) {
        Quotation quotation = Quotation.create(
                quotationNumber,
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 25),
                UUID.randomUUID(),
                "Observaciones"
        );
        for (QuotedProduct product : products) {
            quotation.addItem(
                    product.productName(),
                    product.quantity(),
                    product.fabric(),
                    product.color(),
                    Money.of(new BigDecimal(product.unitPrice())),
                    product.specification()
            );
        }
        if (!discount.equals(Money.zero())) {
            quotation.applyDiscount(discount);
        }
        quotation.approve();
        return quotation;
    }

    private static OrderItem findItemByQuotationItemId(Order order, UUID quotationItemId) {
        return order.getItems().stream()
                .filter(item -> quotationItemId.equals(item.getQuotationItemId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Order item not found for quotation item " + quotationItemId));
    }

    private record QuotedProduct(
            String productName,
            int quantity,
            String fabric,
            String color,
            String unitPrice,
            ProductSpecification specification
    ) {
    }

    private static Quotation historicalApprovedQuotationWithoutNumber() {
        Quotation draft = Quotation.create(
                QuotationNumber.of(1L),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 25),
                UUID.randomUUID(),
                null
        );
        draft.addItem(
                "Camiseta",
                5,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("40000"))
        );

        return Quotation.reconstitute(
                draft.getId(),
                null,
                draft.getCustomerId(),
                draft.getCreationDate(),
                draft.getDeliveryDate(),
                QuotationStatus.APPROVED,
                draft.getSellerId(),
                draft.getObservations(),
                List.copyOf(draft.getItems())
        );
    }
}
