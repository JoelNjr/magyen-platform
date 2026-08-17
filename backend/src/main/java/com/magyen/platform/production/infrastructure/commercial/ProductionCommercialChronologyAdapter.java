package com.magyen.platform.production.infrastructure.commercial;

import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.dto.GetQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetQuotationResult;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.commercial.application.usecase.GetQuotationUseCase;
import com.magyen.platform.production.application.port.CommercialOrderChronology;
import com.magyen.platform.production.application.port.ProductionCommercialChronologyPort;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador Production → Commercial para leer la cronología de una orden.
 */
public class ProductionCommercialChronologyAdapter implements ProductionCommercialChronologyPort {

    private final GetOrderUseCase getOrderUseCase;
    private final GetQuotationUseCase getQuotationUseCase;

    public ProductionCommercialChronologyAdapter(
            GetOrderUseCase getOrderUseCase,
            GetQuotationUseCase getQuotationUseCase
    ) {
        this.getOrderUseCase = Objects.requireNonNull(getOrderUseCase, "Get order use case must not be null");
        this.getQuotationUseCase = Objects.requireNonNull(
                getQuotationUseCase,
                "Get quotation use case must not be null"
        );
    }

    @Override
    public Optional<CommercialOrderChronology> findChronology(UUID commercialOrderId) {
        Objects.requireNonNull(commercialOrderId, "Commercial order id must not be null");

        GetOrderResult order;
        try {
            order = getOrderUseCase.execute(new GetOrderCommand(commercialOrderId));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        GetQuotationResult quotation;
        try {
            quotation = getQuotationUseCase.execute(new GetQuotationCommand(order.quotationId()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        return Optional.of(new CommercialOrderChronology(
                order.orderId(),
                quotation.creationDate(),
                order.confirmationDate(),
                order.deliveryCommitment().promisedDeliveryDate()
        ));
    }
}
