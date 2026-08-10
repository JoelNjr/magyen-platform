package com.magyen.platform.intelligence.application.usecase;

import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.intelligence.application.dto.GetNotificationsResult;
import com.magyen.platform.intelligence.application.dto.NotificationResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso que consolida notificaciones operativas a partir del estado actual del sistema.
 * <p>
 * Solo consulta información existente en otros módulos; no modifica el estado del negocio
 * ni persiste historial de notificaciones.
 */
public class GetNotificationsUseCase {

    private static final int NEAR_DELIVERY_DAYS = 3;

    private static final String SEVERITY_WARNING = "WARNING";
    private static final String SEVERITY_CRITICAL = "CRITICAL";

    private static final String TYPE_LOW_STOCK = "LOW_STOCK";
    private static final String TYPE_NEAR_DELIVERY = "NEAR_DELIVERY";
    private static final String TYPE_DELAYED_PRODUCTION = "DELAYED_PRODUCTION";
    private static final String TYPE_PENDING_BALANCE = "PENDING_BALANCE";

    private static final String MODULE_INVENTORY = "INVENTORY";
    private static final String MODULE_COMMERCIAL = "COMMERCIAL";
    private static final String MODULE_PRODUCTION = "PRODUCTION";
    private static final String MODULE_FINANCE = "FINANCE";

    private final OrderRepository orderRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final PaymentRepository paymentRepository;

    public GetNotificationsUseCase(
            OrderRepository orderRepository,
            ProductionOrderRepository productionOrderRepository,
            InventoryItemRepository inventoryItemRepository,
            PaymentRepository paymentRepository
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "Payment repository must not be null");
    }

    public GetNotificationsResult execute() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        List<NotificationResult> notifications = new ArrayList<>();
        notifications.addAll(detectLowStockNotifications(createdAt));
        notifications.addAll(detectNearDeliveryNotifications(today, createdAt));
        notifications.addAll(detectDelayedProductionNotifications(today, createdAt));
        notifications.addAll(detectPendingBalanceNotifications(createdAt));

        return new GetNotificationsResult(List.copyOf(notifications));
    }

    private List<NotificationResult> detectLowStockNotifications(LocalDateTime createdAt) {
        return inventoryItemRepository.findAll().stream()
                .filter(item -> item.getMinimumStock() != null
                        && item.getStock().compareTo(item.getMinimumStock()) < 0)
                .map(item -> toLowStockNotification(item, createdAt))
                .toList();
    }

    private NotificationResult toLowStockNotification(InventoryItem inventoryItem, LocalDateTime createdAt) {
        UUID materialId = inventoryItem.getId();
        return new NotificationResult(
                "INV-" + materialId,
                TYPE_LOW_STOCK,
                "Low stock",
                "Material " + inventoryItem.getName() + " is below minimum stock.",
                SEVERITY_CRITICAL,
                createdAt,
                materialId.toString(),
                MODULE_INVENTORY
        );
    }

    private List<NotificationResult> detectNearDeliveryNotifications(LocalDate today, LocalDateTime createdAt) {
        LocalDate nearDeliveryLimit = today.plusDays(NEAR_DELIVERY_DAYS);

        return orderRepository.findAll().stream()
                .filter(order -> {
                    LocalDate promisedDeliveryDate = order.getDeliveryCommitment().getPromisedDeliveryDate();
                    return !promisedDeliveryDate.isBefore(today)
                            && !promisedDeliveryDate.isAfter(nearDeliveryLimit);
                })
                .map(order -> toNearDeliveryNotification(order, createdAt))
                .toList();
    }

    private NotificationResult toNearDeliveryNotification(Order order, LocalDateTime createdAt) {
        UUID orderId = order.getId();
        return new NotificationResult(
                "ORD-" + orderId,
                TYPE_NEAR_DELIVERY,
                "Delivery approaching",
                "Order " + order.getOrderNumber().getValue()
                        + " has a promised delivery date within the next "
                        + NEAR_DELIVERY_DAYS
                        + " days.",
                SEVERITY_WARNING,
                createdAt,
                orderId.toString(),
                MODULE_COMMERCIAL
        );
    }

    private List<NotificationResult> detectDelayedProductionNotifications(LocalDate today, LocalDateTime createdAt) {
        return productionOrderRepository.findAll().stream()
                .filter(productionOrder -> {
                    LocalDate plannedEndDate = productionOrder.getPlannedEndDate();
                    return plannedEndDate != null
                            && plannedEndDate.isBefore(today)
                            && productionOrder.getStatus() != ProductionStatus.COMPLETED;
                })
                .map(productionOrder -> toDelayedProductionNotification(productionOrder, createdAt))
                .toList();
    }

    private NotificationResult toDelayedProductionNotification(
            ProductionOrder productionOrder,
            LocalDateTime createdAt
    ) {
        UUID productionOrderId = productionOrder.getId();
        return new NotificationResult(
                "PROD-" + productionOrderId,
                TYPE_DELAYED_PRODUCTION,
                "Production delayed",
                "Production order " + productionOrderId + " is delayed against its planned end date.",
                SEVERITY_CRITICAL,
                createdAt,
                productionOrderId.toString(),
                MODULE_PRODUCTION
        );
    }

    private List<NotificationResult> detectPendingBalanceNotifications(LocalDateTime createdAt) {
        List<Order> orders = orderRepository.findAll();
        Map<UUID, BigDecimal> totalPaidByOrderId = paymentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Payment::getOrderId,
                        Collectors.mapping(
                                payment -> payment.getAmount().getValue(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        List<NotificationResult> notifications = new ArrayList<>();
        for (Order order : orders) {
            BigDecimal totalPaid = totalPaidByOrderId.getOrDefault(order.getId(), BigDecimal.ZERO);
            BigDecimal remainingBalance = order.getTotal().getAmount().subtract(totalPaid);
            if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
                notifications.add(toPendingBalanceNotification(order, createdAt));
            }
        }
        return notifications;
    }

    private NotificationResult toPendingBalanceNotification(Order order, LocalDateTime createdAt) {
        UUID orderId = order.getId();
        return new NotificationResult(
                "FIN-" + orderId,
                TYPE_PENDING_BALANCE,
                "Pending balance",
                "Order " + order.getOrderNumber().getValue() + " has a pending balance to collect.",
                SEVERITY_WARNING,
                createdAt,
                orderId.toString(),
                MODULE_FINANCE
        );
    }
}
