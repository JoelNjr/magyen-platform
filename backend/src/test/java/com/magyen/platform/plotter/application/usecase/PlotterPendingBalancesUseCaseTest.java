package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.GetPlotterPendingBalancesResult;
import com.magyen.platform.plotter.application.dto.PlotterCustomerPendingBalance;
import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentCommand;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.shared.testsupport.PlotterCustomerFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PlotterPendingBalancesUseCaseTest {

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private RegisterPlotterPaymentUseCase registerPlotterPaymentUseCase;

    @Autowired
    private GetPlotterPendingBalancesUseCase getPlotterPendingBalancesUseCase;

    @Autowired
    private PlotterJobRepository plotterJobRepository;

    @Autowired
    private PlotterPaymentRepository plotterPaymentRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void sumsOnlyOpenExternalJobsAndDoesNotCreateFinanceTransactions() {
        UUID customerId = PlotterCustomerFixture.create(createCustomerUseCase);
        UUID otherCustomerId = PlotterCustomerFixture.create(createCustomerUseCase);
        CreateInventoryItemResult roll = createPaperRoll();
        long financeBefore = financialTransactionRepository.findAllNewestFirst().size();

        var unpaid = createExternal(customerId, roll, LocalDate.of(2020, 1, 15), "100.00");
        var partial = createExternal(customerId, roll, LocalDate.of(2020, 2, 15), "80.00");
        var settled = createExternal(customerId, roll, LocalDate.of(2020, 3, 15), "50.00");
        var otherOpen = createExternal(otherCustomerId, roll, LocalDate.of(2020, 4, 15), "30.00");
        registerPlotterPaymentUseCase.execute(new RegisterPlotterPaymentCommand(
                partial.plotterJobId(),
                new BigDecimal("30.00"),
                LocalDate.of(2020, 2, 16),
                "abono"
        ));
        registerPlotterPaymentUseCase.execute(new RegisterPlotterPaymentCommand(
                settled.plotterJobId(),
                new BigDecimal("50.00"),
                LocalDate.of(2020, 3, 16),
                "saldo"
        ));

        plotterJobRepository.save(PlotterJob.reconstitute(
                UUID.randomUUID(),
                PlotterJobType.INTERNAL_MAGYEN,
                customerId,
                UUID.randomUUID(),
                LocalDate.of(2020, 1, 1),
                roll.inventoryItemId(),
                new BigDecimal("2.0000"),
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                PlotterJobStatus.REGISTERED,
                "interno no es deuda"
        ));
        plotterJobRepository.save(PlotterJob.reconstitute(
                UUID.randomUUID(),
                PlotterJobType.WASTE,
                null,
                null,
                LocalDate.of(2020, 1, 2),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO.setScale(2),
                PlotterJobStatus.REGISTERED,
                "merma"
        ));
        UUID cancelledId = UUID.randomUUID();
        plotterJobRepository.save(PlotterJob.reconstitute(
                cancelledId,
                PlotterJobType.EXTERNAL,
                customerId,
                null,
                LocalDate.of(2020, 1, 3),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("999.00"),
                new BigDecimal("999.00"),
                PlotterJobStatus.CANCELLED,
                "cancelado"
        ));
        UUID negativeJobId = UUID.randomUUID();
        plotterJobRepository.save(PlotterJob.reconstitute(
                negativeJobId,
                PlotterJobType.EXTERNAL,
                customerId,
                null,
                LocalDate.of(2020, 5, 1),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                PlotterJobStatus.REGISTERED,
                "inconsistencia"
        ));
        plotterPaymentRepository.save(PlotterPayment.reconstitute(
                UUID.randomUUID(),
                negativeJobId,
                new BigDecimal("15.00"),
                LocalDate.of(2020, 5, 2),
                "pago mayor"
        ));

        GetPlotterPendingBalancesResult beforeMonthConcept = getPlotterPendingBalancesUseCase.execute();
        assertEquals(financeBefore + 2, financialTransactionRepository.findAllNewestFirst().size());

        PlotterCustomerPendingBalance customer = beforeMonthConcept.customers().stream()
                .filter(item -> customerId.equals(item.customerId()))
                .findFirst()
                .orElseThrow();
        assertEquals(2, customer.openJobCount());
        assertEquals(new BigDecimal("180.00"), customer.billedAmount());
        assertEquals(new BigDecimal("30.00"), customer.paidAmount());
        assertEquals(new BigDecimal("150.00"), customer.outstandingAmount());
        assertEquals(2, customer.jobs().size());
        assertTrue(customer.jobs().stream().anyMatch(job -> job.plotterJobId().equals(unpaid.plotterJobId())));
        assertTrue(customer.jobs().stream().noneMatch(job -> job.plotterJobId().equals(settled.plotterJobId())));
        assertTrue(customer.jobs().stream().noneMatch(job -> job.plotterJobId().equals(cancelledId)));
        assertEquals(LocalDate.of(2020, 1, 15), customer.jobs().getFirst().creationDate());

        PlotterCustomerPendingBalance other = beforeMonthConcept.customers().stream()
                .filter(item -> otherCustomerId.equals(item.customerId()))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("30.00"), other.outstandingAmount());

        assertTrue(beforeMonthConcept.negativeBalances().stream()
                .anyMatch(item -> negativeJobId.equals(item.plotterJobId())));
        assertTrue(beforeMonthConcept.customers().stream()
                .flatMap(item -> item.jobs().stream())
                .noneMatch(job -> negativeJobId.equals(job.plotterJobId())));
        assertTrue(beforeMonthConcept.outstandingAmount().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(financeBefore + 2, financialTransactionRepository.findAllNewestFirst().size());

        PlotterCustomerPendingBalance januaryCustomer = customerInMonth(
                beforeMonthConcept,
                2020,
                1,
                customerId
        );
        assertEquals(new BigDecimal("100.00"), januaryCustomer.outstandingAmount());
        assertEquals(LocalDate.of(2020, 1, 15), januaryCustomer.jobs().getFirst().creationDate());

        PlotterCustomerPendingBalance februaryCustomer = customerInMonth(
                beforeMonthConcept,
                2020,
                2,
                customerId
        );
        assertEquals(new BigDecimal("80.00"), februaryCustomer.billedAmount());
        assertEquals(new BigDecimal("30.00"), februaryCustomer.paidAmount());
        assertEquals(new BigDecimal("50.00"), februaryCustomer.outstandingAmount());

        PlotterCustomerPendingBalance aprilCustomer = customerInMonth(
                beforeMonthConcept,
                2020,
                4,
                otherCustomerId
        );
        assertEquals(new BigDecimal("30.00"), aprilCustomer.outstandingAmount());

        assertTrue(beforeMonthConcept.months().stream()
                .filter(month -> month.year() == 2020 && month.month() == 3)
                .flatMap(month -> month.customers().stream())
                .filter(item -> customerId.equals(item.customerId()))
                .findAny()
                .isEmpty());

        List<YearMonth> monthOrder = beforeMonthConcept.months().stream()
                .map(month -> YearMonth.of(month.year(), month.month()))
                .toList();
        assertTrue(monthOrder.indexOf(YearMonth.of(2020, 4)) < monthOrder.indexOf(YearMonth.of(2020, 2)));
        assertTrue(monthOrder.indexOf(YearMonth.of(2020, 2)) < monthOrder.indexOf(YearMonth.of(2020, 1)));

        BigDecimal groupedOutstanding = beforeMonthConcept.months().stream()
                .map(month -> month.outstandingAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, groupedOutstanding.compareTo(beforeMonthConcept.outstandingAmount()));
    }

    private static PlotterCustomerPendingBalance customerInMonth(
            GetPlotterPendingBalancesResult result,
            int year,
            int month,
            UUID customerId
    ) {
        return result.months().stream()
                .filter(item -> item.year() == year && item.month() == month)
                .flatMap(item -> item.customers().stream())
                .filter(item -> customerId.equals(item.customerId()))
                .findFirst()
                .orElseThrow();
    }

    private com.magyen.platform.plotter.application.dto.CreatePlotterJobResult createExternal(
            UUID customerId,
            CreateInventoryItemResult roll,
            LocalDate date,
            String price
    ) {
        return createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                customerId,
                null,
                date,
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal(price),
                null,
                PlotterJobType.EXTERNAL,
                null
        ));
    }

    private CreateInventoryItemResult createPaperRoll() {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "BAL-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel saldo",
                "PAPER",
                "METER",
                new BigDecimal("40.0000"),
                new BigDecimal("1.0000"),
                null,
                new BigDecimal("1000.00"),
                "PAPER",
                true
        ));
    }
}
