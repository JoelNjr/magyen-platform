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
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.plotter.application.dto.CorrectPlotterJobDateCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.EditPlotterJobCommand;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsQuery;
import com.magyen.platform.plotter.application.dto.RegisterPlotterCustomerPaymentCommand;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import com.magyen.platform.shared.testsupport.PlotterCustomerFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PlotterRunningAccountUseCaseTest {

    private static final UUID PLOTTER_1 = UUID.fromString("6f0c1a10-0001-4a01-8a01-000000000001");
    private static final UUID PLOTTER_2 = UUID.fromString("6f0c1a10-0002-4a02-8a02-000000000002");

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;
    @Autowired
    private EditPlotterJobUseCase editPlotterJobUseCase;
    @Autowired
    private CorrectPlotterJobDateUseCase correctPlotterJobDateUseCase;
    @Autowired
    private CancelPlotterJobUseCase cancelPlotterJobUseCase;
    @Autowired
    private RegisterPlotterCustomerPaymentUseCase registerPlotterCustomerPaymentUseCase;
    @Autowired
    private GetPlotterCustomerAccountUseCase getPlotterCustomerAccountUseCase;
    @Autowired
    private GetPlotterJobsUseCase getPlotterJobsUseCase;
    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;
    @Autowired
    private PlotterJobRepository plotterJobRepository;
    @Autowired
    private PlotterPaymentRepository plotterPaymentRepository;
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
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void externalJobsDebitTheAccountOnceAndPaymentsAreIndependentOfEachJob() {
        CreateInventoryItemResult roll = createRoll(PLOTTER_1, "30.0000");
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);
        CreatePlotterJobResult first = createExternal(
                customerId, roll.inventoryItemId(), PLOTTER_1, "4.0000", "1000", "7500", LocalDate.of(2099, 11, 2));
        CreatePlotterJobResult second = createExternal(
                customerId, roll.inventoryItemId(), PLOTTER_1, "2.0000", "1000", "1800", LocalDate.of(2099, 11, 4));

        assertEquals(new BigDecimal("7500.00"), first.totalAmount());
        assertTrue(financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER, first.plotterJobId()).isEmpty());

        var beforePayment = getPlotterCustomerAccountUseCase.detail(customerId);
        assertEquals(new BigDecimal("9300.00"), beforePayment.debitTotal());
        assertEquals(2, beforePayment.movements().stream().filter(row -> "JOB_DEBIT".equals(row.movementType())).count());

        UUID registrationKey = UUID.randomUUID();
        var partial = registerPlotterCustomerPaymentUseCase.execute(new RegisterPlotterCustomerPaymentCommand(
                customerId, new BigDecimal("3000"), LocalDate.of(2099, 11, 20), "Transferencia 100", registrationKey
        ));
        var replay = registerPlotterCustomerPaymentUseCase.execute(new RegisterPlotterCustomerPaymentCommand(
                customerId, new BigDecimal("3000"), LocalDate.of(2099, 11, 20), "Transferencia 100", registrationKey
        ));
        assertTrue(replay.alreadyRegistered());
        assertEquals(partial.paymentId(), replay.paymentId());
        assertEquals(1, incomeCount(partial.paymentId()));
        assertNullJob(partial.paymentId());

        var settled = registerPlotterCustomerPaymentUseCase.execute(new RegisterPlotterCustomerPaymentCommand(
                customerId, new BigDecimal("6300"), LocalDate.of(2099, 11, 21), "Saldo", UUID.randomUUID()
        ));
        assertEquals(new BigDecimal("0.00"), settled.outstandingAmount());
        assertEquals(1, incomeCount(settled.paymentId()));
        assertThrows(PlotterDomainException.class, () -> registerPlotterCustomerPaymentUseCase.execute(
                new RegisterPlotterCustomerPaymentCommand(
                        customerId, new BigDecimal("1"), LocalDate.of(2099, 11, 22), null, UUID.randomUUID()
                )
        ));

        editPlotterJobUseCase.execute(editOf(first, "4.0000", "8000", roll.inventoryItemId()));
        var afterEdit = getPlotterCustomerAccountUseCase.detail(customerId);
        assertEquals(1, afterEdit.movements().stream()
                .filter(row -> first.plotterJobId().equals(row.plotterJobId()))
                .count());
        assertEquals(new BigDecimal("8000.00"), plotterJobRepository.findById(first.plotterJobId()).orElseThrow().getTotalAmount());
        assertEquals(1, movementCount(InventoryMovementSourceType.PLOTTER, first.plotterJobId()));
    }

    @Test
    void editingMetersRewritesTheSingleConsumptionAndRejectsTheOtherMachineRoll() {
        CreateInventoryItemResult roll = createRoll(PLOTTER_1, "20.0000");
        CreateInventoryItemResult otherRoll = createRoll(PLOTTER_1, "12.0000");
        CreateInventoryItemResult foreignRoll = createRoll(PLOTTER_2, "12.0000");
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);
        CreatePlotterJobResult job = createExternal(
                customerId, roll.inventoryItemId(), PLOTTER_1, "5.0000", "1000", "5000", LocalDate.of(2099, 11, 3));

        editPlotterJobUseCase.execute(new EditPlotterJobCommand(
                job.plotterJobId(),
                job.creationDate(),
                otherRoll.inventoryItemId(),
                new BigDecimal("3.0000"),
                new BigDecimal("1000"),
                new BigDecimal("5000"),
                "Cambio de rollo",
                job.plotterMachineId()
        ));

        assertEquals(0, new BigDecimal("20.0000").compareTo(stock(roll.inventoryItemId())));
        assertEquals(0, new BigDecimal("9.0000").compareTo(stock(otherRoll.inventoryItemId())));
        assertEquals(1, movementCount(InventoryMovementSourceType.PLOTTER, job.plotterJobId()));
        assertEquals(otherRoll.inventoryItemId(), inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, job.plotterJobId())
                .orElseThrow()
                .getInventoryItemId());

        assertThrows(PlotterDomainException.class, () -> editPlotterJobUseCase.execute(new EditPlotterJobCommand(
                job.plotterJobId(),
                job.creationDate(),
                foreignRoll.inventoryItemId(),
                new BigDecimal("3.0000"),
                new BigDecimal("1000"),
                new BigDecimal("5000"),
                null,
                job.plotterMachineId()
        )));
        assertThrows(PlotterDomainException.class, () -> editPlotterJobUseCase.execute(new EditPlotterJobCommand(
                job.plotterJobId(),
                job.creationDate(),
                otherRoll.inventoryItemId(),
                new BigDecimal("3.0000"),
                new BigDecimal("1000"),
                new BigDecimal("5000"),
                null,
                PLOTTER_2
        )));
    }

    @Test
    void dateCorrectionDoesNotConsumePaperAgainOrMoveThePaymentDate() {
        CreateInventoryItemResult roll = createRoll(PLOTTER_1, "20.0000");
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);
        CreatePlotterJobResult job = createExternal(
                customerId, roll.inventoryItemId(), PLOTTER_1, "2.0000", "1000", "2500", LocalDate.of(2099, 11, 2));
        var payment = registerPlotterCustomerPaymentUseCase.execute(new RegisterPlotterCustomerPaymentCommand(
                customerId, new BigDecimal("1000"), LocalDate.of(2099, 11, 18), "Efectivo", UUID.randomUUID()
        ));

        correctPlotterJobDateUseCase.execute(new CorrectPlotterJobDateCommand(
                job.plotterJobId(), LocalDate.of(2099, 11, 6)
        ));

        assertEquals(LocalDate.of(2099, 11, 6), plotterJobRepository.findById(job.plotterJobId()).orElseThrow().getCreationDate());
        assertEquals(LocalDate.of(2099, 11, 18), plotterPaymentRepository.findById(payment.paymentId()).orElseThrow().getPaymentDate());
        assertEquals(1, movementCount(InventoryMovementSourceType.PLOTTER, job.plotterJobId()));
        assertEquals(0, new BigDecimal("18.0000").compareTo(stock(roll.inventoryItemId())));
        var account = getPlotterCustomerAccountUseCase.detail(customerId);
        assertEquals(LocalDate.of(2099, 11, 6), account.movements().stream()
                .filter(row -> "JOB_DEBIT".equals(row.movementType()))
                .findFirst()
                .orElseThrow()
                .movementDate());
        assertEquals(LocalDate.of(2099, 11, 18), account.movements().stream()
                .filter(row -> "CUSTOMER_PAYMENT".equals(row.movementType()))
                .findFirst()
                .orElseThrow()
                .movementDate());

        var listed = getPlotterJobsUseCase.execute(new GetPlotterJobsQuery(
                LocalDate.of(2099, 11, 1), LocalDate.of(2099, 11, 30), customerId
        ));
        assertEquals(job.plotterJobId(), listed.jobs().getFirst().plotterJobId());
    }

    @Test
    void cancellationRestoresInventoryOnceAndKeepsReceivedMoney() {
        CreateInventoryItemResult roll = createRoll(PLOTTER_1, "15.0000");
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);
        CreatePlotterJobResult kept = createExternal(
                customerId, roll.inventoryItemId(), PLOTTER_1, "4.0000", "1000", "6000", LocalDate.of(2099, 11, 2));
        CreatePlotterJobResult removable = createExternal(
                customerId, roll.inventoryItemId(), PLOTTER_1, "2.0000", "1000", "2000", LocalDate.of(2099, 11, 3));
        var payment = registerPlotterCustomerPaymentUseCase.execute(new RegisterPlotterCustomerPaymentCommand(
                customerId, new BigDecimal("5000"), LocalDate.of(2099, 11, 8), "Abono", UUID.randomUUID()
        ));

        assertThrows(PlotterDomainException.class, () -> cancelPlotterJobUseCase.execute(kept.plotterJobId()));
        var cancelled = cancelPlotterJobUseCase.execute(removable.plotterJobId());
        var again = cancelPlotterJobUseCase.execute(removable.plotterJobId());

        assertFalse(cancelled.deleted());
        assertEquals(PlotterJobStatus.CANCELLED, cancelled.status());
        assertTrue(again.alreadyCancelled());
        assertEquals(1, movementCount(InventoryMovementSourceType.PLOTTER_REVERSAL, removable.plotterJobId()));
        assertEquals(0, new BigDecimal("11.0000").compareTo(stock(roll.inventoryItemId())));
        assertEquals(new BigDecimal("6000.00"), getPlotterCustomerAccountUseCase.detail(customerId).debitTotal());
        assertEquals(new BigDecimal("5000.00"), getPlotterCustomerAccountUseCase.detail(customerId).paymentTotal());
        assertEquals(1, incomeCount(payment.paymentId()));
        assertTrue(plotterPaymentRepository.findById(payment.paymentId()).isPresent());
    }

    @Test
    void internalAndWasteCancellationReverseTheirOwnEffectsOnce() {
        CreateInventoryItemResult roll = createRoll(PLOTTER_1, "20.0000");
        UUID orderId = createCommercialOrder();
        CreatePlotterJobResult internal = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                orderId,
                LocalDate.of(2026, 8, 4),
                roll.inventoryItemId(),
                new BigDecimal("3.0000"),
                new BigDecimal("2000"),
                null,
                PlotterJobType.INTERNAL_MAGYEN,
                null,
                false,
                PLOTTER_1
        ));
        correctPlotterJobDateUseCase.execute(new CorrectPlotterJobDateCommand(
                internal.plotterJobId(), LocalDate.of(2026, 8, 9)
        ));
        assertEquals(LocalDate.of(2026, 8, 9), ledgerDate(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE, internal.plotterJobId()
        ));
        assertEquals(LocalDate.of(2026, 8, 9), ledgerDate(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME, internal.plotterJobId()
        ));

        var cancelled = cancelPlotterJobUseCase.execute(internal.plotterJobId());
        assertEquals(PlotterJobStatus.CANCELLED, cancelled.status());
        assertTrue(financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE, internal.plotterJobId()).isEmpty());
        assertTrue(financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME, internal.plotterJobId()).isEmpty());
        cancelPlotterJobUseCase.execute(internal.plotterJobId());

        CreatePlotterJobResult waste = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                null,
                LocalDate.of(2099, 11, 4),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                BigDecimal.ZERO,
                null,
                PlotterJobType.WASTE,
                null,
                false,
                PLOTTER_1
        ));
        cancelPlotterJobUseCase.execute(waste.plotterJobId());
        cancelPlotterJobUseCase.execute(waste.plotterJobId());
        assertEquals(1, movementCount(InventoryMovementSourceType.PLOTTER_REVERSAL, waste.plotterJobId()));
        assertEquals(0, new BigDecimal("20.0000").compareTo(stock(roll.inventoryItemId())));
    }

    @Test
    void aJobWithoutDependentsIsDeletedAndThereIsNoManualChargeRoute() {
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);
        CreateInventoryItemResult roll = createRoll(PLOTTER_1, "8.0000");
        PlotterJob orphan = plotterJobRepository.save(PlotterJob.reconstitute(
                UUID.randomUUID(),
                PlotterJobType.EXTERNAL,
                customerId,
                null,
                LocalDate.of(2099, 11, 1),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                PlotterJobStatus.REGISTERED,
                null,
                PLOTTER_1
        ));

        var deleted = cancelPlotterJobUseCase.execute(orphan.getId());
        assertTrue(deleted.deleted());
        assertTrue(plotterJobRepository.findById(orphan.getId()).isEmpty());
        assertFalse(handlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(info -> info.getPatternValues().stream())
                .anyMatch(pattern -> pattern.contains("/plotter/") && pattern.contains("charge")));
    }

    private CreatePlotterJobResult createExternal(
            UUID customerId,
            UUID rollId,
            UUID machineId,
            String meters,
            String price,
            String commercialValue,
            LocalDate creationDate
    ) {
        return createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                customerId,
                null,
                creationDate,
                rollId,
                new BigDecimal(meters),
                new BigDecimal(price),
                "Cuenta",
                PlotterJobType.EXTERNAL,
                null,
                false,
                machineId,
                new BigDecimal(commercialValue)
        ));
    }

    private EditPlotterJobCommand editOf(
            CreatePlotterJobResult job,
            String meters,
            String commercialValue,
            UUID rollId
    ) {
        return new EditPlotterJobCommand(
                job.plotterJobId(),
                job.creationDate(),
                rollId,
                new BigDecimal(meters),
                job.pricePerMeter(),
                new BigDecimal(commercialValue),
                job.observations(),
                job.plotterMachineId()
        );
    }

    private void assertNullJob(UUID paymentId) {
        assertTrue(plotterPaymentRepository.findById(paymentId).orElseThrow().getPlotterJobId() == null);
    }

    private int incomeCount(UUID paymentId) {
        return (int) financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getSourceType() == FinancialTransactionSourceType.PLOTTER)
                .filter(transaction -> paymentId.equals(transaction.getSourceId()))
                .count();
    }

    private int movementCount(InventoryMovementSourceType sourceType, UUID sourceId) {
        return inventoryMovementRepository.findBySourceTypeAndSourceId(sourceType, sourceId).isPresent() ? 1 : 0;
    }

    private BigDecimal stock(UUID inventoryItemId) {
        return inventoryItemRepository.findById(inventoryItemId).orElseThrow().getStock();
    }

    private LocalDate ledgerDate(FinancialTransactionSourceType sourceType, UUID jobId) {
        return financialTransactionRepository.findBySourceTypeAndSourceId(sourceType, jobId)
                .orElseThrow()
                .getTransactionDate();
    }

    private CreateInventoryItemResult createRoll(UUID machineId, String stock) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "ACCT-" + UUID.randomUUID().toString().substring(0, 8),
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

    private UUID createCommercialOrder() {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-Account-" + UUID.randomUUID().toString().substring(0, 8)
        );
        UUID customerId = createCustomerUseCase.execute(new CreateCustomerCommand(
                "Cliente cuenta " + UUID.randomUUID().toString().substring(0, 6)
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
