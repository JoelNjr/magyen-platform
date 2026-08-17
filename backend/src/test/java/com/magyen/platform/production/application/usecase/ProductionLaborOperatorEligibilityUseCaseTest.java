package com.magyen.platform.production.application.usecase;

import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.DeactivatePayrollEmployeeUseCase;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.production.application.dto.GetProductionLaborWorksQuery;
import com.magyen.platform.production.application.dto.GetProductionLaborWorksResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.port.ProductionLaborOperatorInfo;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProductionLaborOperatorEligibilityUseCaseTest {

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private DeactivatePayrollEmployeeUseCase deactivatePayrollEmployeeUseCase;

    @Autowired
    private ListEligibleProductionLaborOperatorsUseCase listEligibleProductionLaborOperatorsUseCase;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private RegisterProductionLaborWorkUseCase registerProductionLaborWorkUseCase;

    @Autowired
    private GetProductionLaborWorksUseCase getProductionLaborWorksUseCase;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void selectorReturnsOnlyActiveProductionBasedEmployees() {
        CreatePayrollEmployeeResult productionBased = createProductionBased("Jean Carlos-" + suffix());
        CreatePayrollEmployeeResult fixed = createFixed("Ana Fija-" + suffix());
        CreatePayrollEmployeeResult inactive = createProductionBased("Maria Inactiva-" + suffix());
        deactivatePayrollEmployeeUseCase.execute(new DeactivatePayrollEmployeeCommand(inactive.employeeId()));

        var eligibleIds = listEligibleProductionLaborOperatorsUseCase.execute().stream()
                .map(ProductionLaborOperatorInfo::employeeId)
                .toList();

        assertTrue(eligibleIds.contains(productionBased.employeeId()));
        assertFalse(eligibleIds.contains(fixed.employeeId()));
        assertFalse(eligibleIds.contains(inactive.employeeId()));
    }

    @Test
    void inactiveOperatorIsNotSelectableButHistoricalNameRemains() {
        CreatePayrollEmployeeResult created = createProductionBased("Operario-Hist-" + suffix());

        ProductionOrder productionOrder = productionOrderRepository.save(ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                productionOrder.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(productionOrder.getId(), null));

        RegisterProductionLaborWorkResult labor = registerProductionLaborWorkUseCase.execute(
                new RegisterProductionLaborWorkCommand(
                        productionOrder.getId(),
                        created.employeeId(),
                        LocalDate.of(2026, 8, 5),
                        "Confección",
                        new BigDecimal("10"),
                        "UNIT",
                        new BigDecimal("3000.00"),
                        "histórico"
                )
        );
        assertEquals(created.employeeId(), labor.operatorEmployeeId());
        assertEquals(new BigDecimal("30000.00"), labor.calculatedAmount());
        assertEquals(created.displayName(), labor.operatorDisplayName());

        deactivatePayrollEmployeeUseCase.execute(new DeactivatePayrollEmployeeCommand(created.employeeId()));

        assertTrue(listEligibleProductionLaborOperatorsUseCase.execute().stream()
                .map(ProductionLaborOperatorInfo::employeeId)
                .noneMatch(created.employeeId()::equals));

        assertThrows(ProductionDomainException.class, () -> registerProductionLaborWorkUseCase.execute(
                new RegisterProductionLaborWorkCommand(
                        productionOrder.getId(),
                        created.employeeId(),
                        LocalDate.of(2026, 8, 5),
                        "Confección",
                        new BigDecimal("1"),
                        "UNIT",
                        new BigDecimal("3000.00"),
                        "inactive"
                )
        ));

        GetProductionLaborWorksResult history = getProductionLaborWorksUseCase.execute(
                new GetProductionLaborWorksQuery(productionOrder.getId())
        );
        assertEquals(created.displayName(), history.laborWorks().getFirst().operatorDisplayName());
        assertEquals(created.employeeId(), history.laborWorks().getFirst().operatorEmployeeId());
    }

    @Test
    void fixedPayrollEmployeeCannotReceiveNewLabor() {
        CreatePayrollEmployeeResult fixed = createFixed("Luis Fijo-" + suffix());
        ProductionOrder productionOrder = startInProgressOrder();

        assertThrows(ProductionDomainException.class, () -> registerProductionLaborWorkUseCase.execute(
                new RegisterProductionLaborWorkCommand(
                        productionOrder.getId(),
                        fixed.employeeId(),
                        LocalDate.of(2026, 8, 5),
                        "Confección",
                        new BigDecimal("1"),
                        "UNIT",
                        new BigDecimal("3000.00"),
                        "fixed"
                )
        ));
    }

    @Test
    void productionOperatorCatalogIsNoLongerASourceOfTruth() {
        assertFalse(applicationContext.containsBean("createProductionOperatorUseCase"));
        assertFalse(applicationContext.containsBean("getProductionOperatorsUseCase"));
        assertFalse(applicationContext.containsBean("productionOperatorRepository"));
        assertFalse(applicationContext.containsBean("productionLaborOperatorAdapter"));
    }

    private ProductionOrder startInProgressOrder() {
        ProductionOrder productionOrder = productionOrderRepository.save(ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                productionOrder.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(productionOrder.getId(), null));
        return productionOrder;
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

    private CreatePayrollEmployeeResult createFixed(String name) {
        return createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                name,
                PayrollCompensationType.FIXED_PAYROLL,
                new BigDecimal("1500000.00"),
                LocalDate.of(2026, 8, 1),
                null
        ));
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
