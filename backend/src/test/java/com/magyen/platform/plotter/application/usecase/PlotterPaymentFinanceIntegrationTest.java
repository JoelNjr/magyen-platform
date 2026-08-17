package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.finance.application.dto.RegisterPlotterPaymentIncomeCommand;
import com.magyen.platform.finance.application.usecase.RegisterPlotterPaymentIncomeUseCase;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterPaymentsQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterPaymentsResult;
import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentCommand;
import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentResult;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PlotterPaymentFinanceIntegrationTest {

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private GetPlotterJobUseCase getPlotterJobUseCase;

    @Autowired
    private RegisterPlotterPaymentUseCase registerPlotterPaymentUseCase;

    @Autowired
    private GetPlotterPaymentsUseCase getPlotterPaymentsUseCase;

    @Autowired
    private RegisterPlotterPaymentIncomeUseCase registerPlotterPaymentIncomeUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void createJobDoesNotCreateFinanceIncome() {
        CreatePlotterJobResult job = createJob("100000.00");

        GetPlotterJobResult detail = getPlotterJobUseCase.execute(new GetPlotterJobQuery(job.plotterJobId()));
        assertEquals(new BigDecimal("0.00"), detail.paidAmount());
        assertEquals(new BigDecimal("100000.00"), detail.outstandingAmount());

        GetPlotterPaymentsResult payments = getPlotterPaymentsUseCase.execute(
                new GetPlotterPaymentsQuery(job.plotterJobId())
        );
        assertTrue(payments.payments().isEmpty());
        assertEquals(0, countPlotterIncomeForJobPayments(job.plotterJobId()));
    }

    @Test
    void registerPaymentsCreateIndependentIncomesAndRespectBalance() {
        CreatePlotterJobResult job = createJob("100000.00");

        RegisterPlotterPaymentResult first = registerPlotterPaymentUseCase.execute(
                new RegisterPlotterPaymentCommand(
                        job.plotterJobId(),
                        new BigDecimal("40000.00"),
                        LocalDate.of(2026, 8, 10),
                        "Abono 1"
                )
        );
        assertEquals(new BigDecimal("40000.00"), first.paidAmount());
        assertEquals(new BigDecimal("60000.00"), first.outstandingAmount());

        List<FinancialTransaction> firstLinked = findByPaymentId(first.paymentId());
        assertEquals(1, firstLinked.size());
        FinancialTransaction income = firstLinked.getFirst();
        assertEquals(FinancialTransactionType.INCOME, income.getType());
        assertEquals(FinancialTransactionSourceType.PLOTTER, income.getSourceType());
        assertEquals(first.paymentId(), income.getSourceId());
        assertEquals(new BigDecimal("40000.00"), income.getAmount().getValue());
        assertEquals(LocalDate.of(2026, 8, 10), income.getTransactionDate());
        assertEquals(FinancialCategory.PLOTTER_REVENUE.name(), income.getCategory());
        assertEquals("Pago de trabajo de plotter", income.getDescription());

        registerPlotterPaymentIncomeUseCase.execute(
                new RegisterPlotterPaymentIncomeCommand(
                        first.paymentId(),
                        first.amount(),
                        first.paymentDate(),
                        first.observations()
                )
        );
        assertEquals(1, findByPaymentId(first.paymentId()).size());

        RegisterPlotterPaymentResult second = registerPlotterPaymentUseCase.execute(
                new RegisterPlotterPaymentCommand(
                        job.plotterJobId(),
                        new BigDecimal("60000.00"),
                        LocalDate.of(2026, 8, 11),
                        "Saldo"
                )
        );
        assertEquals(new BigDecimal("100000.00"), second.paidAmount());
        assertEquals(new BigDecimal("0.00"), second.outstandingAmount());
        assertEquals(1, findByPaymentId(second.paymentId()).size());

        GetPlotterPaymentsResult payments = getPlotterPaymentsUseCase.execute(
                new GetPlotterPaymentsQuery(job.plotterJobId())
        );
        assertEquals(2, payments.payments().size());
        assertEquals(new BigDecimal("0.00"), payments.outstandingAmount());

        assertThrows(PlotterDomainException.class, () ->
                registerPlotterPaymentUseCase.execute(
                        new RegisterPlotterPaymentCommand(
                                job.plotterJobId(),
                                new BigDecimal("1.00"),
                                LocalDate.of(2026, 8, 12),
                                null
                        )
                )
        );
        assertEquals(2, countPlotterIncomeForJobPayments(job.plotterJobId()));
    }

    @Test
    void rejectsZeroAndNegativePayments() {
        CreatePlotterJobResult job = createJob("50000.00");

        assertThrows(PlotterDomainException.class, () ->
                registerPlotterPaymentUseCase.execute(
                        new RegisterPlotterPaymentCommand(
                                job.plotterJobId(),
                                BigDecimal.ZERO,
                                LocalDate.of(2026, 8, 10),
                                null
                        )
                )
        );
        assertThrows(PlotterDomainException.class, () ->
                registerPlotterPaymentUseCase.execute(
                        new RegisterPlotterPaymentCommand(
                                job.plotterJobId(),
                                new BigDecimal("-10.00"),
                                LocalDate.of(2026, 8, 10),
                                null
                        )
                )
        );
        assertEquals(0, countPlotterIncomeForJobPayments(job.plotterJobId()));
    }

    private CreatePlotterJobResult createJob(String pricePerMeterForOneMeter) {
        CreateInventoryItemResult roll = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        "PLTPAY-" + UUID.randomUUID().toString().substring(0, 8),
                        "Papel plotter pago",
                        "PAPER",
                        "METER",
                        new BigDecimal("100.0000"),
                        new BigDecimal("10.0000"),
                        null,
                        new BigDecimal("1000.00"),
                        "PAPER",
                        true
                )
        );

        return createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                null,
                null,
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal(pricePerMeterForOneMeter),
                "Job pago Inc8"
        ));
    }

    private List<FinancialTransaction> findByPaymentId(UUID paymentId) {
        return financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.PLOTTER, paymentId)
                .stream()
                .toList();
    }

    private int countPlotterIncomeForJobPayments(UUID plotterJobId) {
        GetPlotterPaymentsResult payments = getPlotterPaymentsUseCase.execute(
                new GetPlotterPaymentsQuery(plotterJobId)
        );
        int count = 0;
        for (var payment : payments.payments()) {
            count += findByPaymentId(payment.paymentId()).size();
        }
        return count;
    }
}
