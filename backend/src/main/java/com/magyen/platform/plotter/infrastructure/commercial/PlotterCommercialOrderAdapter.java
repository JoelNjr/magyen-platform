package com.magyen.platform.plotter.infrastructure.commercial;

import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Plotter → Commercial para validar atribución de un trabajo a una orden.
 */
public class PlotterCommercialOrderAdapter implements PlotterCommercialOrderPort {

    private final GetOrderUseCase getOrderUseCase;

    public PlotterCommercialOrderAdapter(GetOrderUseCase getOrderUseCase) {
        this.getOrderUseCase = Objects.requireNonNull(getOrderUseCase, "Get order use case must not be null");
    }

    @Override
    public PlotterCommercialOrderView requireExistingOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");

        GetOrderResult order;
        try {
            order = getOrderUseCase.execute(new GetOrderCommand(orderId));
        } catch (IllegalArgumentException exception) {
            throw new PlotterDomainException("Commercial order not found: " + orderId);
        }

        return new PlotterCommercialOrderView(
                order.orderId(),
                order.orderNumber(),
                order.customerId(),
                order.confirmationDate(),
                order.deliveryCommitment().promisedDeliveryDate()
        );
    }
}
