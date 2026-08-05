package com.magyen.platform.production.application.usecase;

import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Caso de uso que coordina la creación de una Orden de Producción a partir de una Orden comercial.
 * <p>
 * La referencia a la Orden comercial se mantiene únicamente por identidad.
 * No modifica el estado de la Orden comercial.
 */
public class CreateProductionOrderFromOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductionOrderRepository productionOrderRepository;

    public CreateProductionOrderFromOrderUseCase(
            OrderRepository orderRepository,
            ProductionOrderRepository productionOrderRepository
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public CreateProductionOrderResult execute(CreateProductionOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + command.orderId()
                ));

        productionOrderRepository.findByOrderId(order.getId()).ifPresent(existing -> {
            throw new ProductionDomainException(
                    "A production order already exists for order: " + order.getId()
            );
        });

        LocalDate creationDate = LocalDate.now();

        ProductionOrder productionOrder = ProductionOrder.create(
                order.getId(),
                creationDate,
                command.priority(),
                command.plannedStartDate(),
                command.plannedEndDate(),
                command.observations()
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
}
