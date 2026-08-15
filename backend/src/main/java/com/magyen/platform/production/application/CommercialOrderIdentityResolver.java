package com.magyen.platform.production.application;

import com.magyen.platform.commercial.application.dto.CustomerResult;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Resuelve identificadores comerciales legibles para lecturas de Producción.
 * <p>
 * Reutiliza {@link GetOrdersUseCase} y {@link GetCustomersUseCase}.
 * No accede a repositorios JPA de Commercial ni inventa nombres.
 */
public class CommercialOrderIdentityResolver {

    private final GetOrdersUseCase getOrdersUseCase;
    private final GetCustomersUseCase getCustomersUseCase;

    public CommercialOrderIdentityResolver(
            GetOrdersUseCase getOrdersUseCase,
            GetCustomersUseCase getCustomersUseCase
    ) {
        this.getOrdersUseCase = Objects.requireNonNull(getOrdersUseCase, "Get orders use case must not be null");
        this.getCustomersUseCase = Objects.requireNonNull(
                getCustomersUseCase,
                "Get customers use case must not be null"
        );
    }

    public Map<UUID, CommercialOrderIdentity> resolveAll() {
        Map<UUID, String> customerNameById = customerNameById();
        Map<UUID, CommercialOrderIdentity> identityByOrderId = new HashMap<>();

        for (OrderResult order : getOrdersUseCase.execute().orders()) {
            if (order == null || order.orderId() == null) {
                continue;
            }
            identityByOrderId.put(order.orderId(), toIdentity(order, customerNameById));
        }

        return identityByOrderId;
    }

    public CommercialOrderIdentity resolve(UUID orderId) {
        if (orderId == null) {
            return CommercialOrderIdentity.missing(null);
        }
        return resolveAll().getOrDefault(orderId, CommercialOrderIdentity.missing(orderId));
    }

    private Map<UUID, String> customerNameById() {
        Map<UUID, String> customerNameById = new HashMap<>();
        for (CustomerResult customer : getCustomersUseCase.execute().customers()) {
            if (customer == null || customer.customerId() == null) {
                continue;
            }
            String name = customer.name();
            if (name == null || name.isBlank()) {
                continue;
            }
            customerNameById.putIfAbsent(customer.customerId(), name.trim());
        }
        return customerNameById;
    }

    private static CommercialOrderIdentity toIdentity(
            OrderResult order,
            Map<UUID, String> customerNameById
    ) {
        String orderNumber = order.orderNumber() == null || order.orderNumber().isBlank()
                ? null
                : order.orderNumber().trim();
        return new CommercialOrderIdentity(
                order.orderId(),
                orderNumber,
                order.customerId(),
                customerNameById.get(order.customerId())
        );
    }

    /**
     * Identidad comercial de presentación. Los UUID internos permanecen disponibles.
     */
    public record CommercialOrderIdentity(
            UUID orderId,
            String orderNumber,
            UUID customerId,
            String customerName
    ) {

        public static CommercialOrderIdentity missing(UUID orderId) {
            return new CommercialOrderIdentity(orderId, null, null, null);
        }
    }
}
