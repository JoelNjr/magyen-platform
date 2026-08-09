package com.magyen.platform.production.application.usecase;

import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.production.application.ProductionSnapshotFactory;
import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.production.domain.ProductionItem;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.production.domain.exception.ProductionOrderAlreadyExistsException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que coordina la creación de una Orden de Producción a partir de una Orden comercial.
 * <p>
 * Consulta Comercial mediante su contrato de aplicación, captura un snapshot productivo independiente
 * y persiste la Orden de Producción. No modifica el estado de la Orden comercial.
 */
public class CreateProductionOrderFromOrderUseCase {

    private final GetOrderUseCase getOrderUseCase;
    private final ProductionSnapshotFactory productionSnapshotFactory;
    private final ProductionOrderRepository productionOrderRepository;

    public CreateProductionOrderFromOrderUseCase(
            GetOrderUseCase getOrderUseCase,
            ProductionSnapshotFactory productionSnapshotFactory,
            ProductionOrderRepository productionOrderRepository
    ) {
        this.getOrderUseCase = Objects.requireNonNull(getOrderUseCase, "Get order use case must not be null");
        this.productionSnapshotFactory = Objects.requireNonNull(
                productionSnapshotFactory,
                "Production snapshot factory must not be null"
        );
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public CreateProductionOrderResult execute(CreateProductionOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        GetOrderResult order = getOrderUseCase.execute(new GetOrderCommand(command.orderId()));
        validateOrderEligibility(order);

        productionOrderRepository.findByOrderId(order.orderId()).ifPresent(existing -> {
            throw new ProductionOrderAlreadyExistsException();
        });

        List<ProductionItem> snapshotItems = productionSnapshotFactory.captureFrom(order);

        ProductionOrder productionOrder = ProductionOrder.create(
                order.orderId(),
                LocalDate.now(),
                command.priority(),
                command.plannedStartDate(),
                command.plannedEndDate(),
                command.observations(),
                snapshotItems
        );

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return new CreateProductionOrderResult(
                savedProductionOrder.getId(),
                savedProductionOrder.getOrderId(),
                savedProductionOrder.getStatus(),
                savedProductionOrder.getPriority(),
                savedProductionOrder.getCreationDate()
        );
    }

    private void validateCommand(CreateProductionOrderCommand command) {
        Objects.requireNonNull(command.orderId(), "Order id must not be null");
        Objects.requireNonNull(command.priority(), "Priority must not be null");
    }

    private void validateOrderEligibility(GetOrderResult order) {
        if (order.status() != OrderStatus.CONFIRMED) {
            throw new ProductionDomainException(
                    "A production order can only be created from a CONFIRMED commercial order. Current status: "
                            + order.status()
            );
        }

        if (order.items() == null || order.items().isEmpty()) {
            throw new ProductionDomainException(
                    "A production order requires at least one commercial order item. Order: " + order.orderId()
            );
        }
    }
}
