package com.magyen.platform.finance.presentation;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PayrollEmployeeCommissionApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private OrderRepository orderRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void exposesCommissionsSummaryAndPerformanceWithoutCreatingLedgerEntries() throws Exception {
        CreatePayrollEmployeeResult seller = createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                "API-Comision-" + UUID.randomUUID().toString().substring(0, 8),
                PayrollCompensationType.FIXED_PAYROLL,
                new BigDecimal("1500000.00"),
                LocalDate.of(2026, 8, 1),
                null
        ));
        saveDeliveredOrder(seller.employeeId(), "200000.00");

        mockMvc.perform(
                        get("/api/v1/finance/payroll/employees/{employeeId}/commissions", seller.employeeId())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellerCommissionApplicable").value(true))
                .andExpect(jsonPath("$.numberOfEligibleOrders").value(1))
                .andExpect(jsonPath("$.totalSales").value(200000.00))
                .andExpect(jsonPath("$.commissionRate").value(5.00))
                .andExpect(jsonPath("$.accumulatedCommission").value(10000.00));

        mockMvc.perform(
                        get("/api/v1/finance/payroll/employees/{employeeId}/summary", seller.employeeId())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compensationType").value("FIXED_PAYROLL"))
                .andExpect(jsonPath("$.accumulatedCommission").value(10000.00))
                .andExpect(jsonPath("$.productionLaborApplicable").value(false))
                .andExpect(jsonPath("$.activeDeductionTotal").value(0.00));

        mockMvc.perform(get("/api/v1/finance/payroll/employees/performance").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellers[*].employeeId", hasItem(seller.employeeId().toString())))
                .andExpect(jsonPath("$.sellers[?(@.employeeId=='" + seller.employeeId()
                        + "')].displayName").value(hasItem(seller.displayName())))
                .andExpect(jsonPath("$.sellers[?(@.employeeId=='" + seller.employeeId()
                        + "')].numberOfEligibleOrders").value(hasItem(1)))
                .andExpect(jsonPath("$.sellers[?(@.employeeId=='" + seller.employeeId()
                        + "')].totalSales").value(hasItem(200000.00)))
                .andExpect(jsonPath("$.sellers[?(@.employeeId=='" + seller.employeeId()
                        + "')].accumulatedCommission").value(hasItem(10000.00)))
                .andExpect(jsonPath("$.sellers[?(@.employeeId=='" + seller.employeeId()
                        + "')].commissionRate").value(hasItem(5.00)));
    }

    @Test
    void returnsBadRequestWhenEmployeeDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/v1/finance/payroll/employees/{employeeId}/summary", UUID.randomUUID())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    private void saveDeliveredOrder(UUID sellerId, String unitPrice) {
        LocalDate confirmationDate = LocalDate.of(2026, 8, 8);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto API comisión",
                1,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );
        Money total = item.getSubtotal();
        orderRepository.save(Order.reconstitute(
                UUID.randomUUID(),
                OrderNumber.of("ORD-HC-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmationDate,
                OrderStatus.DELIVERED,
                DeliveryCommitment.of(confirmationDate.plusDays(7)),
                PaymentSummary.forConfirmedOrder(total),
                sellerId,
                null,
                "Pedido API comisión",
                List.of(item)
        ));
    }
}
