package com.magyen.platform.commercial.presentation;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentAmount;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class OrderLifecycleApiContractTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 23);
    private static final LocalDate CONFIRMATION = LocalDate.of(2026, 8, 10);

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private Clock clock;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.systemDefault();
        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(zone).toInstant());
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void explicitLifecycleReachesDeliveredAndCloseRequiresCollectedPayments() throws Exception {
        Order order = save(OrderStatus.CONFIRMED, null);

        mockMvc.perform(patch("/api/v1/orders/{orderId}/start-production", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PRODUCTION"));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/ready-for-delivery", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY_FOR_DELIVERY"));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-09-18\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.actualDeliveryDate").value("2026-09-18"));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-09-18\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actualDeliveryDate").value("2026-09-18"));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-09-19\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/v1/orders/{orderId}/close", order.getId()))
                .andExpect(status().isBadRequest());

        assertEquals(OrderStatus.DELIVERED, orderRepository.findById(order.getId()).orElseThrow().getStatus());

        paymentRepository.save(Payment.create(
                order.getId(),
                PaymentAmount.of(order.getTotal().getAmount()),
                LocalDate.of(2026, 9, 18),
                "covered in test"
        ));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/close", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.finalPaymentAcknowledged").value(true));
    }

    @Test
    void invalidDeliveryPayloadsAreRejected() throws Exception {
        Order order = save(OrderStatus.READY_FOR_DELIVERY, null);

        mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-13-40\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-08-01\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-09-24\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/orders/{orderId}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY_FOR_DELIVERY"))
                .andExpect(jsonPath("$.actualDeliveryDate").isEmpty());
    }

    @Test
    void invalidForwardTransitionsAreRejected() throws Exception {
        Order confirmed = save(OrderStatus.CONFIRMED, null);

        mockMvc.perform(patch("/api/v1/orders/{orderId}/ready-for-delivery", confirmed.getId()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/v1/orders/{orderId}/close", confirmed.getId()))
                .andExpect(status().isBadRequest());

        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(confirmed.getId()).orElseThrow().getStatus());
    }

    private Order save(OrderStatus status, LocalDate actualDeliveryDate) {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta ciclo",
                1,
                "Algodón",
                "Blanco",
                Money.of(new BigDecimal("120000.00")),
                ProductSpecification.empty(),
                List.of()
        );
        Order created = Order.create(
                OrderNumber.of("ORD-API-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                CONFIRMATION,
                DeliveryCommitment.of(CONFIRMATION.plusDays(20)),
                UUID.randomUUID(),
                "lifecycle api",
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
