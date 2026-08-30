package com.magyen.platform.production.application.usecase;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionAdditionalCostCommand;
import com.magyen.platform.production.application.dto.RegisterProductionAdditionalCostResult;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.domain.ProductionDirectCostCategory;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.shared.domain.Money;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProductionAdditionalCostUseCaseTest {

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private RegisterProductionAdditionalCostUseCase registerProductionAdditionalCostUseCase;

    @Autowired
    private GetProductionOrderUseCase getProductionOrderUseCase;

    @Autowired
    private GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void registersOtherCostOnceAndImpactsProfitabilityWithoutDoubleAccounting() {
        Order order = orderRepository.save(Order.create(
                OrderNumber.of("9001"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                DeliveryCommitment.of(LocalDate.of(2026, 8, 20)),
                UUID.randomUUID(),
                null,
                "Pedido con envío",
                List.of(OrderItem.reconstitute(
                        UUID.randomUUID(),
                        "Camiseta",
                        10,
                        "Algodón",
                        "Blanco",
                        Money.of(new BigDecimal("50000.00")),
                        com.magyen.platform.commercial.domain.ProductSpecification.empty(),
                        List.of()
                ))
        ));

        ProductionOrder productionOrder = productionOrderRepository.save(ProductionOrder.create(
                order.getId(),
                LocalDate.of(2026, 8, 2),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        ));
        UUID productionOrderId = productionOrder.getId();

        assertThrows(ProductionDomainException.class, () -> register(
                productionOrderId,
                "Envío de uniformes a Cartagena",
                "80000.00"
        ));

        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                productionOrderId,
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 10),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(
                productionOrderId,
                LocalDate.of(2026, 8, 3)
        ));

        assertThrows(ProductionDomainException.class, () -> register(productionOrderId, "  ", "80000.00"));
        assertThrows(ProductionDomainException.class, () -> register(productionOrderId, "Medias", "0.00"));

        long expensesBefore = countProductionExpenses();
        RegisterProductionAdditionalCostResult created = register(
                productionOrderId,
                "Envío de uniformes a Cartagena",
                "80000.00"
        );

        assertEquals(ProductionDirectCostCategory.OTHER, created.category());
        assertEquals("Envío de uniformes a Cartagena", created.description());
        assertEquals(new BigDecimal("80000.00"), created.amount());
        assertNotNull(created.financialTransactionId());
        assertEquals(expensesBefore + 1, countProductionExpenses());

        FinancialTransaction expense = financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.PRODUCTION, created.additionalCostId())
                .orElseThrow();
        assertEquals(FinancialTransactionType.EXPENSE, expense.getType());
        assertEquals("OTHER_EXPENSE", expense.getCategory());
        assertEquals("Envío de uniformes a Cartagena", expense.getDescription());
        assertEquals(new BigDecimal("80000.00"), expense.getAmount().getValue());

        assertThrows(
                com.magyen.platform.finance.domain.exception.FinanceDomainException.class,
                () -> financialTransactionRepository.findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.PRODUCTION,
                        created.additionalCostId()
                ).ifPresent(existing -> {
                    throw new com.magyen.platform.finance.domain.exception.FinanceDomainException(
                            "A PRODUCTION financial transaction already exists for this additional cost"
                    );
                })
        );

        GetProductionOrderResult detail = getProductionOrderUseCase.execute(
                new GetProductionOrderCommand(productionOrderId)
        );
        assertEquals(1, detail.otherCostSummary().otherCostCount());
        assertEquals(new BigDecimal("80000.00"), detail.otherCostSummary().totalOtherCost());
        assertEquals(new BigDecimal("80000.00"), detail.totalProductionCost());
        assertEquals("Envío de uniformes a Cartagena", detail.additionalCosts().getFirst().description());

        GetOrderProfitabilityResult profitability = getOrderProfitabilityUseCase.execute(
                new GetOrderProfitabilityQuery(order.getId())
        );
        assertEquals(new BigDecimal("80000.00"), profitability.otherDirectCost());
        assertEquals(new BigDecimal("80000.00"), profitability.totalDirectCost());
        assertEquals(new BigDecimal("420000.00"), profitability.directProfit());
        assertEquals(expensesBefore + 1, countProductionExpenses());
        assertTrue(profitability.totalDirectCost().compareTo(profitability.otherDirectCost()) == 0);
    }

    private RegisterProductionAdditionalCostResult register(
            UUID productionOrderId,
            String description,
            String amount
    ) {
        return registerProductionAdditionalCostUseCase.execute(new RegisterProductionAdditionalCostCommand(
                productionOrderId,
                "OTHER",
                description,
                new BigDecimal(amount),
                LocalDate.of(2026, 8, 15)
        ));
    }

    private long countProductionExpenses() {
        return financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getSourceType() == FinancialTransactionSourceType.PRODUCTION)
                .count();
    }
}
