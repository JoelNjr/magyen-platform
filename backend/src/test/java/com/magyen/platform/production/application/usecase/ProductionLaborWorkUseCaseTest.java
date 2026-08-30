package com.magyen.platform.production.application.usecase;

import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.DeactivatePayrollEmployeeUseCase;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.production.application.dto.CancelProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.GetProductionLaborWorksQuery;
import com.magyen.platform.production.application.dto.GetProductionLaborWorksResult;
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.PayProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.PayProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.domain.ProductionLaborWorkStatus;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.production.domain.exception.ProductionLaborWorkAlreadyPaidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProductionLaborWorkUseCaseTest {

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private DeactivatePayrollEmployeeUseCase deactivatePayrollEmployeeUseCase;

    @Autowired
    private RegisterProductionLaborWorkUseCase registerProductionLaborWorkUseCase;

    @Autowired
    private GetProductionLaborWorksUseCase getProductionLaborWorksUseCase;

    @Autowired
    private PayProductionLaborWorkUseCase payProductionLaborWorkUseCase;

    @Autowired
    private CancelProductionLaborWorkUseCase cancelProductionLaborWorkUseCase;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private GetProductionOrderUseCase getProductionOrderUseCase;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private UUID productionOrderId;

    @BeforeEach
    void setUp() {
        ProductionOrder created = productionOrderRepository.save(ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        ));
        productionOrderId = created.getId();
    }

    @Test
    void coversRegistrationPaymentCancelAndCostAttributionCases() {
        CreatePayrollEmployeeResult productionOperator = createProductionBasedEmployee("Jean Carlos-" + suffix());
        UUID unknownOperatorId = UUID.randomUUID();
        CreatePayrollEmployeeResult secondOperator = createProductionBasedEmployee("Maria-" + suffix());
        CreatePayrollEmployeeResult inactiveOperator = createProductionBasedEmployee("Operario-Inactivo-" + suffix());
        deactivatePayrollEmployeeUseCase.execute(new DeactivatePayrollEmployeeCommand(inactiveOperator.employeeId()));

        assertThrows(ProductionDomainException.class, () -> register(
                productionOperator.employeeId(),
                "100",
                "800.00",
                "before-start"
        ));

        moveToInProgress();

        assertThrows(ProductionDomainException.class, () -> register(
                unknownOperatorId,
                "100",
                "800.00",
                "unknown-rejected"
        ));
        assertThrows(ProductionDomainException.class, () -> register(
                inactiveOperator.employeeId(),
                "100",
                "800.00",
                "inactive-rejected"
        ));
        assertThrows(IllegalArgumentException.class, () -> registerProductionLaborWorkUseCase.execute(
                new RegisterProductionLaborWorkCommand(
                        UUID.randomUUID(),
                        productionOperator.employeeId(),
                        LocalDate.of(2026, 8, 10),
                        "Confección",
                        new BigDecimal("100"),
                        "UNIT",
                        new BigDecimal("800.00"),
                        null
                )
        ));
        assertThrows(ProductionDomainException.class, () -> register(
                productionOperator.employeeId(),
                "0",
                "800.00",
                "qty"
        ));
        assertThrows(ProductionDomainException.class, () -> register(
                productionOperator.employeeId(),
                "10",
                "-1.00",
                "rate"
        ));

        long financeBefore = countPayrollExpenses();

        RegisterProductionLaborWorkResult first = register(
                productionOperator.employeeId(),
                "100",
                "800.00",
                "first"
        );
        assertEquals(productionOperator.employeeId(), first.operatorEmployeeId());
        assertEquals(new BigDecimal("80000.00"), first.calculatedAmount());
        assertEquals(ProductionLaborWorkStatus.PENDING, first.status());
        assertEquals(financeBefore, countPayrollExpenses());

        RegisterProductionLaborWorkResult secondSameOperator = register(
                productionOperator.employeeId(),
                "50",
                "200.00",
                "second-same"
        );
        RegisterProductionLaborWorkResult otherOperator = register(
                secondOperator.employeeId(),
                "100",
                "300.00",
                "other-operator"
        );
        assertEquals(new BigDecimal("10000.00"), secondSameOperator.calculatedAmount());
        assertEquals(new BigDecimal("30000.00"), otherOperator.calculatedAmount());
        assertEquals(financeBefore, countPayrollExpenses());

        GetProductionLaborWorksResult history = getProductionLaborWorksUseCase.execute(
                new GetProductionLaborWorksQuery(productionOrderId)
        );
        assertEquals(3, history.laborWorks().size());
        assertEquals(new BigDecimal("120000.00"), history.laborCostSummary().totalLaborCost());
        assertEquals(3, history.laborCostSummary().pendingCount());

        PayProductionLaborWorkResult paid = payProductionLaborWorkUseCase.execute(
                new PayProductionLaborWorkCommand(
                        productionOrderId,
                        first.laborWorkId(),
                        LocalDate.of(2026, 8, 11),
                        "pago"
                )
        );
        assertEquals(ProductionLaborWorkStatus.PAID, paid.status());
        assertNotNull(paid.financialTransactionId());
        assertEquals(financeBefore + 1, countPayrollExpenses());

        FinancialTransaction expense = financialTransactionRepository
                .findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.PAYROLL,
                        com.magyen.platform.finance.domain.LaborPaymentWeek.of(LocalDate.of(2026, 8, 11)).sourceId()
                )
                .orElseThrow();
        assertEquals(FinancialTransactionType.EXPENSE, expense.getType());
        assertEquals("PAYROLL", expense.getCategory());
        assertEquals(new BigDecimal("80000.00"), expense.getAmount().getValue());
        assertTrue(expense.getDescription().contains("Mano de obra"));
        assertTrue(expense.getDescription().contains("1 pago"));

        PayProductionLaborWorkResult paidSameWeek = payProductionLaborWorkUseCase.execute(
                new PayProductionLaborWorkCommand(
                        productionOrderId,
                        otherOperator.laborWorkId(),
                        LocalDate.of(2026, 8, 12),
                        null
                )
        );
        assertEquals(paid.financialTransactionId(), paidSameWeek.financialTransactionId());
        assertEquals(financeBefore + 1, countPayrollExpenses());
        FinancialTransaction weekly = financialTransactionRepository
                .findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.PAYROLL,
                        com.magyen.platform.finance.domain.LaborPaymentWeek.of(LocalDate.of(2026, 8, 12)).sourceId()
                )
                .orElseThrow();
        assertEquals(new BigDecimal("110000.00"), weekly.getAmount().getValue());
        assertTrue(weekly.getDescription().contains("2 pagos"));

        assertThrows(ProductionLaborWorkAlreadyPaidException.class, () -> payProductionLaborWorkUseCase.execute(
                new PayProductionLaborWorkCommand(
                        productionOrderId,
                        first.laborWorkId(),
                        LocalDate.of(2026, 8, 12),
                        null
                )
        ));
        assertEquals(financeBefore + 1, countPayrollExpenses());

        assertThrows(ProductionDomainException.class, () -> cancelProductionLaborWorkUseCase.execute(
                new CancelProductionLaborWorkCommand(productionOrderId, first.laborWorkId())
        ));

        cancelProductionLaborWorkUseCase.execute(
                new CancelProductionLaborWorkCommand(productionOrderId, secondSameOperator.laborWorkId())
        );
        assertEquals(financeBefore + 1, countPayrollExpenses());

        assertThrows(ProductionDomainException.class, () -> payProductionLaborWorkUseCase.execute(
                new PayProductionLaborWorkCommand(
                        productionOrderId,
                        secondSameOperator.laborWorkId(),
                        LocalDate.of(2026, 8, 12),
                        null
                )
        ));

        GetProductionOrderResult order = getProductionOrderUseCase.execute(
                new GetProductionOrderCommand(productionOrderId)
        );
        assertEquals(new BigDecimal("110000.00"), order.laborCostSummary().totalLaborCost());
        assertEquals(2, order.laborCostSummary().laborWorkCount());
        assertEquals(0, order.laborCostSummary().pendingCount());
        assertEquals(2, order.laborCostSummary().paidCount());
        assertEquals(new BigDecimal("110000.00"), order.totalProductionCost());

        ProductionOrder reloaded = productionOrderRepository.findById(productionOrderId).orElseThrow();
        assertEquals(
                new BigDecimal("800.00"),
                reloaded.requireLaborWork(first.laborWorkId()).getUnitRate()
        );
        assertEquals(
                new BigDecimal("80000.00"),
                reloaded.requireLaborWork(first.laborWorkId()).getCalculatedAmount()
        );
        assertEquals(otherOperator.laborWorkId(), reloaded.requireLaborWork(otherOperator.laborWorkId()).getId());
    }

    private RegisterProductionLaborWorkResult register(
            UUID operatorEmployeeId,
            String quantity,
            String unitRate,
            String observation
    ) {
        return registerProductionLaborWorkUseCase.execute(new RegisterProductionLaborWorkCommand(
                productionOrderId,
                operatorEmployeeId,
                LocalDate.of(2026, 8, 10),
                "Confección",
                new BigDecimal(quantity),
                "UNIT",
                new BigDecimal(unitRate),
                observation
        ));
    }

    private void moveToInProgress() {
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                productionOrderId,
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(productionOrderId, null));
    }

    private CreatePayrollEmployeeResult createProductionBasedEmployee(String name) {
        return createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                name,
                PayrollCompensationType.PRODUCTION_BASED,
                null,
                null,
                null
        ));
    }

    private long countPayrollExpenses() {
        List<FinancialTransaction> all = financialTransactionRepository.findAllNewestFirst();
        return all.stream()
                .filter(transaction -> transaction.getSourceType() == FinancialTransactionSourceType.PAYROLL)
                .filter(transaction -> transaction.getType() == FinancialTransactionType.EXPENSE)
                .count();
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
