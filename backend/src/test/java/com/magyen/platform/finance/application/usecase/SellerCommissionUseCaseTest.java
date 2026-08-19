package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase;
import com.magyen.platform.commercial.application.usecase.GetSellersUseCase;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollDeductionCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.GetPayrollDeductionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeFinancialSummaryQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeFinancialSummaryResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeePerformanceQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsResult;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollDeductionType;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class SellerCommissionUseCaseTest {

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private DeactivatePayrollEmployeeUseCase deactivatePayrollEmployeeUseCase;

    @Autowired
    private GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase;

    @Autowired
    private GetPayrollEmployeePerformanceUseCase getPayrollEmployeePerformanceUseCase;

    @Autowired
    private GetPayrollEmployeeFinancialSummaryUseCase getPayrollEmployeeFinancialSummaryUseCase;

    @Autowired
    private GetPayrollEmployeeProductionEarningsUseCase getPayrollEmployeeProductionEarningsUseCase;

    @Autowired
    private CreatePayrollDeductionUseCase createPayrollDeductionUseCase;

    @Autowired
    private GetPayrollDeductionsUseCase getPayrollDeductionsUseCase;

    @Autowired
    private GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;

    @Autowired
    private GetSellersUseCase getSellersUseCase;

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;

    @Autowired
    private CreateQuotationUseCase createQuotationUseCase;

    @Autowired
    private AddQuotationItemUseCase addQuotationItemUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void commissionIsFivePercentOfCompletedSalesAndDoesNotTouchLedgerOrProfitability() {
        CreatePayrollEmployeeResult seller = createFixed("Vendedor-H-" + suffix());
        Order first = saveOrder(seller.employeeId(), OrderStatus.DELIVERED, "500000.00", LocalDate.of(2026, 8, 10));
        saveOrder(seller.employeeId(), OrderStatus.CLOSED, "400000.00", LocalDate.of(2026, 8, 12));

        long financeBefore = financialTransactionRepository.findAllNewestFirst().size();
        GetPayrollEmployeeCommissionsResult result = getPayrollEmployeeCommissionsUseCase.execute(
                new GetPayrollEmployeeCommissionsQuery(seller.employeeId(), null, null)
        );

        assertTrue(result.sellerCommissionApplicable());
        assertEquals(2, result.numberOfEligibleOrders());
        assertEquals(new BigDecimal("900000.00"), result.totalSales());
        assertEquals(new BigDecimal("5.00"), result.commissionRate());
        assertEquals(new BigDecimal("45000.00"), result.accumulatedCommission());
        assertEquals(financeBefore, financialTransactionRepository.findAllNewestFirst().size());

        GetOrderProfitabilityResult profitability = getOrderProfitabilityUseCase.execute(
                new GetOrderProfitabilityQuery(first.getId())
        );
        assertEquals(new BigDecimal("500000.00"), profitability.orderValue());
        assertEquals(new BigDecimal("500000.00"), profitability.directProfit());
    }

    @Test
    void quotationOnlyAndConfirmedOrdersDoNotGenerateCommission() {
        CreatePayrollEmployeeResult seller = createFixed("Vendedor-H-inc-" + suffix());
        var customer = createCustomerUseCase.execute(new CreateCustomerCommand("Cliente H " + suffix()));
        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customer.customerId(),
                LocalDate.of(2026, 8, 20),
                seller.employeeId(),
                null,
                LocalDate.of(2026, 8, 10)
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Producto cotizado H",
                1,
                "Sudáfrica",
                "Negro",
                new BigDecimal("800000"),
                null
        ));
        saveOrder(seller.employeeId(), OrderStatus.CONFIRMED, "800000.00", LocalDate.of(2026, 8, 11));

        GetPayrollEmployeeCommissionsResult result = getPayrollEmployeeCommissionsUseCase.execute(
                new GetPayrollEmployeeCommissionsQuery(seller.employeeId(), null, null)
        );
        assertEquals(0, result.numberOfEligibleOrders());
        assertEquals(new BigDecimal("0.00"), result.totalSales());
        assertEquals(new BigDecimal("0.00"), result.accumulatedCommission());
    }

    @Test
    void productionBasedEmployeeHasNoSellerCommission() {
        CreatePayrollEmployeeResult operator = createProduction("Operario-H-" + suffix());
        GetPayrollEmployeeCommissionsResult result = getPayrollEmployeeCommissionsUseCase.execute(
                new GetPayrollEmployeeCommissionsQuery(operator.employeeId(), null, null)
        );
        assertFalse(result.sellerCommissionApplicable());
        assertEquals(new BigDecimal("0.00"), result.accumulatedCommission());
        assertFalse(getSellersUseCase.execute().sellers().stream()
                .anyMatch(seller -> operator.employeeId().equals(seller.sellerId())));
    }

    @Test
    void inactiveSellerKeepsHistoricalCommissionButLeavesSelector() {
        CreatePayrollEmployeeResult seller = createFixed("Vendedor-H-inact-" + suffix());
        saveOrder(seller.employeeId(), OrderStatus.DELIVERED, "200000.00", LocalDate.of(2026, 8, 5));
        deactivatePayrollEmployeeUseCase.execute(new DeactivatePayrollEmployeeCommand(seller.employeeId()));

        GetPayrollEmployeeCommissionsResult result = getPayrollEmployeeCommissionsUseCase.execute(
                new GetPayrollEmployeeCommissionsQuery(seller.employeeId(), null, null)
        );
        assertEquals(new BigDecimal("10000.00"), result.accumulatedCommission());
        assertFalse(result.eligibleForNewQuotations());
        assertFalse(getSellersUseCase.execute().sellers().stream()
                .anyMatch(item -> seller.employeeId().equals(item.sellerId())));
    }

    @Test
    void dateRangeIncludesOnlyOrdersConfirmedInsidePeriod() {
        CreatePayrollEmployeeResult seller = createFixed("Vendedor-H-rango-" + suffix());
        saveOrder(seller.employeeId(), OrderStatus.DELIVERED, "300000.00", LocalDate.of(2026, 3, 10));
        saveOrder(seller.employeeId(), OrderStatus.DELIVERED, "700000.00", LocalDate.of(2026, 8, 10));

        GetPayrollEmployeeCommissionsResult august = getPayrollEmployeeCommissionsUseCase.execute(
                new GetPayrollEmployeeCommissionsQuery(
                        seller.employeeId(),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );
        assertEquals(1, august.numberOfEligibleOrders());
        assertEquals(new BigDecimal("700000.00"), august.totalSales());
        assertEquals(new BigDecimal("35000.00"), august.accumulatedCommission());
    }

    @Test
    void employeeWithoutOrdersHasZeroCommissionAndSummaryKeepsDeductionsAndProduction() {
        CreatePayrollEmployeeResult seller = createFixed("Vendedor-H-vacio-" + suffix());
        CreatePayrollEmployeeResult operator = createProduction("Operario-H-sum-" + suffix());
        createPayrollDeductionUseCase.execute(new CreatePayrollDeductionCommand(
                seller.employeeId(),
                PayrollDeductionType.LOAN,
                new BigDecimal("80000.00"),
                LocalDate.of(2026, 8, 2),
                "préstamo H"
        ));

        GetPayrollEmployeeCommissionsResult empty = getPayrollEmployeeCommissionsUseCase.execute(
                new GetPayrollEmployeeCommissionsQuery(seller.employeeId(), null, null)
        );
        assertEquals(0, empty.numberOfEligibleOrders());
        assertEquals(new BigDecimal("0.00"), empty.accumulatedCommission());

        GetPayrollEmployeeFinancialSummaryResult sellerSummary = getPayrollEmployeeFinancialSummaryUseCase.execute(
                new GetPayrollEmployeeFinancialSummaryQuery(seller.employeeId(), null, null)
        );
        assertTrue(sellerSummary.sellerCommissionApplicable());
        assertFalse(sellerSummary.productionLaborApplicable());
        assertEquals(1, sellerSummary.activeDeductionCount());
        assertEquals(new BigDecimal("80000.00"), sellerSummary.activeDeductionTotal());
        assertEquals(new BigDecimal("0.00"), sellerSummary.productionGenerated());

        GetPayrollEmployeeProductionEarningsResult production = getPayrollEmployeeProductionEarningsUseCase.execute(
                new GetPayrollEmployeeProductionEarningsQuery(
                        operator.employeeId(),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );
        assertTrue(production.productionLaborApplicable());
        assertEquals(new BigDecimal("0.00"), production.totalCalculatedAmount());

        assertTrue(getPayrollEmployeePerformanceUseCase.execute(
                new GetPayrollEmployeePerformanceQuery(null, null)
        ).sellers().stream().anyMatch(item -> seller.employeeId().equals(item.employeeId())));

        assertEquals(1, getPayrollDeductionsUseCase.execute(
                new GetPayrollDeductionsQuery(seller.employeeId(), null)
        ).activeCount());
    }

    @Test
    void performancePopulatesFixedPayrollSellersAndExcludesProductionBased() {
        CreatePayrollEmployeeResult activeSeller = createFixed("Vendedor-I-act-" + suffix());
        CreatePayrollEmployeeResult inactiveSeller = createFixed("Vendedor-I-inact-" + suffix());
        CreatePayrollEmployeeResult operator = createProduction("Operario-I-perf-" + suffix());
        saveOrder(activeSeller.employeeId(), OrderStatus.DELIVERED, "200000.00", LocalDate.of(2026, 8, 5));
        saveOrder(inactiveSeller.employeeId(), OrderStatus.CLOSED, "400000.00", LocalDate.of(2026, 8, 6));
        deactivatePayrollEmployeeUseCase.execute(new DeactivatePayrollEmployeeCommand(inactiveSeller.employeeId()));

        long financeBefore = financialTransactionRepository.findAllNewestFirst().size();
        var result = getPayrollEmployeePerformanceUseCase.execute(
                new GetPayrollEmployeePerformanceQuery(null, null)
        );
        assertEquals(financeBefore, financialTransactionRepository.findAllNewestFirst().size());

        var activeRow = result.sellers().stream()
                .filter(item -> activeSeller.employeeId().equals(item.employeeId()))
                .findFirst()
                .orElseThrow();
        assertEquals(activeSeller.displayName(), activeRow.displayName());
        assertTrue(activeRow.active());
        assertEquals(1, activeRow.numberOfEligibleOrders());
        assertEquals(new BigDecimal("200000.00"), activeRow.totalSales());
        assertEquals(new BigDecimal("10000.00"), activeRow.accumulatedCommission());

        var inactiveRow = result.sellers().stream()
                .filter(item -> inactiveSeller.employeeId().equals(item.employeeId()))
                .findFirst()
                .orElseThrow();
        assertFalse(inactiveRow.active());
        assertEquals(new BigDecimal("20000.00"), inactiveRow.accumulatedCommission());

        assertTrue(result.sellers().stream()
                .noneMatch(item -> operator.employeeId().equals(item.employeeId())));
    }

    private Order saveOrder(UUID sellerId, OrderStatus status, String unitPrice, LocalDate confirmationDate) {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto comisión H",
                1,
                "Sudáfrica",
                "Negro",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );
        Money total = item.getSubtotal();
        Order order = Order.reconstitute(
                UUID.randomUUID(),
                OrderNumber.of("ORD-H-" + suffix()),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmationDate,
                status,
                DeliveryCommitment.of(confirmationDate.plusDays(7)),
                PaymentSummary.forConfirmedOrder(total),
                sellerId,
                null,
                "Pedido comisión H",
                List.of(item)
        );
        return orderRepository.save(order);
    }

    private CreatePayrollEmployeeResult createFixed(String name) {
        return createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                name,
                PayrollCompensationType.FIXED_PAYROLL,
                new BigDecimal("1500000.00"),
                LocalDate.of(2026, 8, 1),
                null
        ));
    }

    private CreatePayrollEmployeeResult createProduction(String name) {
        return createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                name,
                PayrollCompensationType.PRODUCTION_BASED,
                null,
                null,
                null
        ));
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
