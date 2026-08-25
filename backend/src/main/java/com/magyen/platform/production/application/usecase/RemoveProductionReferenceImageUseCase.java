package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.RemoveProductionReferenceImageCommand;
import com.magyen.platform.production.application.dto.RemoveProductionReferenceImageResult;
import com.magyen.platform.production.application.port.ProductionReferenceImageStoragePort;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.Objects;

/**
 * Elimina la imagen de referencia. Solo deja la orden en NULL si el almacenamiento
 * confirmó la eliminación (o el objeto ya no existía).
 */
public class RemoveProductionReferenceImageUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionReferenceImageStoragePort productionReferenceImageStoragePort;

    public RemoveProductionReferenceImageUseCase(
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

    public RemoveProductionReferenceImageResult execute(RemoveProductionReferenceImageCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        if (productionOrder.getReferenceImage() == null) {
            return new RemoveProductionReferenceImageResult(productionOrder.getId(), false);
        }

        productionReferenceImageStoragePort.delete(productionOrder.getReferenceImage().getObjectKey());
        productionOrder.clearReferenceImage();
        productionOrderRepository.save(productionOrder);

        return new RemoveProductionReferenceImageResult(productionOrder.getId(), false);
    }
}
