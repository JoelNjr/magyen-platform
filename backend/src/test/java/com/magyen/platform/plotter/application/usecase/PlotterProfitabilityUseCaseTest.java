package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.InventoryAcquisitionCommand;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterProfitabilityQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterProfitabilityResult;
import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentCommand;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.PlotterProfitabilityScope;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PlotterProfitabilityUseCaseTest {

    private static final LocalDate PERIOD_START = LocalDate.of(2099, 3, 1);
    private static final LocalDate PERIOD_END = LocalDate.of(2099, 3, 31);
    private static final LocalDate JOB_DATE = LocalDate.of(2099, 3, 12);

    @Autowired
    private GetPlotterProfitabilityUseCase getPlotterProfitabilityUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private RegisterPlotterPaymentUseCase registerPlotterPaymentUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

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
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Test
    void usesPaperAcquisitionsNotConsumptionOutsAndKeepsInternalServiceAnalytical() {
        CreateInventoryItemResult roll = createPurchasedPaperRoll(
                "1",
                "200000.00",
                "100.0000",
                JOB_DATE
        );
        CommercialOrderFixture order = createCommercialOrder();

        CreatePlotterJobResult external = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                null,
                JOB_DATE,
                roll.inventoryItemId(),
                new BigDecimal("10.0000"),
                new BigDecimal("20000.00"),
                "externo G",
                PlotterJobType.EXTERNAL,
                null
        ));
        CreatePlotterJobResult internal = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                order.orderId(),
                JOB_DATE,
                roll.inventoryItemId(),
                new BigDecimal("6.0000"),
                new BigDecimal("8000.00"),
                "interno G",
                PlotterJobType.INTERNAL_MAGYEN,
                null
        ));

        long financeBefore = countAllFinancialTransactions();
        int externalOutsBefore = countPaperOutsFor(external.plotterJobId());
        int internalOutsBefore = countPaperOutsFor(internal.plotterJobId());

        GetPlotterProfitabilityResult all = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(PERIOD_START, PERIOD_END, PlotterProfitabilityScope.ALL)
        );

        assertEquals(financeBefore, countAllFinancialTransactions());
        assertEquals(externalOutsBefore, countPaperOutsFor(external.plotterJobId()));
        assertEquals(internalOutsBefore, countPaperOutsFor(internal.plotterJobId()));
        assertEquals(2, all.jobCount());
        assertEquals(1, all.externalJobCount());
        assertEquals(1, all.internalJobCount());
        assertEquals(new BigDecimal("16.0000"), all.totalPaperPrintedMeters());
        assertEquals(new BigDecimal("200000.00"), all.externalRevenue());
        assertEquals(new BigDecimal("0.00"), all.externalPaidAmount());
        assertEquals(new BigDecimal("200000.00"), all.externalOutstandingAmount());
        assertEquals(new BigDecimal("48000.00"), all.internalRevenue());
        assertEquals(new BigDecimal("248000.00"), all.combinedRevenue());
        assertEquals(new BigDecimal("200000.00"), all.totalPaperCost());
        assertEquals(new BigDecimal("6.0000"), all.internalPaperPrintedMeters());
        assertEquals(new BigDecimal("48000.00"), all.analyticalPlotterResult());
        assertTrue(all.inkCostRecorded());
        assertEquals(new BigDecimal("0.00"), all.inkCost());
        assertEquals(0, all.wasteJobCount());
        assertEquals(new BigDecimal("0.0000"), all.wastePrintedMeters());
        assertTrue(all.paperCostComplete());
        assertEquals(order.orderNumber(), all.internalOrders().getFirst().orderNumber());
        assertEquals(order.customerName(), all.internalOrders().getFirst().customerName());
        assertEquals(new BigDecimal("48000.00"), all.internalOrders().getFirst().serviceValue());

        GetPlotterProfitabilityResult internals = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(PERIOD_START, PERIOD_END, PlotterProfitabilityScope.INTERNAL)
        );
        assertEquals(0, internals.externalJobCount());
        assertEquals(new BigDecimal("0.00"), internals.externalRevenue());
        assertEquals(new BigDecimal("48000.00"), internals.internalRevenue());
        assertEquals(new BigDecimal("200000.00"), internals.totalPaperCost());
        assertEquals(new BigDecimal("-152000.00"), internals.analyticalPlotterResult());

        GetPlotterProfitabilityResult externals = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(PERIOD_START, PERIOD_END, PlotterProfitabilityScope.EXTERNAL)
        );
        assertEquals(0, externals.internalJobCount());
        assertEquals(new BigDecimal("200000.00"), externals.externalRevenue());
        assertTrue(externals.internalOrders().isEmpty());

        long incomeBeforePayment = countPlotterIncome();
        registerPlotterPaymentUseCase.execute(new RegisterPlotterPaymentCommand(
                external.plotterJobId(),
                new BigDecimal("200000.00"),
                JOB_DATE,
                "pago externo G"
        ));
        assertEquals(incomeBeforePayment + 1, countPlotterIncome());
        assertEquals(1, countPaperOutsFor(external.plotterJobId()));
        assertEquals(1, countPaperOutsFor(internal.plotterJobId()));

        GetPlotterProfitabilityResult afterPayment = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(PERIOD_START, PERIOD_END, PlotterProfitabilityScope.ALL)
        );
        assertEquals(new BigDecimal("200000.00"), afterPayment.externalPaidAmount());
        assertEquals(new BigDecimal("0.00"), afterPayment.externalOutstandingAmount());
        assertEquals(new BigDecimal("48000.00"), afterPayment.analyticalPlotterResult());
    }

    @Test
    void paperCostUsesPurchaseDateNotProductionOutDate() {
        createPurchasedPaperRoll("3", "200000.00", "300.0000", LocalDate.of(2099, 8, 10));
        GetPlotterProfitabilityResult august = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(
                        LocalDate.of(2099, 8, 1),
                        LocalDate.of(2099, 8, 31),
                        PlotterProfitabilityScope.ALL
                )
        );
        assertEquals(new BigDecimal("600000.00"), august.totalPaperCost());

        GetPlotterProfitabilityResult march = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(PERIOD_START, PERIOD_END, PlotterProfitabilityScope.ALL)
        );
        assertEquals(new BigDecimal("0.00"), march.totalPaperCost());
    }

    @Test
    void inkAcquisitionsAccumulateByPeriodAndSubtractFromResultWithoutJobConsumption() {
        createPurchasedPaperRoll("1", "200000.00", "100.0000", JOB_DATE);
        CreateInventoryItemResult firstInk = createPurchasedInk("100000.00", JOB_DATE);
        CreateInventoryItemResult secondInk = createPurchasedInk("150000.00", JOB_DATE);
        createPurchasedInk("90000.00", LocalDate.of(2099, 2, 10));

        CreatePlotterJobResult external = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                null,
                JOB_DATE,
                createPaperRoll("5000.00").inventoryItemId(),
                new BigDecimal("4.0000"),
                new BigDecimal("10000.00"),
                "externo tinta",
                PlotterJobType.EXTERNAL,
                null
        ));

        long financeBefore = countAllFinancialTransactions();
        GetPlotterProfitabilityResult march = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(PERIOD_START, PERIOD_END, PlotterProfitabilityScope.ALL)
        );

        assertTrue(march.inkCostRecorded());
        assertEquals(new BigDecimal("250000.00"), march.inkCost());
        assertEquals(new BigDecimal("200000.00"), march.totalPaperCost());
        assertEquals(new BigDecimal("40000.00"), march.externalRevenue());
        assertEquals(new BigDecimal("-410000.00"), march.analyticalPlotterResult());
        assertEquals(financeBefore, countAllFinancialTransactions());
        assertEquals(0, countInkOuts(firstInk.inventoryItemId()));
        assertEquals(0, countInkOuts(secondInk.inventoryItemId()));
        assertEquals(1, countPaperOutsFor(external.plotterJobId()));

        GetPlotterProfitabilityResult february = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(
                        LocalDate.of(2099, 2, 1),
                        LocalDate.of(2099, 2, 28),
                        PlotterProfitabilityScope.ALL
                )
        );
        assertEquals(new BigDecimal("90000.00"), february.inkCost());
        assertEquals(new BigDecimal("0.00"), february.totalPaperCost());

        GetPlotterProfitabilityResult empty = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(
                        LocalDate.of(2098, 1, 1),
                        LocalDate.of(2098, 1, 31),
                        PlotterProfitabilityScope.ALL
                )
        );
        assertEquals(new BigDecimal("0.00"), empty.inkCost());
        assertTrue(empty.inkCostRecorded());
    }

    @Test
    void dateFilterExcludesJobsOutsidePeriod() {
        CreateInventoryItemResult roll = createPaperRoll("5000.00");
        createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                null,
                LocalDate.of(2099, 1, 15),
                roll.inventoryItemId(),
                new BigDecimal("4.0000"),
                new BigDecimal("10000.00"),
                "fuera de periodo",
                PlotterJobType.EXTERNAL,
                null
        ));

        GetPlotterProfitabilityResult result = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(PERIOD_START, PERIOD_END, PlotterProfitabilityScope.ALL)
        );
        assertEquals(0, result.jobCount());
        assertEquals(new BigDecimal("0.0000"), result.totalPaperPrintedMeters());
    }

    @Test
    void externalCollectionSummarizesPaidAndOutstandingWithoutChangingAccounting() {
        CreateInventoryItemResult roll = createPaperRoll("5000.00");
        CommercialOrderFixture order = createCommercialOrder(
                LocalDate.of(2099, 5, 2),
                LocalDate.of(2099, 5, 20)
        );

        createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                null,
                LocalDate.of(2099, 5, 4),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("100000.00"),
                "externo sin pago",
                PlotterJobType.EXTERNAL,
                null
        ));
        CreatePlotterJobResult partial = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                null,
                LocalDate.of(2099, 5, 5),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("80000.00"),
                "externo parcial",
                PlotterJobType.EXTERNAL,
                null
        ));
        CreatePlotterJobResult paid = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                null,
                LocalDate.of(2099, 5, 6),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("50000.00"),
                "externo pagado",
                PlotterJobType.EXTERNAL,
                null
        ));
        createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                order.orderId(),
                LocalDate.of(2099, 5, 7),
                roll.inventoryItemId(),
                new BigDecimal("2.0000"),
                new BigDecimal("8000.00"),
                "interno cobranza",
                PlotterJobType.INTERNAL_MAGYEN,
                null
        ));
        createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                null,
                LocalDate.of(2099, 5, 8),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                BigDecimal.ZERO,
                "merma cobranza",
                PlotterJobType.WASTE,
                null
        ));

        registerPlotterPaymentUseCase.execute(new RegisterPlotterPaymentCommand(
                partial.plotterJobId(),
                new BigDecimal("30000.00"),
                LocalDate.of(2099, 5, 10),
                "abono"
        ));
        registerPlotterPaymentUseCase.execute(new RegisterPlotterPaymentCommand(
                paid.plotterJobId(),
                new BigDecimal("50000.00"),
                LocalDate.of(2099, 5, 11),
                "saldo"
        ));

        long financeBeforeRead = countAllFinancialTransactions();
        GetPlotterProfitabilityResult all = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(
                        LocalDate.of(2099, 5, 1),
                        LocalDate.of(2099, 5, 31),
                        PlotterProfitabilityScope.ALL
                )
        );

        assertEquals(financeBeforeRead, countAllFinancialTransactions());
        assertEquals(3, all.externalJobCount());
        assertEquals(1, all.internalJobCount());
        assertEquals(1, all.wasteJobCount());
        assertEquals(new BigDecimal("230000.00"), all.externalRevenue());
        assertEquals(new BigDecimal("80000.00"), all.externalPaidAmount());
        assertEquals(new BigDecimal("150000.00"), all.externalOutstandingAmount());

        GetPlotterProfitabilityResult internals = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(
                        LocalDate.of(2099, 5, 1),
                        LocalDate.of(2099, 5, 31),
                        PlotterProfitabilityScope.INTERNAL
                )
        );
        assertEquals(new BigDecimal("0.00"), internals.externalRevenue());
        assertEquals(new BigDecimal("0.00"), internals.externalPaidAmount());
        assertEquals(new BigDecimal("0.00"), internals.externalOutstandingAmount());

        GetPlotterProfitabilityResult waste = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(
                        LocalDate.of(2099, 5, 1),
                        LocalDate.of(2099, 5, 31),
                        PlotterProfitabilityScope.WASTE
                )
        );
        assertEquals(new BigDecimal("0.00"), waste.externalRevenue());
        assertEquals(new BigDecimal("0.00"), waste.externalPaidAmount());
        assertEquals(new BigDecimal("0.00"), waste.externalOutstandingAmount());
        assertEquals(1, waste.wasteJobCount());
    }

    private CreateInventoryItemResult createPurchasedPaperRoll(
            String rollQuantity,
            String pricePerRoll,
            String meters,
            LocalDate purchaseDate
    ) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "PLPR-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel analítica",
                "PAPER",
                "METER",
                new BigDecimal(meters),
                new BigDecimal("10.0000"),
                null,
                null,
                "PAPER",
                true,
                new InventoryAcquisitionCommand(
                        UUID.randomUUID(),
                        new BigDecimal(rollQuantity),
                        new BigDecimal(pricePerRoll),
                        null,
                        purchaseDate,
                        "compra papel analítica"
                )
        ));
    }

    private CreateInventoryItemResult createPurchasedInk(String totalCost, LocalDate purchaseDate) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "INK-" + UUID.randomUUID().toString().substring(0, 8),
                "Tinta analítica",
                "INK",
                "LITER",
                new BigDecimal("1.0000"),
                new BigDecimal("0.1000"),
                null,
                null,
                "INK",
                false,
                new InventoryAcquisitionCommand(
                        UUID.randomUUID(),
                        BigDecimal.ONE,
                        new BigDecimal(totalCost),
                        null,
                        purchaseDate,
                        "compra tinta analítica"
                )
        ));
    }

    private CreateInventoryItemResult createPaperRoll(String unitCost) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "PLPR-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel analítica",
                "PAPER",
                "METER",
                new BigDecimal("100.0000"),
                new BigDecimal("10.0000"),
                null,
                new BigDecimal(unitCost),
                "PAPER",
                true
        ));
    }

    private CommercialOrderFixture createCommercialOrder() {
        return createCommercialOrder(LocalDate.of(2099, 3, 8), LocalDate.of(2099, 3, 25));
    }

    private CommercialOrderFixture createCommercialOrder(LocalDate confirmationDate, LocalDate deliveryDate) {
        String customerName = "Cliente plotter G " + UUID.randomUUID();
        var customer = createCustomerUseCase.execute(new CreateCustomerCommand(customerName));
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor plotter G " + UUID.randomUUID()
        );
        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customer.customerId(),
                deliveryDate,
                sellerId,
                null,
                confirmationDate.minusDays(3)
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Producto plotter G",
                2,
                "Sudáfrica",
                "Blanco",
                new BigDecimal("40000"),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        var order = createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                null,
                confirmationDate,
                deliveryDate,
                null
        ));
        return new CommercialOrderFixture(order.orderId(), order.orderNumber(), customerName);
    }

    private long countAllFinancialTransactions() {
        return financialTransactionRepository.findAllNewestFirst().size();
    }

    private long countPlotterIncome() {
        return financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getSourceType() == FinancialTransactionSourceType.PLOTTER)
                .filter(transaction -> transaction.getType() == FinancialTransactionType.INCOME)
                .count();
    }

    private long countInkOuts(UUID inventoryItemId) {
        return inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(inventoryItemId)
                .stream()
                .filter(movement -> movement.getSourceType() == InventoryMovementSourceType.PLOTTER)
                .count();
    }

    private int countPaperOutsFor(UUID plotterJobId) {
        return inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, plotterJobId)
                .isPresent() ? 1 : 0;
    }

    private record CommercialOrderFixture(UUID orderId, String orderNumber, String customerName) {
    }
}
