package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.CommercialOrderIdentityResolver;
import com.magyen.platform.production.application.CommercialOrderIdentityResolver.CommercialOrderIdentity;
import com.magyen.platform.production.application.dto.GetProductionOrdersQuery;
import com.magyen.platform.production.application.dto.GetProductionOrdersResult;
import com.magyen.platform.production.application.dto.ProductionOrderResult;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que consulta las Órdenes de Producción existentes.
 */
public class GetProductionOrdersUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final CommercialOrderIdentityResolver commercialOrderIdentityResolver;

    public GetProductionOrdersUseCase(
            ProductionOrderRepository productionOrderRepository,
            CommercialOrderIdentityResolver commercialOrderIdentityResolver
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.commercialOrderIdentityResolver = Objects.requireNonNull(
                commercialOrderIdentityResolver,
                "Commercial order identity resolver must not be null"
        );
    }

    public GetProductionOrdersResult execute() {
        return execute(new GetProductionOrdersQuery(null, null));
    }

    public GetProductionOrdersResult execute(GetProductionOrdersQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        validateRange(query.fromDate(), query.toDate());

        Map<UUID, CommercialOrderIdentity> commercialIdentityByOrderId =
                commercialOrderIdentityResolver.resolveAll();

        List<ProductionOrderResult> productionOrders = productionOrderRepository.findAll().stream()
                .filter(productionOrder -> inRange(
                        productionOrder.getCreationDate(),
                        query.fromDate(),
                        query.toDate()
                ))
                .map(productionOrder -> toProductionOrderResult(
                        productionOrder,
                        commercialIdentityByOrderId.getOrDefault(
                                productionOrder.getOrderId(),
                                CommercialOrderIdentity.missing(productionOrder.getOrderId())
                        )
                ))
                .toList();

        return new GetProductionOrdersResult(productionOrders);
    }

    private static void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return;
        }
        if (fromDate == null || toDate == null) {
            throw new ProductionDomainException("Both fromDate and toDate must be provided together");
        }
        if (fromDate.isAfter(toDate)) {
            throw new ProductionDomainException("From date must not be after to date");
        }
    }

    private static boolean inRange(LocalDate businessDate, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            return true;
        }
        return businessDate != null && !businessDate.isBefore(fromDate) && !businessDate.isAfter(toDate);
    }

    private ProductionOrderResult toProductionOrderResult(
            ProductionOrder productionOrder,
            CommercialOrderIdentity commercialIdentity
    ) {
        return new ProductionOrderResult(
                productionOrder.getId(),
                productionOrder.getOrderId(),
                commercialIdentity.orderNumber(),
                commercialIdentity.orderDescription(),
                commercialIdentity.customerId(),
                commercialIdentity.customerName(),
                productionOrder.getCreationDate(),
                productionOrder.getStatus(),
                productionOrder.getPriority(),
                productionOrder.getPlannedStartDate(),
                productionOrder.getPlannedEndDate(),
                productionOrder.getActualStartDate(),
                productionOrder.getActualCompletionDate(),
                productionOrder.getObservations()
        );
    }
}
