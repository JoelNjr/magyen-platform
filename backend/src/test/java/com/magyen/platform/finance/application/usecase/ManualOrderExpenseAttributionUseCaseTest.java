package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CloseOrderCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.DeliverOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.MarkOrderReadyForDeliveryCommand;
import com.magyen.platform.commercial.application.dto.StartOrderProductionCommand;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CloseOrderUseCase;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.DeliverOrderUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase;
import com.magyen.platform.commercial.application.usecase.MarkOrderReadyForDeliveryUseCase;
import com.magyen.platform.commercial.application.usecase.StartOrderProductionUseCase;
import com.magyen.platform.commercial.domain.OrderProfitabilityStatus;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ManualOrderExpenseAttributionUseCaseTest {

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;

    @Autowired
    private CreateQuotationUseCase createQuotationUseCase;

    @Autowired
    private AddQuotationItemUseCase addQuotationItemUseCase;

    @Autowired
    private ApproveQuotationUseCase approveQuotationUseCase;

    @Autowired
    private CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase;

    @Autowired
    private StartOrderProductionUseCase startOrderProductionUseCase;

    @Autowired
    private MarkOrderReadyForDeliveryUseCase markOrderReadyForDeliveryUseCase;

    @Autowired
    private DeliverOrderUseCase deliverOrderUseCase;

    @Autowired
    private CloseOrderUseCase closeOrderUseCase;

    @Autowired
    private RegisterPaymentUseCase registerPaymentUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase;

    @Autowired
    private GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void attributesOneManualExpenseToOrderOtherCostsWithoutDuplicatingIt() {
        UUID orderId = createOrder("Colegio medias " + UUID.randomUUID());
        UUID otherOrderId = createOrder("Colegio sin gasto " + UUID.randomUUID());
        long before = financialTransactionRepository.findAllNewestFirst().size();

        var unassigned = registerFinancialTransactionUseCase.execute(expense(null, "40.00", "Gasto general"));
        var assigned = registerFinancialTransactionUseCase.execute(expense(orderId, "180.00", "Medias"));

        assertNull(unassigned.orderId());
        assertEquals(orderId, assigned.orderId());
        assertEquals(FinancialTransactionSourceType.MANUAL, assigned.sourceType());
        assertEquals(before + 2, financialTransactionRepository.findAllNewestFirst().size());
        assertEquals(1, financialTransactionRepository.findByOrderId(orderId).size());
        assertEquals(
                assigned.transactionId(),
                financialTransactionRepository.findByOrderId(orderId).getFirst().getId()
        );

        var profitability = getOrderProfitabilityUseCase.execute(new GetOrderProfitabilityQuery(orderId));
        assertEquals(new BigDecimal("180.00"), profitability.otherDirectCost());
        assertEquals(new BigDecimal("180.00"), profitability.totalDirectCost());
        assertEquals(OrderProfitabilityStatus.COMPLETE, profitability.profitabilityStatus());
        assertEquals(0, profitability.orderValue().subtract(new BigDecimal("180.00"))
                .compareTo(profitability.directProfit()));

        var other = getOrderProfitabilityUseCase.execute(new GetOrderProfitabilityQuery(otherOrderId));
        assertEquals(new BigDecimal("0.00"), other.otherDirectCost());
        assertEquals(OrderProfitabilityStatus.NO_COST_DATA, other.profitabilityStatus());
    }

    @Test
    void rejectsIncomeAndClosedOrderAssociations() {
        UUID closedOrderId = createOrder("Pedido cerrado gasto " + UUID.randomUUID());
        startOrderProductionUseCase.execute(new StartOrderProductionCommand(closedOrderId));
        markOrderReadyForDeliveryUseCase.execute(new MarkOrderReadyForDeliveryCommand(closedOrderId));
        deliverOrderUseCase.execute(new DeliverOrderCommand(closedOrderId, LocalDate.now()));
        var delivered = orderRepository.findById(closedOrderId).orElseThrow();
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                closedOrderId,
                delivered.getTotal().getAmount(),
                LocalDate.now(),
                "cobro que cubre el total"
        ));
        closeOrderUseCase.execute(new CloseOrderCommand(closedOrderId));

        FinanceDomainException income = assertThrows(
                FinanceDomainException.class,
                () -> registerFinancialTransactionUseCase.execute(new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        new BigDecimal("10.00"),
                        LocalDate.now(),
                        "SALES",
                        "Ingreso",
                        null,
                        FinancialTransactionSourceType.MANUAL,
                        null,
                        UUID.randomUUID()
                ))
        );
        assertTrue(income.getMessage().contains("Only an expense"));

        FinanceDomainException closed = assertThrows(
                FinanceDomainException.class,
                () -> registerFinancialTransactionUseCase.execute(expense(closedOrderId, "15.00", "Tarde"))
        );
        assertTrue(closed.getMessage().contains("cannot receive"));

        FinanceDomainException missing = assertThrows(
                FinanceDomainException.class,
                () -> registerFinancialTransactionUseCase.execute(expense(UUID.randomUUID(), "15.00", "Fantasma"))
        );
        assertTrue(missing.getMessage().contains("Order not found"));
    }

    private RegisterFinancialTransactionCommand expense(UUID orderId, String amount, String description) {
        return new RegisterFinancialTransactionCommand(
                FinancialTransactionType.EXPENSE,
                new BigDecimal(amount),
                LocalDate.now(),
                "OTHER_EXPENSE",
                description,
                null,
                FinancialTransactionSourceType.MANUAL,
                null,
                orderId
        );
    }

    private UUID createOrder(String customerName) {
        LocalDate today = LocalDate.now();
        UUID customerId = createCustomerUseCase.execute(new CreateCustomerCommand(customerName)).customerId();
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor gasto " + UUID.randomUUID()
        );
        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customerId,
                today.plusDays(4),
                sellerId,
                null,
                today
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Camiseta",
                2,
                "Sudáfrica",
                "Blanco",
                new BigDecimal("100.00"),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        return createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                customerName,
                today,
                today.plusDays(4),
                null
        )).orderId();
    }
}
