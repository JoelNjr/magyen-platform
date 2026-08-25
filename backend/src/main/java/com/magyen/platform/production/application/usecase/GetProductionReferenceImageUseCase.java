package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionReferenceImageResult;
import com.magyen.platform.production.application.dto.ProductionReferenceImageContent;
import com.magyen.platform.production.application.port.ProductionReferenceImageStoragePort;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionReferenceImage;

import java.util.Objects;

/**
 * Entrega los bytes de la imagen de referencia para un GET autenticado.
 */
public class GetProductionReferenceImageUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionReferenceImageStoragePort productionReferenceImageStoragePort;

    public GetProductionReferenceImageUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionReferenceImageStoragePort productionReferenceImageStoragePort
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.productionReferenceImageStoragePort = Objects.requireNonNull(
                productionReferenceImageStoragePort,
                "Production reference image storage port must not be null"
        );
    }

    public GetProductionReferenceImageResult execute(GetProductionOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        ProductionReferenceImage referenceImage = productionOrder.getReferenceImage();
        if (referenceImage == null) {
            throw new IllegalArgumentException("Reference image not found");
        }

        ProductionReferenceImageContent stored = productionReferenceImageStoragePort
                .get(referenceImage.getObjectKey())
                .orElseThrow(() -> new IllegalArgumentException("Reference image not found"));

        String contentType = stored.contentType() == null || stored.contentType().isBlank()
                ? referenceImage.getContentType()
                : stored.contentType();

        return new GetProductionReferenceImageResult(stored.content(), contentType);
    }
}
