package com.magyen.platform.plotter.infrastructure.commercial;

import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador Plotter → Commercial para validar atribución y enriquecer identidad.
 */
public class PlotterCommercialOrderAdapter implements PlotterCommercialOrderPort {

    private final GetOrderUseCase getOrderUseCase;
    private final GetCustomersUseCase getCustomersUseCase;

    public PlotterCommercialOrderAdapter(
            GetOrderUseCase getOrderUseCase,
            GetCustomersUseCase getCustomersUseCase
    ) {
        this.getOrderUseCase = Objects.requireNonNull(getOrderUseCase, "Get order use case must not be null");
        this.getCustomersUseCase = Objects.requireNonNull(
                getCustomersUseCase,
                "Get customers use case must not be null"
        );
    }

    @Override
    public PlotterCommercialOrderView requireExistingOrder(UUID orderId) {
        return findOrder(orderId).orElseThrow(() ->
                new PlotterDomainException("Commercial order not found: " + orderId)
        );
    }

    @Override
    public Optional<PlotterCommercialOrderView> findOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");

        GetOrderResult order;
        try {
            order = getOrderUseCase.execute(new GetOrderCommand(orderId));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        return Optional.of(toView(order));
    }

    @Override
    public Optional<String> findCustomerName(UUID customerId) {
        Objects.requireNonNull(customerId, "Customer id must not be null");
        return getCustomersUseCase.execute().customers().stream()
                .filter(customer -> customerId.equals(customer.customerId()))
                .map(customer -> customer.name())
                .findFirst();
    }

    private static PlotterCommercialOrderView toView(GetOrderResult order) {
        LocalDate deliveryDate = order.deliveryCommitment() == null
                ? null
                : order.deliveryCommitment().promisedDeliveryDate();
        return new PlotterCommercialOrderView(
                order.orderId(),
                order.orderNumber(),
                order.description(),
                order.customerId(),
                order.customerName(),
                order.confirmationDate(),
                deliveryDate
        );
    }
}
