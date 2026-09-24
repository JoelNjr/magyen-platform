package com.magyen.platform.commercial.presentation;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.ProductSpecification;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class OrderProfitabilityMonthApiContractTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 14);

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderRepository orderRepository;

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
    void omittedDatesDefaultToCurrentMonthAndEchoPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/orders/profitability").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromDate").value("2026-09-01"))
                .andExpect(jsonPath("$.toDate").value("2026-09-30"));
    }

    @Test
    void selectedPeriodIsReflectedInResponse() throws Exception {
        mockMvc.perform(
                        get("/api/v1/orders/profitability")
                                .param("fromDate", "2026-08-01")
                                .param("toDate", "2026-08-31")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromDate").value("2026-08-01"))
                .andExpect(jsonPath("$.toDate").value("2026-08-31"));
    }

    @Test
    void oneParameterAloneIsRejected() throws Exception {
        mockMvc.perform(
                        get("/api/v1/orders/profitability")
                                .param("fromDate", "2026-09-01")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deliversReadyForDeliveryOrder() throws Exception {
        Order order = saveOrder(OrderStatus.READY_FOR_DELIVERY, null);

        mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-09-10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId().toString()))
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.actualDeliveryDate").value("2026-09-10"));
    }

    @Test
    void confirmedCannotBeDeliveredThroughApi() throws Exception {
        Order order = saveOrder(OrderStatus.CONFIRMED, null);

        mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-09-10\"}"))
                .andExpect(status().isBadRequest());
    }

    private Order saveOrder(OrderStatus status, LocalDate actualDeliveryDate) {
        LocalDate confirmation = LocalDate.of(2026, 8, 10);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta API mes",
                1,
                "Tela",
                "Negro",
                Money.of(new BigDecimal("200000.00")),
                ProductSpecification.empty(),
                List.of()
        );
        Order created = Order.create(
                OrderNumber.of("ORD-APIM-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmation,
                DeliveryCommitment.of(confirmation.plusDays(7)),
                UUID.randomUUID(),
                "API mes",
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
