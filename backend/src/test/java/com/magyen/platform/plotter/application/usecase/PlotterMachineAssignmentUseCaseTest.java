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
import com.magyen.platform.inventory.application.dto.AssignPaperRollMachineCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.AssignPaperRollMachineUseCase;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterMachineOperationsQuery;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.PlotterMachineRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import com.magyen.platform.shared.testsupport.PlotterCustomerFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PlotterMachineAssignmentUseCaseTest {

    private static final UUID PLOTTER_1 = UUID.fromString("6f0c1a10-0001-4a01-8a01-000000000001");
    private static final UUID PLOTTER_2 = UUID.fromString("6f0c1a10-0002-4a02-8a02-000000000002");

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private AssignPaperRollMachineUseCase assignPaperRollMachineUseCase;

    @Autowired
    private ChangePlotterMachineActiveUseCase changePlotterMachineActiveUseCase;

    @Autowired
    private GetPlotterMachinesUseCase getPlotterMachinesUseCase;

    @Autowired
    private GetPlotterMachineOperationsUseCase getPlotterMachineOperationsUseCase;

    @Autowired
    private PlotterMachineRepository plotterMachineRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private CreateQuotationUseCase createQuotationUseCase;

    @Autowired
    private AddQuotationItemUseCase addQuotationItemUseCase;

    @Autowired
    private ApproveQuotationUseCase approveQuotationUseCase;

    @Autowired
    private CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void registersEachJobTypeOnTheSelectedMachineWithoutExtraFinanceForTheMachine() {
        assertTrue(getPlotterMachinesUseCase.execute().stream()
                .anyMatch(machine -> machine.plotterMachineId().equals(PLOTTER_1) && machine.defaultForRegistration()));
        assertTrue(getPlotterMachinesUseCase.execute().stream()
                .anyMatch(machine -> machine.plotterMachineId().equals(PLOTTER_2) && machine.active()));

        CreateInventoryItemResult roll = createRoll(PLOTTER_1, "40.0000");
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);
        UUID orderId = createCommercialOrder();
        long financeBefore = financialTransactionRepository.findAllNewestFirst().size();

        CreatePlotterJobResult external = createJob(
                PlotterJobType.EXTERNAL, customerId, null, roll.inventoryItemId(), PLOTTER_1, "4.0000", "1000");
        CreatePlotterJobResult internal = createJob(
                PlotterJobType.INTERNAL_MAGYEN, null, orderId, roll.inventoryItemId(), PLOTTER_1, "3.0000", "2000");
        CreatePlotterJobResult waste = createJob(
                PlotterJobType.WASTE, null, null, roll.inventoryItemId(), PLOTTER_1, "1.0000", "0");

        assertEquals(PLOTTER_1, external.plotterMachineId());
        assertEquals(PLOTTER_1, internal.plotterMachineId());
        assertEquals(PLOTTER_1, waste.plotterMachineId());
        assertEquals("Plotter 1", external.machineName());
        assertEquals(1, movementCount(external.plotterJobId()));
        assertEquals(1, movementCount(internal.plotterJobId()));
        assertEquals(1, movementCount(waste.plotterJobId()));
        assertTrue(financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER, external.plotterJobId()).isEmpty());
        assertTrue(financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE, internal.plotterJobId()).isPresent());
        assertTrue(financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER, waste.plotterJobId()).isEmpty());
        assertTrue(financialTransactionRepository.findAllNewestFirst().size() >= financeBefore);
    }

    @Test
    void rejectsMissingUnknownAndInactiveMachines() {
        CreateInventoryItemResult roll = createRoll(PLOTTER_1, "20.0000");
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);

        changePlotterMachineActiveUseCase.execute(PLOTTER_1, false);
        assertThrows(PlotterDomainException.class, () -> createJob(
                PlotterJobType.EXTERNAL, customerId, null, roll.inventoryItemId(), null, "1.0000", "1000"));

        assertThrows(PlotterDomainException.class, () -> createJob(
                PlotterJobType.EXTERNAL, customerId, null, roll.inventoryItemId(), UUID.randomUUID(), "1.0000", "1000"));

        changePlotterMachineActiveUseCase.execute(PLOTTER_2, false);
        assertThrows(PlotterDomainException.class, () -> createJob(
                PlotterJobType.EXTERNAL, customerId, null, roll.inventoryItemId(), PLOTTER_2, "1.0000", "1000"));
    }

    @Test
    void rejectsARollAssignedToTheOtherMachineAndAnUnassignedRoll() {
        CreateInventoryItemResult rollOnOne = createRoll(PLOTTER_1, "15.0000");
        CreateInventoryItemResult rollOnTwo = createRoll(PLOTTER_2, "15.0000");
        InventoryItem unassigned = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PAPER"),
                "Rollo sin máquina",
                "PAPER",
                "METER",
                new BigDecimal("10.0000"),
                null,
                null,
                new BigDecimal("1000.00"),
                InventoryMaterialType.PAPER,
                "RP-OPEN-" + UUID.randomUUID().toString().substring(0, 8)
        ));
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);

        assertThrows(PlotterDomainException.class, () -> createJob(
                PlotterJobType.EXTERNAL, customerId, null, rollOnOne.inventoryItemId(), PLOTTER_2, "1.0000", "1000"));
        assertThrows(PlotterDomainException.class, () -> createJob(
                PlotterJobType.WASTE, null, null, rollOnTwo.inventoryItemId(), PLOTTER_1, "1.0000", "0"));
        assertThrows(PlotterDomainException.class, () -> createJob(
                PlotterJobType.EXTERNAL, customerId, null, unassigned.getId(), PLOTTER_1, "1.0000", "1000"));
        assertEquals(0, movementCount(unassigned.getId()));
    }

    @Test
    void receivingAssignsTheMachineOnceAndDoesNotChangeStockOrCost() {
        CreateInventoryItemResult created = createRoll(PLOTTER_2, "25.5000");
        InventoryItem saved = inventoryItemRepository.findById(created.inventoryItemId()).orElseThrow();
        assertEquals(PLOTTER_2, saved.getPlotterMachineId());
        assertEquals(0, new BigDecimal("25.5000").compareTo(saved.getStock()));

        InventoryItem open = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PAPER"),
                "Rollo por asignar",
                "PAPER",
                "METER",
                new BigDecimal("9.0000"),
                null,
                null,
                new BigDecimal("1200.00"),
                InventoryMaterialType.PAPER,
                "RP-SET-" + UUID.randomUUID().toString().substring(0, 8)
        ));
        var assigned = assignPaperRollMachineUseCase.execute(
                new AssignPaperRollMachineCommand(open.getId(), PLOTTER_1)
        );
        assertEquals(PLOTTER_1, assigned.plotterMachineId());
        assertEquals(0, new BigDecimal("9.0000").compareTo(assigned.stock()));
        assertEquals(new BigDecimal("1200.00"), assigned.unitCost());
        assertThrows(InventoryDomainException.class, () -> assignPaperRollMachineUseCase.execute(
                new AssignPaperRollMachineCommand(open.getId(), PLOTTER_2)
        ));
        assertThrows(InventoryDomainException.class, () -> inventoryItemRepository.findById(open.getId())
                .orElseThrow()
                .assignPlotterMachine(PLOTTER_2));
    }

    @Test
    void retryDoesNotDuplicateTheStockMovement() {
        CreateInventoryItemResult roll = createRoll(PLOTTER_2, "30.0000");
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);
        UUID jobId = UUID.randomUUID();
        CreatePlotterJobCommand command = new CreatePlotterJobCommand(
                customerId,
                null,
                LocalDate.of(2026, 8, 2),
                roll.inventoryItemId(),
                new BigDecimal("2.0000"),
                new BigDecimal("1500"),
                "Reintento",
                PlotterJobType.EXTERNAL,
                jobId,
                false,
                PLOTTER_2
        );

        CreatePlotterJobResult first = createPlotterJobUseCase.execute(command);
        CreatePlotterJobResult second = createPlotterJobUseCase.execute(command);

        assertEquals(first.plotterJobId(), second.plotterJobId());
        assertEquals(1, movementCount(jobId));
        assertEquals(0, new BigDecimal("28.0000").compareTo(
                inventoryItemRepository.findById(roll.inventoryItemId()).orElseThrow().getStock()
        ));
    }

    @Test
    void historicalBackfillKeepsRecordedJobsAndPaperRollsOnPlotterOne() {
        assertEquals(0, count(
                "SELECT count(*) FROM inventory_items WHERE material_type <> 'PAPER' AND plotter_machine_id IS NOT NULL"
        ));
        assertEquals(0, count("SELECT count(*) FROM plotter_jobs WHERE machine_id IS NULL"));
        assertEquals(0, count("""
                SELECT count(*) FROM inventory_items
                WHERE material_type = 'PAPER'
                  AND paper_roll_number IS NOT NULL
                  AND plotter_machine_id IS NULL
                """));
        assertTrue(count("""
                SELECT count(*) FROM plotter_jobs
                WHERE machine_id = '6f0c1a10-0001-4a01-8a01-000000000001'
                """) >= 152);

        var roll = jdbcTemplate.queryForMap("""
                SELECT stock, unit_cost
                FROM inventory_items
                WHERE paper_roll_number = 'RP-015'
                  AND plotter_machine_id = '6f0c1a10-0001-4a01-8a01-000000000001'
                """);
        assertEquals(0, new BigDecimal("42.2000").compareTo(new BigDecimal(roll.get("stock").toString())));
        assertEquals(0, new BigDecimal("1200.00").compareTo(new BigDecimal(roll.get("unit_cost").toString())));
    }

    @Test
    void machineOperationsUseJobDateForMetersAndPaymentDateForExternalRevenue() {
        CreateInventoryItemResult roll = createRoll(PLOTTER_2, "20.0000");
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);
        createJob(PlotterJobType.EXTERNAL, customerId, null, roll.inventoryItemId(), PLOTTER_2, "5.0000", "1000");
        createJob(PlotterJobType.WASTE, null, null, roll.inventoryItemId(), PLOTTER_2, "2.0000", "0");

        var report = getPlotterMachineOperationsUseCase.execute(new GetPlotterMachineOperationsQuery(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                PLOTTER_2,
                false
        ));

        assertEquals(1, report.machines().size());
        assertEquals(2, report.machines().getFirst().jobCount());
        assertEquals(0, new BigDecimal("5.0000").compareTo(report.machines().getFirst().printedMeters()));
        assertEquals(0, new BigDecimal("2.0000").compareTo(report.machines().getFirst().wasteMeters()));
        assertEquals(0, BigDecimal.ZERO.setScale(2).compareTo(report.machines().getFirst().externalRevenue()));
        assertTrue(plotterMachineRepository.findById(PLOTTER_2).orElseThrow().isActive());
    }

    private CreateInventoryItemResult createRoll(UUID machineId, String stock) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "MACH-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel plotter",
                "PAPER",
                "METER",
                new BigDecimal(stock),
                new BigDecimal("5.0000"),
                null,
                new BigDecimal("1000.00"),
                "PAPER",
                true,
                null,
                machineId
        ));
    }

    private CreatePlotterJobResult createJob(
            PlotterJobType jobType,
            UUID customerId,
            UUID orderId,
            UUID rollId,
            UUID machineId,
            String meters,
            String price
    ) {
        return createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                customerId,
                orderId,
                LocalDate.of(2026, 8, 4),
                rollId,
                new BigDecimal(meters),
                new BigDecimal(price),
                null,
                jobType,
                null,
                false,
                machineId
        ));
    }

    private int count(String sql) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class);
        return value == null ? 0 : value;
    }

    private int movementCount(UUID sourceId) {
        return inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, sourceId)
                .isPresent() ? 1 : 0;
    }

    private UUID createCommercialOrder() {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-Machine-" + UUID.randomUUID().toString().substring(0, 8)
        );
        UUID customerId = createCustomerUseCase.execute(new CreateCustomerCommand(
                "Cliente máquina " + UUID.randomUUID().toString().substring(0, 6)
        )).customerId();
        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customerId,
                LocalDate.of(2026, 8, 6),
                sellerId,
                null,
                LocalDate.of(2026, 7, 27)
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Camisetas",
                4,
                "Sudáfrica",
                "Blanco",
                new BigDecimal("40000"),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        return createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                null,
                LocalDate.of(2026, 7, 29),
                LocalDate.of(2026, 8, 10),
                null
        )).orderId();
    }
}
