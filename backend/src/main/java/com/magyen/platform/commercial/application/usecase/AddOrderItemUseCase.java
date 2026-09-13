package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CommercialCatalogValidator;
import com.magyen.platform.commercial.application.OrderPaymentFloorGuard;
import com.magyen.platform.commercial.application.dto.AddOrderItemCommand;
import com.magyen.platform.commercial.application.dto.AddOrderItemResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.shared.domain.Money;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrega un OrderItem manual a una Orden comercial editable.
 * <p>
 * El ítem nuevo no tiene quotationItemId. No sincroniza cotización ni producción.
 */
public class AddOrderItemUseCase {

    private final OrderRepository orderRepository;
    private final CommercialCatalogValidator commercialCatalogValidator;
    private final OrderPaymentFloorGuard orderPaymentFloorGuard;

    public AddOrderItemUseCase(
            OrderRepository orderRepository,
            CommercialCatalogValidator commercialCatalogValidator,
            OrderPaymentFloorGuard orderPaymentFloorGuard
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.commercialCatalogValidator = Objects.requireNonNull(
                commercialCatalogValidator,
                "Commercial catalog validator must not be null"
        );
        this.orderPaymentFloorGuard = Objects.requireNonNull(
                orderPaymentFloorGuard,
                "Order payment floor guard must not be null"
        );
    }

    @Transactional
    public AddOrderItemResult execute(AddOrderItemCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.orderId(), "Order id must not be null");

        Order order = requireOrder(command.orderId());
        String fabric = commercialCatalogValidator.requirePrimaryFabric(command.fabric());
        String secondaryFabric = commercialCatalogValidator.requireSecondaryFabric(command.secondaryFabric());

        order.addItem(
                command.productName(),
                command.quantity(),
                fabric,
                secondaryFabric,
                command.color(),
                Money.of(command.unitPrice()),
                commercialCatalogValidator.requireProductSpecification(command.productSpecification())
        );
        orderPaymentFloorGuard.ensureTotalCoversAmountPaid(order);

        UUID itemId = lastCreatedItemId(order);
        Order saved = orderRepository.save(order);

        return new AddOrderItemResult(
                saved.getId(),
                itemId,
                saved.getSubtotal().getAmount(),
                saved.getDiscount().getAmount(),
                saved.getTotal().getAmount()
        );
    }

    private Order requireOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    private static UUID lastCreatedItemId(Order order) {
        List<OrderItem> items = order.getItems();
        return items.get(items.size() - 1).getId();
    }
}
