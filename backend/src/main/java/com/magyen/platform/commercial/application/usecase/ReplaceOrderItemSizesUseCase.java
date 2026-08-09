package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.ReplaceOrderItemSizesCommand;
import com.magyen.platform.commercial.application.dto.ReplaceOrderItemSizesResult;
import com.magyen.platform.commercial.application.dto.SizeBreakdownCommand;
import com.magyen.platform.commercial.application.dto.SizeBreakdownResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.SizeBreakdown;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que reemplaza la distribución completa de tallas de un OrderItem.
 */
public class ReplaceOrderItemSizesUseCase {

    private final OrderRepository orderRepository;

    public ReplaceOrderItemSizesUseCase(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
    }

    public ReplaceOrderItemSizesResult execute(ReplaceOrderItemSizesCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + command.orderId()
                ));

        OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getId().equals(command.orderItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order item not found in order: " + command.orderItemId()
                ));

        List<SizeBreakdown> sizeBreakdowns = command.sizes().stream()
                .map(this::toSizeBreakdown)
                .toList();

        orderItem.replaceSizeBreakdowns(sizeBreakdowns);

        Order savedOrder = orderRepository.save(order);

        OrderItem savedItem = savedOrder.getItems().stream()
                .filter(item -> item.getId().equals(command.orderItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Order item missing after save: " + command.orderItemId()
                ));

        return new ReplaceOrderItemSizesResult(
                savedItem.getId(),
                savedItem.getSizeBreakdowns().stream()
                        .map(this::toSizeBreakdownResult)
                        .toList()
        );
    }

    private void validateCommand(ReplaceOrderItemSizesCommand command) {
        Objects.requireNonNull(command.orderId(), "Order id must not be null");
        Objects.requireNonNull(command.orderItemId(), "Order item id must not be null");
        Objects.requireNonNull(command.sizes(), "Sizes must not be null");
    }

    private SizeBreakdown toSizeBreakdown(SizeBreakdownCommand command) {
        Objects.requireNonNull(command, "Size breakdown command must not be null");
        return SizeBreakdown.create(command.size(), command.quantity());
    }

    private SizeBreakdownResult toSizeBreakdownResult(SizeBreakdown sizeBreakdown) {
        return new SizeBreakdownResult(
                sizeBreakdown.getSize(),
                sizeBreakdown.getQuantity()
        );
    }
}
