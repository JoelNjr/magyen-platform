package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.CreateProductionOperatorCommand;
import com.magyen.platform.production.application.dto.CreateProductionOperatorResult;
import com.magyen.platform.production.application.dto.GetProductionLaborWorksQuery;
import com.magyen.platform.production.application.dto.GetProductionLaborWorksResult;
import com.magyen.platform.production.application.dto.GetProductionOperatorsResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.port.ProductionLaborOperatorInfo;
import com.magyen.platform.production.domain.ProductionOperator;
import com.magyen.platform.production.domain.ProductionOperatorRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProductionOperatorUseCaseTest {

    @Autowired
    private CreateProductionOperatorUseCase createProductionOperatorUseCase;

    @Autowired
    private GetProductionOperatorsUseCase getProductionOperatorsUseCase;

    @Autowired
    private ListEligibleProductionLaborOperatorsUseCase listEligibleProductionLaborOperatorsUseCase;

    @Autowired
    private ProductionOperatorRepository productionOperatorRepository;

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

    @Test
    void createsListsAndRejectsDuplicateNames() {
        String uniqueName = "Operario-" + UUID.randomUUID().toString().substring(0, 8);
        CreateProductionOperatorResult created = createProductionOperatorUseCase.execute(
                new CreateProductionOperatorCommand("  " + uniqueName + "  ")
        );

        assertEquals(uniqueName, created.name());
        assertTrue(created.active());

        GetProductionOperatorsResult all = getProductionOperatorsUseCase.execute();
        assertTrue(all.operators().stream().anyMatch(operator -> operator.operatorId().equals(created.operatorId())));

        assertThrows(IllegalArgumentException.class, () -> createProductionOperatorUseCase.execute(
                new CreateProductionOperatorCommand(uniqueName.toLowerCase())
        ));
    }

    @Test
    void inactiveOperatorIsNotSelectableButHistoricalNameRemains() {
        CreateProductionOperatorResult created = createProductionOperatorUseCase.execute(
                new CreateProductionOperatorCommand("Operario-Hist-" + UUID.randomUUID().toString().substring(0, 8))
        );

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
                        created.operatorId(),
                        LocalDate.of(2026, 8, 5),
                        "Confección",
                        new BigDecimal("10"),
                        "UNIT",
                        new BigDecimal("3000.00"),
                        "histórico"
                )
        );
        assertEquals(new BigDecimal("30000.00"), labor.calculatedAmount());
        assertEquals(created.name(), labor.operatorDisplayName());

        ProductionOperator operator = productionOperatorRepository.findById(created.operatorId()).orElseThrow();
        operator.deactivate();
        productionOperatorRepository.save(operator);

        assertTrue(listEligibleProductionLaborOperatorsUseCase.execute().stream()
                .map(ProductionLaborOperatorInfo::employeeId)
                .noneMatch(created.operatorId()::equals));

        assertThrows(ProductionDomainException.class, () -> registerProductionLaborWorkUseCase.execute(
                new RegisterProductionLaborWorkCommand(
                        productionOrder.getId(),
                        created.operatorId(),
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
        assertEquals(created.name(), history.laborWorks().getFirst().operatorDisplayName());
        assertEquals(created.operatorId(), history.laborWorks().getFirst().operatorEmployeeId());
    }
}
