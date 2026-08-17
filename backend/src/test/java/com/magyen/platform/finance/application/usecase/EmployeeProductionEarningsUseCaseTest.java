package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsResult;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.production.application.dto.CancelProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.PayProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.usecase.CancelProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.PayProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class EmployeeProductionEarningsUseCaseTest {

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private GetPayrollEmployeeProductionEarningsUseCase getPayrollEmployeeProductionEarningsUseCase;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private RegisterProductionLaborWorkUseCase registerProductionLaborWorkUseCase;

    @Autowired
    private PayProductionLaborWorkUseCase payProductionLaborWorkUseCase;

    @Autowired
    private CancelProductionLaborWorkUseCase cancelProductionLaborWorkUseCase;

    @Test
    void productionEarningsCountPendingAndPaidAndExcludeCancelled() {
        CreatePayrollEmployeeResult jeanCarlos = createProductionBased("Jean Carlos-" + suffix());
        UUID productionOrderId = startInProgressOrder();

        RegisterProductionLaborWorkResult pending = registerLabor(productionOrderId, jeanCarlos.employeeId(), "10", "3000.00");
        RegisterProductionLaborWorkResult paid = registerLabor(productionOrderId, jeanCarlos.employeeId(), "8", "2000.00");
        RegisterProductionLaborWorkResult cancelled = registerLabor(productionOrderId, jeanCarlos.employeeId(), "5", "1000.00");

        payProductionLaborWorkUseCase.execute(new PayProductionLaborWorkCommand(
                productionOrderId,
                paid.laborWorkId(),
                LocalDate.of(2026, 8, 11),
                "pago"
        ));
        cancelProductionLaborWorkUseCase.execute(
                new CancelProductionLaborWorkCommand(productionOrderId, cancelled.laborWorkId())
        );

        GetPayrollEmployeeProductionEarningsResult earnings =
                getPayrollEmployeeProductionEarningsUseCase.execute(new GetPayrollEmployeeProductionEarningsQuery(
                        jeanCarlos.employeeId(),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                ));

        assertTrue(earnings.productionLaborApplicable());
        assertEquals(jeanCarlos.employeeId(), earnings.employeeId());
        assertEquals(2, earnings.laborWorkCount());
        assertEquals(0, new BigDecimal("18").compareTo(earnings.totalQuantity()));
        assertEquals(new BigDecimal("46000.00"), earnings.totalCalculatedAmount());
        assertEquals(new BigDecimal("16000.00"), earnings.totalPaidAmount());
        assertEquals(new BigDecimal("30000.00"), earnings.totalPendingAmount());
        assertEquals(pending.calculatedAmount(), earnings.totalPendingAmount());
        assertEquals(paid.calculatedAmount(), earnings.totalPaidAmount());
    }

    @Test
    void fixedPayrollEmployeeDoesNotExposeProductionEarnings() {
        CreatePayrollEmployeeResult fixed = createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                "Ana Fija-" + suffix(),
                PayrollCompensationType.FIXED_PAYROLL,
                new BigDecimal("1500000.00"),
                LocalDate.of(2026, 8, 1),
                null
        ));

        GetPayrollEmployeeProductionEarningsResult earnings =
                getPayrollEmployeeProductionEarningsUseCase.execute(new GetPayrollEmployeeProductionEarningsQuery(
                        fixed.employeeId(),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                ));

        assertFalse(earnings.productionLaborApplicable());
        assertEquals(PayrollCompensationType.FIXED_PAYROLL, earnings.compensationType());
        assertEquals(0, earnings.laborWorkCount());
        assertEquals(new BigDecimal("0.00"), earnings.totalCalculatedAmount());
        assertEquals(new BigDecimal("0.00"), earnings.totalPaidAmount());
        assertEquals(new BigDecimal("0.00"), earnings.totalPendingAmount());
    }

    @Test
    void earningsOutsideDateRangeAreExcluded() {
        CreatePayrollEmployeeResult employee = createProductionBased("Maria-" + suffix());
        UUID productionOrderId = startInProgressOrder();
        registerLabor(productionOrderId, employee.employeeId(), "10", "3000.00");

        GetPayrollEmployeeProductionEarningsResult july =
                getPayrollEmployeeProductionEarningsUseCase.execute(new GetPayrollEmployeeProductionEarningsQuery(
                        employee.employeeId(),
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 31)
                ));

        assertEquals(0, july.laborWorkCount());
        assertEquals(new BigDecimal("0.00"), july.totalCalculatedAmount());
    }

    private UUID startInProgressOrder() {
        ProductionOrder created = productionOrderRepository.save(ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                created.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(created.getId(), null));
        return created.getId();
    }

    private RegisterProductionLaborWorkResult registerLabor(
            UUID productionOrderId,
            UUID employeeId,
            String quantity,
            String unitRate
    ) {
        return registerProductionLaborWorkUseCase.execute(new RegisterProductionLaborWorkCommand(
                productionOrderId,
                employeeId,
                LocalDate.of(2026, 8, 10),
                "Confección",
                new BigDecimal(quantity),
                "UNIT",
                new BigDecimal(unitRate),
                null
        ));
    }

    private CreatePayrollEmployeeResult createProductionBased(String name) {
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
