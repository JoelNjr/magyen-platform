package com.magyen.platform.home.infrastructure.production;

import com.magyen.platform.home.application.port.ProductionDashboardPort;
import com.magyen.platform.production.application.dto.ProductionOrderResult;
import com.magyen.platform.production.application.usecase.GetProductionOrdersUseCase;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Adaptador Home → Production para el resumen operativo de Production Orders.
 * <p>
 * Reutiliza {@link GetProductionOrdersUseCase}. No accede a JPA ni recalcula costos.
 * <p>
 * {@code items} solo incluye CREATED / PLANNED / IN_PROGRESS.
 * Orden: IN_PROGRESS → PLANNED → CREATED, luego prioridad (URGENT→LOW),
 * creationDate ASC, productionOrderId.
 */
public class ProductionDashboardAdapter implements ProductionDashboardPort {

    private static final Set<ProductionStatus> ACTIVE_STATUSES = EnumSet.of(
            ProductionStatus.CREATED,
            ProductionStatus.PLANNED,
            ProductionStatus.IN_PROGRESS
    );

    private static final Comparator<ProductionOrderResult> ACTIVE_ORDER = Comparator
            .comparingInt((ProductionOrderResult item) -> statusRank(item.status()))
            .thenComparingInt(item -> priorityRank(item.priority()))
            .thenComparing(ProductionOrderResult::creationDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(item -> item.productionOrderId().toString());

    private final GetProductionOrdersUseCase getProductionOrdersUseCase;

    public ProductionDashboardAdapter(GetProductionOrdersUseCase getProductionOrdersUseCase) {
        this.getProductionOrdersUseCase = Objects.requireNonNull(
                getProductionOrdersUseCase,
                "Get production orders use case must not be null"
        );
    }

    @Override
    public HomeProductionSummarySnapshot getCurrentProductionSummary() {
        List<ProductionOrderResult> all = getProductionOrdersUseCase.execute().productionOrders();

        int created = 0;
        int planned = 0;
        int inProgress = 0;
        int completed = 0;
        for (ProductionOrderResult order : all) {
            if (order.status() == ProductionStatus.CREATED) {
                created++;
            } else if (order.status() == ProductionStatus.PLANNED) {
                planned++;
            } else if (order.status() == ProductionStatus.IN_PROGRESS) {
                inProgress++;
            } else if (order.status() == ProductionStatus.COMPLETED) {
                completed++;
            }
        }

        List<ProductionDashboardItem> activeItems = all.stream()
                .filter(order -> ACTIVE_STATUSES.contains(order.status()))
                .sorted(ACTIVE_ORDER)
                .map(this::toItem)
                .toList();

        return new HomeProductionSummarySnapshot(
                all.size(),
                created,
                planned,
                inProgress,
                completed,
                activeItems
        );
    }

    private ProductionDashboardItem toItem(ProductionOrderResult order) {
        return new ProductionDashboardItem(
                order.productionOrderId(),
                order.orderId(),
                order.orderNumber(),
                order.customerId(),
                order.customerName(),
                order.status() == null ? null : order.status().name(),
                order.creationDate(),
                order.priority() == null ? null : order.priority().name()
        );
    }

    private static int statusRank(ProductionStatus status) {
        if (status == ProductionStatus.IN_PROGRESS) {
            return 0;
        }
        if (status == ProductionStatus.PLANNED) {
            return 1;
        }
        if (status == ProductionStatus.CREATED) {
            return 2;
        }
        return 99;
    }

    private static int priorityRank(ProductionPriority priority) {
        if (priority == null) {
            return 99;
        }
        return switch (priority) {
            case URGENT -> 0;
            case HIGH -> 1;
            case NORMAL -> 2;
            case LOW -> 3;
        };
    }
}
