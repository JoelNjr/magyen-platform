package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.ProductionDocumentFilename;
import com.magyen.platform.production.application.ProductionPdfDocumentMapper;
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.ProductionDocumentPdfResult;
import com.magyen.platform.production.application.dto.ProductionOrderPdfDocument;
import com.magyen.platform.production.application.dto.ProductionReferenceImageContent;
import com.magyen.platform.production.application.port.ProductionDocumentPdfPort;
import com.magyen.platform.production.application.port.ProductionReferenceImageStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Genera el PDF de una Orden de Producción a partir de la lectura existente.
 * <p>
 * No crea documentos persistidos. No modifica la orden.
 * Si la imagen de referencia no se puede recuperar, el PDF se genera sin ella.
 */
public class GenerateProductionOrderPdfUseCase {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateProductionOrderPdfUseCase.class);

    private final GetProductionOrderUseCase getProductionOrderUseCase;
    private final ProductionDocumentPdfPort productionDocumentPdfPort;
    private final ProductionReferenceImageStoragePort productionReferenceImageStoragePort;

    public GenerateProductionOrderPdfUseCase(
            GetProductionOrderUseCase getProductionOrderUseCase,
            ProductionDocumentPdfPort productionDocumentPdfPort,
            ProductionReferenceImageStoragePort productionReferenceImageStoragePort
    ) {
        this.getProductionOrderUseCase = Objects.requireNonNull(
                getProductionOrderUseCase,
                "Get production order use case must not be null"
        );
        this.productionDocumentPdfPort = Objects.requireNonNull(
                productionDocumentPdfPort,
                "Production document PDF port must not be null"
        );
        this.productionReferenceImageStoragePort = Objects.requireNonNull(
                productionReferenceImageStoragePort,
                "Production reference image storage port must not be null"
        );
    }

    public ProductionDocumentPdfResult execute(GetProductionOrderCommand command) {
        GetProductionOrderResult productionOrder = getProductionOrderUseCase.execute(command);
        byte[] referenceImage = loadReferenceImage(productionOrder);
        ProductionOrderPdfDocument document = ProductionPdfDocumentMapper.toDocument(productionOrder, referenceImage);
        byte[] content = productionDocumentPdfPort.renderProductionOrder(document);
        return new ProductionDocumentPdfResult(
                content,
                ProductionDocumentFilename.productionOrder(document.orderNumber()),
                PDF_CONTENT_TYPE
        );
    }

    private byte[] loadReferenceImage(GetProductionOrderResult productionOrder) {
        String objectKey = productionOrder.referenceImageObjectKey();
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        try {
            return productionReferenceImageStoragePort.get(objectKey)
                    .map(ProductionReferenceImageContent::content)
                    .orElse(null);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Unable to load production reference image [{}]: {}",
                    objectKey,
                    exception.getMessage()
            );
            return null;
        }
    }
}
