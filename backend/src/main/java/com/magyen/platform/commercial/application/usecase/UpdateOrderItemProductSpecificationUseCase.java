package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CommercialCatalogValidator;
import com.magyen.platform.commercial.application.dto.ProductSpecificationResult;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.ProductSpecification;

import java.util.Objects;

/**
 * Caso de uso que actualiza la especificación comercial de un OrderItem.
 */
public class UpdateOrderItemProductSpecificationUseCase {

    private final OrderRepository orderRepository;
    private final CommercialCatalogValidator commercialCatalogValidator;

    public UpdateOrderItemProductSpecificationUseCase(
            OrderRepository orderRepository,
            CommercialCatalogValidator commercialCatalogValidator
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.commercialCatalogValidator = Objects.requireNonNull(
                commercialCatalogValidator,
                "Commercial catalog validator must not be null"
        );
    }

    public UpdateOrderItemProductSpecificationResult execute(UpdateOrderItemProductSpecificationCommand command) {
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

        ProductSpecification productSpecification =
                commercialCatalogValidator.requireProductSpecification(command.productSpecification());
        orderItem.assignProductSpecification(productSpecification);

        Order savedOrder = orderRepository.save(order);

        OrderItem savedItem = savedOrder.getItems().stream()
                .filter(item -> item.getId().equals(command.orderItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Order item missing after save: " + command.orderItemId()
                ));

        return new UpdateOrderItemProductSpecificationResult(
                savedItem.getId(),
                toProductSpecificationResult(savedItem.getProductSpecification())
        );
    }

    private void validateCommand(UpdateOrderItemProductSpecificationCommand command) {
        Objects.requireNonNull(command.orderId(), "Order id must not be null");
        Objects.requireNonNull(command.orderItemId(), "Order item id must not be null");
        Objects.requireNonNull(command.productSpecification(), "Product specification must not be null");
    }

    private ProductSpecificationResult toProductSpecificationResult(ProductSpecification specification) {
        ProductSpecification resolved = specification == null ? ProductSpecification.empty() : specification;

        return new ProductSpecificationResult(
                resolved.getGarmentType(),
                resolved.getCollarType(),
                resolved.getSleeveType(),
                resolved.getCuffRequired(),
                resolved.isSublimationRequired(),
                resolved.isEmbroideryRequired(),
                resolved.isDtfRequired(),
                resolved.getDecorationNotes(),
                resolved.isIncludesNames(),
                resolved.isIncludesNumbers(),
                resolved.isIncludesLogos(),
                resolved.getPersonalizationNotes(),
                resolved.getItemObservations()
        );
    }
}
