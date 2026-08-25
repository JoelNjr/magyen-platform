package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.ProductionReferenceImageInspector;
import com.magyen.platform.production.application.ProductionReferenceImageInspector.InspectedReferenceImage;
import com.magyen.platform.production.application.ProductionReferenceImageObjectKeys;
import com.magyen.platform.production.application.dto.ReplaceProductionReferenceImageCommand;
import com.magyen.platform.production.application.dto.ReplaceProductionReferenceImageResult;
import com.magyen.platform.production.application.port.ProductionReferenceImageStoragePort;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionReferenceImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Carga o reemplaza la imagen de referencia. Sube el objeto nuevo y persiste la clave
 * antes de intentar borrar el objeto anterior.
 */
public class ReplaceProductionReferenceImageUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceProductionReferenceImageUseCase.class);

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionReferenceImageStoragePort productionReferenceImageStoragePort;
    private final ProductionReferenceImageInspector productionReferenceImageInspector;
    private final String objectKeyPrefix;

    public ReplaceProductionReferenceImageUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionReferenceImageStoragePort productionReferenceImageStoragePort,
            ProductionReferenceImageInspector productionReferenceImageInspector,
            String objectKeyPrefix
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.productionReferenceImageStoragePort = Objects.requireNonNull(
                productionReferenceImageStoragePort,
                "Production reference image storage port must not be null"
        );
        this.productionReferenceImageInspector = Objects.requireNonNull(
                productionReferenceImageInspector,
                "Production reference image inspector must not be null"
        );
        this.objectKeyPrefix = objectKeyPrefix;
    }

    public ReplaceProductionReferenceImageResult execute(ReplaceProductionReferenceImageCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        InspectedReferenceImage inspected = productionReferenceImageInspector.inspect(
                command.content(),
                command.originalFilename(),
                command.declaredContentType()
        );

        String previousKey = productionOrder.getReferenceImage() == null
                ? null
                : productionOrder.getReferenceImage().getObjectKey();
        String objectKey = ProductionReferenceImageObjectKeys.next(
                objectKeyPrefix,
                productionOrder.getId(),
                inspected.extension()
        );

        productionReferenceImageStoragePort.put(objectKey, inspected.content(), inspected.contentType());

        try {
            productionOrder.attachReferenceImage(ProductionReferenceImage.of(objectKey, inspected.contentType()));
            productionOrderRepository.save(productionOrder);
        } catch (RuntimeException exception) {
            deleteQuietly(objectKey, "new reference image after persistence failure");
            throw exception;
        }

        if (previousKey != null && !previousKey.equals(objectKey)) {
            deleteQuietly(previousKey, "previous reference image");
        }

        return new ReplaceProductionReferenceImageResult(productionOrder.getId(), true);
    }

    private void deleteQuietly(String objectKey, String description) {
        try {
            productionReferenceImageStoragePort.delete(objectKey);
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to delete {} [{}]: {}", description, objectKey, exception.getMessage());
        }
    }
}
