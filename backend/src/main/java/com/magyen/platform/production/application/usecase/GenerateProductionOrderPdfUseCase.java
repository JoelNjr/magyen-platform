package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.ProductionDocumentFilename;
import com.magyen.platform.production.application.ProductionPdfDocumentMapper;
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.ProductionDocumentPdfResult;
import com.magyen.platform.production.application.dto.ProductionOrderPdfDocument;
import com.magyen.platform.production.application.port.ProductionDocumentPdfPort;

import java.util.Objects;

/**
 * Genera el PDF de una Orden de Producción a partir de la lectura existente.
 * <p>
 * No crea documentos persistidos. No modifica la orden.
 */
public class GenerateProductionOrderPdfUseCase {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final GetProductionOrderUseCase getProductionOrderUseCase;
    private final ProductionDocumentPdfPort productionDocumentPdfPort;

    public GenerateProductionOrderPdfUseCase(
            GetProductionOrderUseCase getProductionOrderUseCase,
            ProductionDocumentPdfPort productionDocumentPdfPort
    ) {
        this.getProductionOrderUseCase = Objects.requireNonNull(
                getProductionOrderUseCase,
                "Get production order use case must not be null"
        );
        this.productionDocumentPdfPort = Objects.requireNonNull(
                productionDocumentPdfPort,
                "Production document PDF port must not be null"
        );
    }

    public ProductionDocumentPdfResult execute(GetProductionOrderCommand command) {
        GetProductionOrderResult productionOrder = getProductionOrderUseCase.execute(command);
        ProductionOrderPdfDocument document = ProductionPdfDocumentMapper.toDocument(productionOrder);
        byte[] content = productionDocumentPdfPort.renderProductionOrder(document);
        return new ProductionDocumentPdfResult(
                content,
                ProductionDocumentFilename.productionOrder(document.orderNumber()),
                PDF_CONTENT_TYPE
        );
    }
}
