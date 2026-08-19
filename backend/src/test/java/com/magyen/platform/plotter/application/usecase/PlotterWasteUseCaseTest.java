package com.magyen.platform.plotter.application.usecase;

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
import com.magyen.platform.plotter.application.dto.GetPlotterJobsQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterProfitabilityQuery;
import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentCommand;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.PlotterProfitabilityScope;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
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
class PlotterWasteUseCaseTest {

    private static final LocalDate JOB_DATE = LocalDate.of(2099, 6, 10);

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private RegisterPlotterPaymentUseCase registerPlotterPaymentUseCase;

    @Autowired
    private GetPlotterJobsUseCase getPlotterJobsUseCase;

    @Autowired
    private GetPlotterProfitabilityUseCase getPlotterProfitabilityUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void wasteConsumesPaperWithoutCustomerPaymentOrFinanceIncome() {
        CreateInventoryItemResult roll = createPaperRoll("40.0000");
        long financeBefore = financialTransactionRepository.findAllNewestFirst().size();

        CreatePlotterJobResult waste = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                null,
                JOB_DATE,
                roll.inventoryItemId(),
                new BigDecimal("5.0000"),
                null,
                "prueba fallida",
                PlotterJobType.WASTE,
                null
        ));

        assertEquals(PlotterJobType.WASTE, waste.jobType());
        assertNull(waste.customerId());
        assertNull(waste.orderId());
        assertEquals(new BigDecimal("0.00"), waste.totalAmount());

        var movement = inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, waste.plotterJobId())
                .orElseThrow();
        assertEquals(InventoryMovementType.OUT, movement.getMovementType());
        assertEquals(0, new BigDecimal("5.0000").compareTo(movement.getQuantity()));
        assertEquals(financeBefore, financialTransactionRepository.findAllNewestFirst().size());
        assertEquals(0, countPlotterIncome(waste.plotterJobId()));
        assertEquals(0, countInternalExpenses(waste.plotterJobId()));

        assertThrows(PlotterDomainException.class, () ->
                registerPlotterPaymentUseCase.execute(new RegisterPlotterPaymentCommand(
                        waste.plotterJobId(),
                        new BigDecimal("1000.00"),
                        JOB_DATE,
                        null
                ))
        );
        assertEquals(financeBefore, financialTransactionRepository.findAllNewestFirst().size());
    }

    @Test
    void wasteAppearsInAnalyticsAndWasteScopeFilter() {
        CreateInventoryItemResult roll = createPaperRoll("40.0000");
        CreatePlotterJobResult waste = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                null,
                JOB_DATE,
                roll.inventoryItemId(),
                new BigDecimal("7.0000"),
                BigDecimal.ZERO,
                "muestra",
                PlotterJobType.WASTE,
                null
        ));

        var all = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(
                        LocalDate.of(2099, 6, 1),
                        LocalDate.of(2099, 6, 30),
                        PlotterProfitabilityScope.ALL
                )
        );
        assertEquals(1, all.wasteJobCount());
        assertEquals(new BigDecimal("7.0000"), all.wastePrintedMeters());
        assertEquals(new BigDecimal("7.0000"), all.totalPaperPrintedMeters());
        assertEquals(new BigDecimal("0.00"), all.externalRevenue());
        assertEquals(new BigDecimal("0.00"), all.internalRevenue());
        assertEquals(new BigDecimal("0.00"), all.externalPaidAmount());
        assertEquals(new BigDecimal("0.00"), all.externalOutstandingAmount());

        var wasteScope = getPlotterProfitabilityUseCase.execute(
                new GetPlotterProfitabilityQuery(
                        LocalDate.of(2099, 6, 1),
                        LocalDate.of(2099, 6, 30),
                        PlotterProfitabilityScope.WASTE
                )
        );
        assertEquals(1, wasteScope.wasteJobCount());
        assertEquals(0, wasteScope.externalJobCount());
        assertEquals(0, wasteScope.internalJobCount());

        var july = getPlotterJobsUseCase.execute(
                new GetPlotterJobsQuery(LocalDate.of(2099, 7, 1), LocalDate.of(2099, 7, 31))
        );
        assertTrue(july.jobs().stream().noneMatch(job -> waste.plotterJobId().equals(job.plotterJobId())));

        var june = getPlotterJobsUseCase.execute(
                new GetPlotterJobsQuery(LocalDate.of(2099, 6, 1), LocalDate.of(2099, 6, 30))
        );
        var listedWaste = june.jobs().stream()
                .filter(job -> waste.plotterJobId().equals(job.plotterJobId()))
                .findFirst()
                .orElseThrow();
        assertEquals(PlotterJobType.WASTE, listedWaste.jobType());
        assertEquals(new BigDecimal("0.00"), listedWaste.paidAmount());
        assertEquals(new BigDecimal("0.00"), listedWaste.outstandingAmount());
    }

    @Test
    void wasteRejectsCustomerOrOrder() {
        CreateInventoryItemResult roll = createPaperRoll("10.0000");
        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        UUID.randomUUID(),
                        null,
                        JOB_DATE,
                        roll.inventoryItemId(),
                        new BigDecimal("1.0000"),
                        BigDecimal.ZERO,
                        null,
                        PlotterJobType.WASTE,
                        null
                ))
        );
        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        null,
                        UUID.randomUUID(),
                        JOB_DATE,
                        roll.inventoryItemId(),
                        new BigDecimal("1.0000"),
                        BigDecimal.ZERO,
                        null,
                        PlotterJobType.WASTE,
                        null
                ))
        );
    }

    private CreateInventoryItemResult createPaperRoll(String stock) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "WST-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel merma",
                "PAPER",
                "METER",
                new BigDecimal(stock),
                new BigDecimal("5.0000"),
                null,
                new BigDecimal("5000.00"),
                "PAPER",
                true
        ));
    }

    private long countPlotterIncome(UUID plotterJobId) {
        return financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getSourceType() == FinancialTransactionSourceType.PLOTTER)
                .filter(transaction -> transaction.getType() == FinancialTransactionType.INCOME)
                .filter(transaction -> plotterJobId.equals(transaction.getSourceId()))
                .count();
    }

    private long countInternalExpenses(UUID plotterJobId) {
        return financialTransactionRepository.findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE,
                        plotterJobId
                )
                .stream()
                .count();
    }
}
