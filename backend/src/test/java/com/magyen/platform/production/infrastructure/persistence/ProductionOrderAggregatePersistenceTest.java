package com.magyen.platform.production.infrastructure.persistence;

import com.magyen.platform.production.application.dto.AddProductionOperationCommand;
import com.magyen.platform.production.application.dto.AddProductionOperationResult;
import com.magyen.platform.production.application.dto.AssignProductionOperationOperatorCommand;
import com.magyen.platform.production.application.dto.CompleteProductionOperationCommand;
import com.magyen.platform.production.application.dto.CompleteProductionOrderCommand;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.StartProductionOperationCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.usecase.AddProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.AssignProductionOperationOperatorUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.domain.ProductionOperation;
import com.magyen.platform.production.domain.ProductionOperationStatus;
import com.magyen.platform.production.domain.ProductionOperationType;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que la persistencia del agregado ProductionOrder preserve estado e identidades
 * a través del ciclo de vida completo (plan / start / complete y operaciones).
 * <p>
 * El test hace flush + clear entre pasos para forzar recarga desde la base de datos.
 */
@SpringBootTest
@Transactional
class ProductionOrderAggregatePersistenceTest {

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private AddProductionOperationUseCase addProductionOperationUseCase;

    @Autowired
    private AssignProductionOperationOperatorUseCase assignProductionOperationOperatorUseCase;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private StartProductionOperationUseCase startProductionOperationUseCase;

    @Autowired
    private CompleteProductionOperationUseCase completeProductionOperationUseCase;

    @Autowired
    private CompleteProductionOrderUseCase completeProductionOrderUseCase;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsCompleteAggregateLifecycleWithoutLosingOperationsOrIds() {
        LocalDate today = LocalDate.now();
        LocalDate plannedStart = today.plusDays(1);
        LocalDate plannedEnd = today.plusDays(10);

        ProductionOrder created = ProductionOrder.create(
                UUID.randomUUID(),
                today,
                ProductionPriority.NORMAL,
                null,
                null,
                "Persistence lifecycle verification"
        );
        ProductionOrder persisted = productionOrderRepository.save(created);
        UUID productionOrderId = persisted.getId();

        ProductionOrder reloadedAfterCreate = reload(productionOrderId);
        assertEquals(ProductionStatus.CREATED, reloadedAfterCreate.getStatus());
        assertTrue(reloadedAfterCreate.getOperations().isEmpty());

        AddProductionOperationResult cuttingResult = addProductionOperationUseCase.execute(
                new AddProductionOperationCommand(
                        productionOrderId,
                        ProductionOperationType.CUTTING,
                        plannedStart,
                        plannedEnd,
                        "Cutting operation"
                )
        );
        AddProductionOperationResult sewingResult = addProductionOperationUseCase.execute(
                new AddProductionOperationCommand(
                        productionOrderId,
                        ProductionOperationType.SEWING,
                        plannedStart.plusDays(1),
                        plannedEnd,
                        "Sewing operation"
                )
        );

        UUID cuttingOperationId = cuttingResult.operationId();
        UUID sewingOperationId = sewingResult.operationId();

        ProductionOrder reloadedAfterAdd = reload(productionOrderId);
        assertEquals(2, reloadedAfterAdd.getOperations().size());
        assertEquals(
                Set.of(cuttingOperationId, sewingOperationId),
                operationIds(reloadedAfterAdd)
        );
        assertEquals(ProductionStatus.CREATED, reloadedAfterAdd.getStatus());

        assignProductionOperationOperatorUseCase.execute(
                new AssignProductionOperationOperatorCommand(
                        productionOrderId,
                        cuttingOperationId,
                        "Operator A"
                )
        );
        assignProductionOperationOperatorUseCase.execute(
                new AssignProductionOperationOperatorCommand(
                        productionOrderId,
                        sewingOperationId,
                        "Operator B"
                )
        );

        ProductionOrder reloadedAfterAssign = reload(productionOrderId);
        assertEquals("Operator A", findOperation(reloadedAfterAssign, cuttingOperationId).getAssignedOperator());
        assertEquals("Operator B", findOperation(reloadedAfterAssign, sewingOperationId).getAssignedOperator());
        assertEquals(
                Set.of(cuttingOperationId, sewingOperationId),
                operationIds(reloadedAfterAssign)
        );

        planProductionOrderUseCase.execute(
                new PlanProductionOrderCommand(
                        productionOrderId,
                        plannedStart,
                        plannedEnd,
                        ProductionPriority.HIGH
                )
        );

        ProductionOrder reloadedAfterPlan = reload(productionOrderId);
        assertEquals(ProductionStatus.PLANNED, reloadedAfterPlan.getStatus());
        assertEquals(ProductionPriority.HIGH, reloadedAfterPlan.getPriority());
        assertEquals(plannedStart, reloadedAfterPlan.getPlannedStartDate());
        assertEquals(plannedEnd, reloadedAfterPlan.getPlannedEndDate());
        assertEquals(2, reloadedAfterPlan.getOperations().size());
        assertEquals(
                Set.of(cuttingOperationId, sewingOperationId),
                operationIds(reloadedAfterPlan)
        );

        startProductionOrderUseCase.execute(new StartProductionOrderCommand(productionOrderId));

        ProductionOrder reloadedAfterStartOrder = reload(productionOrderId);
        assertEquals(ProductionStatus.IN_PROGRESS, reloadedAfterStartOrder.getStatus());
        assertEquals(2, reloadedAfterStartOrder.getOperations().size());

        startProductionOperationUseCase.execute(
                new StartProductionOperationCommand(productionOrderId, cuttingOperationId)
        );
        completeProductionOperationUseCase.execute(
                new CompleteProductionOperationCommand(productionOrderId, cuttingOperationId)
        );
        startProductionOperationUseCase.execute(
                new StartProductionOperationCommand(productionOrderId, sewingOperationId)
        );
        completeProductionOperationUseCase.execute(
                new CompleteProductionOperationCommand(productionOrderId, sewingOperationId)
        );

        ProductionOrder reloadedAfterOperations = reload(productionOrderId);
        assertEquals(ProductionStatus.IN_PROGRESS, reloadedAfterOperations.getStatus());
        assertEquals(2, reloadedAfterOperations.getOperations().size());
        assertEquals(
                Set.of(cuttingOperationId, sewingOperationId),
                operationIds(reloadedAfterOperations)
        );
        assertEquals(
                ProductionOperationStatus.COMPLETED,
                findOperation(reloadedAfterOperations, cuttingOperationId).getStatus()
        );
        assertEquals(
                ProductionOperationStatus.COMPLETED,
                findOperation(reloadedAfterOperations, sewingOperationId).getStatus()
        );

        completeProductionOrderUseCase.execute(new CompleteProductionOrderCommand(productionOrderId));

        ProductionOrder finalAggregate = reload(productionOrderId);
        assertEquals(ProductionStatus.COMPLETED, finalAggregate.getStatus());
        assertEquals(2, finalAggregate.getOperations().size());
        assertEquals(
                Set.of(cuttingOperationId, sewingOperationId),
                operationIds(finalAggregate)
        );
        assertTrue(finalAggregate.getOperations().stream()
                .allMatch(operation -> operation.getStatus() == ProductionOperationStatus.COMPLETED));
        assertEquals("Operator A", findOperation(finalAggregate, cuttingOperationId).getAssignedOperator());
        assertEquals("Operator B", findOperation(finalAggregate, sewingOperationId).getAssignedOperator());
    }

    private ProductionOrder reload(UUID productionOrderId) {
        entityManager.flush();
        entityManager.clear();
        return productionOrderRepository.findById(productionOrderId)
                .orElseThrow(() -> new IllegalStateException(
                        "Production order not found after reload: " + productionOrderId
                ));
    }

    private static Set<UUID> operationIds(ProductionOrder productionOrder) {
        return productionOrder.getOperations().stream()
                .map(ProductionOperation::getId)
                .collect(Collectors.toSet());
    }

    private static ProductionOperation findOperation(ProductionOrder productionOrder, UUID operationId) {
        List<ProductionOperation> matches = productionOrder.getOperations().stream()
                .filter(operation -> operation.getId().equals(operationId))
                .toList();
        assertEquals(1, matches.size(), "Expected exactly one operation with id " + operationId);
        return matches.getFirst();
    }
}
