package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
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
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentCommand;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionMaterialConsumptionUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
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
class PlotterInternalExternalUseCaseTest {

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

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
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private RegisterPlotterPaymentUseCase registerPlotterPaymentUseCase;

    @Autowired
    private PlotterJobRepository plotterJobRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private RegisterProductionMaterialConsumptionUseCase registerProductionMaterialConsumptionUseCase;

    @Test
    void createsInternalJobWithVariablePriceAndMatchingExpenseIncomePair() {
        CommercialOrderFixture order = createCommercialOrder("Cliente interno IncI");
        CreateInventoryItemResult roll = createPaperRoll("80.0000", "8000.00");
        long plotterIncomeBefore = countPlotterIncome();

        CreatePlotterJobResult created = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                order.orderId(),
                LocalDate.of(2026, 8, 3),
                roll.inventoryItemId(),
                new BigDecimal("6.0000"),
                new BigDecimal("8000"),
                "Producción Magyen",
                PlotterJobType.INTERNAL_MAGYEN,
                null
        ));

        assertEquals(PlotterJobType.INTERNAL_MAGYEN, created.jobType());
        assertEquals(order.orderId(), created.orderId());
        assertEquals(order.orderNumber(), created.orderNumber());
        assertEquals(order.customerName(), created.customerName());
        assertEquals(new BigDecimal("8000.00"), created.pricePerMeter());
        assertEquals(new BigDecimal("48000.00"), created.totalAmount());

        var movement = inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, created.plotterJobId())
                .orElseThrow();
        assertEquals(InventoryMovementType.OUT, movement.getMovementType());
        assertEquals(0, new BigDecimal("6.0000").compareTo(movement.getQuantity()));
        assertEquals(new BigDecimal("8000.00"), movement.getUnitCost());
        assertEquals(new BigDecimal("48000.00"), movement.getTotalCost());
        assertEquals(1, countPlotterOuts(created.plotterJobId()));
        assertEquals(plotterIncomeBefore, countPlotterIncome());

        var expense = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE,
                created.plotterJobId()
        ).orElseThrow();
        var income = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME,
                created.plotterJobId()
        ).orElseThrow();
        assertEquals(FinancialTransactionType.EXPENSE, expense.getType());
        assertEquals(FinancialTransactionType.INCOME, income.getType());
        assertEquals(new BigDecimal("48000.00"), expense.getAmount().getValue());
        assertEquals(new BigDecimal("48000.00"), income.getAmount().getValue());
        assertEquals(0, expense.getAmount().getValue().compareTo(income.getAmount().getValue()));
        assertEquals(1, countInternalExpenses(created.plotterJobId()));
        assertEquals(1, countInternalIncomes(created.plotterJobId()));

        assertThrows(PlotterDomainException.class, () ->
                registerPlotterPaymentUseCase.execute(new RegisterPlotterPaymentCommand(
                        created.plotterJobId(),
                        new BigDecimal("1000.00"),
                        LocalDate.of(2026, 8, 4),
                        null
                ))
        );
        assertEquals(plotterIncomeBefore, countPlotterIncome());
    }

    @Test
    void createsExternalJobWithSingleInventoryOutWithoutCommercialOrder() {
        UUID customerId = createCustomerUseCase.execute(
                new CreateCustomerCommand("Cliente externo IncD-" + UUID.randomUUID().toString().substring(0, 8))
        ).customerId();
        CreateInventoryItemResult roll = createPaperRoll("80.0000", "8000.00");
        long plotterIncomeBefore = countPlotterIncome();

        CreatePlotterJobResult created = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                customerId,
                null,
                LocalDate.of(2026, 8, 3),
                roll.inventoryItemId(),
                new BigDecimal("6.0000"),
                new BigDecimal("8000"),
                "Cliente externo",
                PlotterJobType.EXTERNAL,
                null
        ));

        assertEquals(PlotterJobType.EXTERNAL, created.jobType());
        assertNull(created.orderId());
        assertNull(created.orderNumber());
        assertEquals(new BigDecimal("48000.00"), created.totalAmount());
        assertEquals(1, countPlotterOuts(created.plotterJobId()));
        assertEquals(plotterIncomeBefore, countPlotterIncome());
    }

    @Test
    void internalJobRequiresExistingOrderAndExternalJobRejectsOrderId() {
        CreateInventoryItemResult roll = createPaperRoll("20.0000", "8000.00");
        UUID customerId = UUID.randomUUID();

        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        customerId,
                        null,
                        LocalDate.of(2026, 8, 3),
                        roll.inventoryItemId(),
                        new BigDecimal("1.0000"),
                        new BigDecimal("8000"),
                        null,
                        PlotterJobType.INTERNAL_MAGYEN,
                        null
                ))
        );

        CommercialOrderFixture order = createCommercialOrder("Orden solo interna");
        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        customerId,
                        order.orderId(),
                        LocalDate.of(2026, 8, 3),
                        roll.inventoryItemId(),
                        new BigDecimal("1.0000"),
                        new BigDecimal("8000"),
                        null,
                        PlotterJobType.EXTERNAL,
                        null
                ))
        );
        assertEquals(0, plotterJobRepository.findByOrderId(order.orderId()).size());
    }

    @Test
    void insufficientStockLeavesNoPartialPlotterOrInventoryState() {
        CreateInventoryItemResult roll = createPaperRoll("5.0000", "8000.00");
        long plotterIncomeBefore = countPlotterIncome();
        long jobsBefore = plotterJobRepository.findAll().size();
        long movementsBefore = inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(roll.inventoryItemId())
                .size();

        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        UUID.randomUUID(),
                        null,
                        LocalDate.of(2026, 8, 3),
                        roll.inventoryItemId(),
                        new BigDecimal("6.0000"),
                        new BigDecimal("8000"),
                        null,
                        PlotterJobType.EXTERNAL,
                        null
                ))
        );

        assertEquals(jobsBefore, plotterJobRepository.findAll().size());
        assertEquals(
                movementsBefore,
                inventoryMovementRepository
                        .findByInventoryItemIdOrderByMovementDateDesc(roll.inventoryItemId())
                        .size()
        );
        assertEquals(plotterIncomeBefore, countPlotterIncome());
    }

    @Test
    void retryWithSamePlotterJobIdDoesNotDuplicateConsumptionForInternalOrExternal() {
        CommercialOrderFixture order = createCommercialOrder("Retry interno");
        CreateInventoryItemResult internalRoll = createPaperRoll("80.0000", "8000.00");
        UUID internalJobId = UUID.randomUUID();

        CreatePlotterJobCommand internalCommand = new CreatePlotterJobCommand(
                null,
                order.orderId(),
                LocalDate.of(2026, 8, 3),
                internalRoll.inventoryItemId(),
                new BigDecimal("6.0000"),
                new BigDecimal("8000"),
                null,
                PlotterJobType.INTERNAL_MAGYEN,
                internalJobId
        );
        CreatePlotterJobResult firstInternal = createPlotterJobUseCase.execute(internalCommand);
        CreatePlotterJobResult retryInternal = createPlotterJobUseCase.execute(internalCommand);

        assertEquals(firstInternal.plotterJobId(), retryInternal.plotterJobId());
        assertEquals(1, countPlotterOuts(internalJobId));
        assertEquals(1, countInternalExpenses(internalJobId));
        assertEquals(1, countInternalIncomes(internalJobId));

        UUID customerId = createCustomerUseCase.execute(
                new CreateCustomerCommand("Retry externo-" + UUID.randomUUID().toString().substring(0, 8))
        ).customerId();
        CreateInventoryItemResult externalRoll = createPaperRoll("80.0000", "8000.00");
        UUID externalJobId = UUID.randomUUID();
        CreatePlotterJobCommand externalCommand = new CreatePlotterJobCommand(
                customerId,
                null,
                LocalDate.of(2026, 8, 3),
                externalRoll.inventoryItemId(),
                new BigDecimal("6.0000"),
                new BigDecimal("8000"),
                null,
                PlotterJobType.EXTERNAL,
                externalJobId
        );
        createPlotterJobUseCase.execute(externalCommand);
        createPlotterJobUseCase.execute(externalCommand);
        assertEquals(1, countPlotterOuts(externalJobId));
    }

    @Test
    void productionCannotConsumePlotterPaperAfterInternalJob() {
        CommercialOrderFixture order = createCommercialOrder("Sin doble consumo");
        CreateInventoryItemResult roll = createPaperRoll("80.0000", "8000.00");
        createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                order.orderId(),
                LocalDate.of(2026, 8, 3),
                roll.inventoryItemId(),
                new BigDecimal("6.0000"),
                new BigDecimal("8000"),
                null,
                PlotterJobType.INTERNAL_MAGYEN,
                null
        ));

        ProductionOrder productionOrder = productionOrderRepository.save(ProductionOrder.create(
                order.orderId(),
                LocalDate.of(2026, 8, 3),
                ProductionPriority.NORMAL,
                null,
                null,
                "doble consumo"
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                productionOrder.getId(),
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 6),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(
                new StartProductionOrderCommand(productionOrder.getId(), LocalDate.of(2026, 8, 3))
        );

        assertThrows(ProductionDomainException.class, () ->
                registerProductionMaterialConsumptionUseCase.execute(
                        new RegisterProductionMaterialConsumptionCommand(
                                productionOrder.getId(),
                                roll.inventoryItemId(),
                                new BigDecimal("6.0000"),
                                "METER",
                                "intento duplicado"
                        )
                )
        );
        assertEquals(1, inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(roll.inventoryItemId())
                .size());
    }

    @Test
    void internalJobRejectsZeroPricePerMeter() {
        CommercialOrderFixture order = createCommercialOrder("Precio interno obligatorio");
        CreateInventoryItemResult roll = createPaperRoll("20.0000", "8000.00");

        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        null,
                        order.orderId(),
                        LocalDate.of(2026, 8, 3),
                        roll.inventoryItemId(),
                        new BigDecimal("6.0000"),
                        BigDecimal.ZERO,
                        null,
                        PlotterJobType.INTERNAL_MAGYEN,
                        null
                ))
        );
    }

    private CommercialOrderFixture createCommercialOrder(String customerName) {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-IncD-" + UUID.randomUUID().toString().substring(0, 8)
        );
        UUID customerId = createCustomerUseCase.execute(new CreateCustomerCommand(customerName)).customerId();

        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customerId,
                LocalDate.of(2026, 8, 6),
                sellerId,
                null,
                LocalDate.of(2026, 7, 27)
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Camisetas de voleibol",
                10,
                "Sudáfrica",
                "Blanco",
                new BigDecimal("40000"),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        CreateOrderFromQuotationResult order = createOrderFromQuotationUseCase.execute(
                new CreateOrderFromQuotationCommand(
                        quotation.quotationId(),
                        "ORD-INCD-" + UUID.randomUUID().toString().substring(0, 8),
                        null,
                        LocalDate.of(2026, 7, 29),
                        LocalDate.of(2026, 8, 6),
                        null
                )
        );
        return new CommercialOrderFixture(order.orderId(), order.orderNumber(), customerName);
    }

    private CreateInventoryItemResult createPaperRoll(String stock, String unitCost) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "INCD-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel sublimación",
                "PAPER",
                "METER",
                new BigDecimal(stock),
                new BigDecimal("10.0000"),
                null,
                new BigDecimal(unitCost),
                "PAPER",
                true
        ));
    }

    private int countPlotterOuts(UUID plotterJobId) {
        return inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, plotterJobId)
                .isPresent() ? 1 : 0;
    }

    private long countPlotterIncome() {
        return financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getSourceType() == FinancialTransactionSourceType.PLOTTER)
                .filter(transaction -> transaction.getType() == FinancialTransactionType.INCOME)
                .count();
    }

    private long countInternalExpenses(UUID plotterJobId) {
        return financialTransactionRepository.findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE,
                        plotterJobId
                )
                .stream()
                .filter(transaction -> transaction.getType() == FinancialTransactionType.EXPENSE)
                .count();
    }

    private long countInternalIncomes(UUID plotterJobId) {
        return financialTransactionRepository.findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME,
                        plotterJobId
                )
                .stream()
                .filter(transaction -> transaction.getType() == FinancialTransactionType.INCOME)
                .count();
    }

    private record CommercialOrderFixture(UUID orderId, String orderNumber, String customerName) {
    }
}
