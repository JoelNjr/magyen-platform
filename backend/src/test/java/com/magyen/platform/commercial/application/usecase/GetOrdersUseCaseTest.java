package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CustomerNameResolver;
import com.magyen.platform.commercial.application.SellerNameResolver;
import com.magyen.platform.commercial.application.dto.GetOrdersQuery;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrdersUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private SellerNameResolver sellerNameResolver;

    @Mock
    private CustomerNameResolver customerNameResolver;

    private GetOrdersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetOrdersUseCase(
                orderRepository,
                quotationRepository,
                sellerNameResolver,
                customerNameResolver
        );
        when(sellerNameResolver.nameLookup(any())).thenReturn(id -> "Seller");
        when(customerNameResolver.nameLookup(any())).thenReturn(id -> "Customer");
        when(quotationRepository.findAll()).thenReturn(List.of());
    }

    @Test
    void listsOrdersByNumericCommercialNumberNotLexicographicText() {
        List<Order> shuffled = new ArrayList<>();
        IntStream.rangeClosed(1, 12).forEach(number -> shuffled.add(order(String.valueOf(number), LocalDate.of(2026, 8, 1))));
        Collections.shuffle(shuffled);
        when(orderRepository.findAll()).thenReturn(shuffled);

        List<String> numbers = useCase.execute().orders().stream()
                .map(OrderResult::orderNumber)
                .toList();

        assertEquals(
                List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"),
                numbers
        );
    }

    @Test
    void updatedOrderDoesNotJumpToTheFront() {
        Order first = order("13", LocalDate.of(2026, 8, 2));
        Order second = order("14", LocalDate.of(2026, 8, 3));
        Order third = order("15", LocalDate.of(2026, 8, 1));
        when(orderRepository.findAll()).thenReturn(List.of(third, first, second));

        List<String> numbers = useCase.execute().orders().stream()
                .map(OrderResult::orderNumber)
                .toList();

        assertEquals(List.of("13", "14", "15"), numbers);
    }

    @Test
    void confirmationDateFilterStillAppliesAndKeepsConsecutiveOrder() {
        Order inRangeHigh = order("20", LocalDate.of(2026, 8, 10));
        Order inRangeLow = order("9", LocalDate.of(2026, 8, 20));
        Order outOfRange = order("11", LocalDate.of(2026, 9, 1));
        when(orderRepository.findAll()).thenReturn(List.of(inRangeHigh, outOfRange, inRangeLow));

        List<String> numbers = useCase.execute(
                new GetOrdersQuery(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))
        ).orders().stream().map(OrderResult::orderNumber).toList();

        assertEquals(List.of("9", "20"), numbers);
    }

    private static Order order(String orderNumber, LocalDate confirmationDate) {
        Money price = Money.of(new BigDecimal("1000"));
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto",
                1,
                "Sudáfrica",
                "Blanco",
                price,
                ProductSpecification.empty(),
                List.of()
        );
        return Order.reconstitute(
                UUID.randomUUID(),
                OrderNumber.of(orderNumber),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmationDate,
                OrderStatus.CONFIRMED,
                DeliveryCommitment.of(confirmationDate.plusDays(7)),
                PaymentSummary.forConfirmedOrder(price),
                UUID.randomUUID(),
                "Observación " + orderNumber,
                List.of(item)
        );
    }
}
