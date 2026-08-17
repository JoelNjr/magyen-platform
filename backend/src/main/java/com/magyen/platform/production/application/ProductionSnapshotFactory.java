package com.magyen.platform.production.application;

import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.dto.OrderItemResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationResult;
import com.magyen.platform.commercial.application.dto.SizeBreakdownResult;
import com.magyen.platform.production.domain.ProductSpecification;
import com.magyen.platform.production.domain.ProductionItem;
import com.magyen.platform.production.domain.SizeBreakdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Construye el snapshot productivo a partir del contrato de lectura comercial.
 * <p>
 * La captura es de solo lectura respecto a Comercial: no muta la Orden ni sus ítems.
 * El resultado pertenece exclusivamente al dominio de Producción.
 */
public class ProductionSnapshotFactory {

    /**
     * Captura los ítems productivos relevantes de una Orden comercial consultada.
     */
    public List<ProductionItem> captureFrom(GetOrderResult order) {
        Objects.requireNonNull(order, "Order result must not be null");
        Objects.requireNonNull(order.items(), "Order items must not be null");

        List<ProductionItem> productionItems = new ArrayList<>();
        for (OrderItemResult orderItem : order.items()) {
            productionItems.add(captureItem(orderItem));
        }
        return List.copyOf(productionItems);
    }

    private ProductionItem captureItem(OrderItemResult orderItem) {
        Objects.requireNonNull(orderItem, "Order item result must not be null");

        return ProductionItem.create(
                orderItem.productName(),
                orderItem.quantity(),
                captureProductSpecification(orderItem.productSpecification()),
                captureSizeBreakdowns(orderItem.sizes())
        );
    }

    private ProductSpecification captureProductSpecification(ProductSpecificationResult specification) {
        if (specification == null) {
            return ProductSpecification.empty();
        }

        return ProductSpecification.of(
                specification.garmentType(),
                specification.collarType(),
                specification.sleeveType(),
                specification.cuffRequired(),
                specification.sublimationRequired(),
                specification.embroideryRequired(),
                specification.dtfRequired(),
                specification.decorationNotes(),
                specification.includesNames(),
                specification.includesNumbers(),
                specification.includesLogos(),
                specification.personalizationNotes(),
                specification.itemObservations()
        );
    }

    private List<SizeBreakdown> captureSizeBreakdowns(List<SizeBreakdownResult> commercialSizeBreakdowns) {
        if (commercialSizeBreakdowns == null || commercialSizeBreakdowns.isEmpty()) {
            return List.of();
        }

        List<SizeBreakdown> sizeBreakdowns = new ArrayList<>();
        for (SizeBreakdownResult commercialSizeBreakdown : commercialSizeBreakdowns) {
            Objects.requireNonNull(commercialSizeBreakdown, "Size breakdown result must not be null");
            sizeBreakdowns.add(SizeBreakdown.create(
                    commercialSizeBreakdown.size(),
                    commercialSizeBreakdown.quantity()
            ));
        }
        return List.copyOf(sizeBreakdowns);
    }
}
