package com.magyen.platform.plotter.infrastructure.commercial;

import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador Plotter → Commercial para validar atribución y enriquecer identidad.
 */
public class PlotterCommercialOrderAdapter implements PlotterCommercialOrderPort {

    private final GetOrderUseCase getOrderUseCase;
    private final GetOrdersUseCase getOrdersUseCase;
    private final GetCustomersUseCase getCustomersUseCase;

    public PlotterCommercialOrderAdapter(
            GetOrderUseCase getOrderUseCase,
            GetOrdersUseCase getOrdersUseCase,
            GetCustomersUseCase getCustomersUseCase
    ) {
        this.getOrderUseCase = Objects.requireNonNull(getOrderUseCase, "Get order use case must not be null");
        this.getOrdersUseCase = Objects.requireNonNull(getOrdersUseCase, "Get orders use case must not be null");
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
    public PlotterCommercialOrderView requireOrderOpenForPlotterJob(UUID orderId) {
        PlotterCommercialOrderView order = requireExistingOrder(orderId);
        if (!order.openForPlotterJob()) {
            throw new PlotterDomainException(
                    "Internal Magyen plotter jobs require an open commercial order. Current status: "
                            + order.status()
            );
        }
        return order;
    }

    @Override
    public List<PlotterCommercialOrderView> findOrdersOpenForPlotterJob() {
        return getOrdersUseCase.execute().orders().stream()
                .filter(order -> order.status() != null && order.status().allowsCommercialContentEditing())
                .map(PlotterCommercialOrderAdapter::toListView)
                .toList();
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

        return Optional.of(toDetailView(order));
    }

    @Override
    public Optional<String> findCustomerName(UUID customerId) {
        Objects.requireNonNull(customerId, "Customer id must not be null");
        return getCustomersUseCase.execute().customers().stream()
                .filter(customer -> customerId.equals(customer.customerId()))
                .map(customer -> customer.name())
                .findFirst();
    }

    @Override
    public void requireExternalPlotterCustomer(UUID customerId) {
        Objects.requireNonNull(customerId, "Customer id must not be null");
        var customer = getCustomersUseCase.execute().customers().stream()
                .filter(candidate -> customerId.equals(candidate.customerId()))
                .findFirst()
                .orElseThrow(() -> new PlotterDomainException(
                        "External plotter jobs require a Plotter customer"
                ));
        if (customer.category() == null || !customer.category().allowsExternalPlotterJob()) {
            throw new PlotterDomainException(
                    "External plotter jobs require a Plotter customer"
            );
        }
    }

    private static PlotterCommercialOrderView toDetailView(GetOrderResult order) {
        LocalDate deliveryDate = order.deliveryCommitment() == null
                ? null
                : order.deliveryCommitment().promisedDeliveryDate();
        return toView(
                order.orderId(),
                order.orderNumber(),
                order.description(),
                order.customerId(),
                order.customerName(),
                order.confirmationDate(),
                deliveryDate,
                order.status()
        );
    }

    private static PlotterCommercialOrderView toListView(OrderResult order) {
        return toView(
                order.orderId(),
                order.orderNumber(),
                order.description(),
                order.customerId(),
                order.customerName(),
                order.confirmationDate(),
                order.promisedDeliveryDate(),
                order.status()
        );
    }

    private static PlotterCommercialOrderView toView(
            UUID orderId,
            String orderNumber,
            String description,
            UUID customerId,
            String customerName,
            LocalDate confirmationDate,
            LocalDate deliveryDate,
            OrderStatus status
    ) {
        boolean openForPlotterJob = status != null && status.allowsCommercialContentEditing();
        return new PlotterCommercialOrderView(
                orderId,
                orderNumber,
                description,
                customerId,
                customerName,
                confirmationDate,
                deliveryDate,
                status == null ? null : status.name(),
                openForPlotterJob
        );
    }
}
