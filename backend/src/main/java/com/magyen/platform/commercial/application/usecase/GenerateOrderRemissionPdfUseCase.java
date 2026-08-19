package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CommercialDocumentFilename;
import com.magyen.platform.commercial.application.CommercialPdfDocumentMapper;
import com.magyen.platform.commercial.application.dto.CommercialDocumentPdfResult;
import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.dto.RemissionPdfDocument;
import com.magyen.platform.commercial.application.port.CommercialDocumentPdfPort;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;

import java.util.Objects;

/**
 * Genera el PDF de remisión de una orden comercial a partir de lecturas existentes.
 * <p>
 * No es una factura. No crea documentos persistidos. No modifica la orden.
 */
public class GenerateOrderRemissionPdfUseCase {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final GetOrderUseCase getOrderUseCase;
    private final OrderPaymentCollectionPort orderPaymentCollectionPort;
    private final CommercialDocumentPdfPort commercialDocumentPdfPort;

    public GenerateOrderRemissionPdfUseCase(
            GetOrderUseCase getOrderUseCase,
            OrderPaymentCollectionPort orderPaymentCollectionPort,
            CommercialDocumentPdfPort commercialDocumentPdfPort
    ) {
        this.getOrderUseCase = Objects.requireNonNull(getOrderUseCase, "Get order use case must not be null");
        this.orderPaymentCollectionPort = Objects.requireNonNull(
                orderPaymentCollectionPort,
                "Order payment collection port must not be null"
        );
        this.commercialDocumentPdfPort = Objects.requireNonNull(
                commercialDocumentPdfPort,
                "Commercial document PDF port must not be null"
        );
    }

    public CommercialDocumentPdfResult execute(GetOrderCommand command) {
        GetOrderResult order = getOrderUseCase.execute(command);
        OrderPaymentCollectionPort.OrderPaymentCollection collection =
                orderPaymentCollectionPort.getCollection(order.orderId());
        RemissionPdfDocument document = CommercialPdfDocumentMapper.toRemissionDocument(order, collection);
        byte[] content = commercialDocumentPdfPort.renderRemission(document);
        return new CommercialDocumentPdfResult(
                content,
                CommercialDocumentFilename.remission(document.orderNumber()),
                PDF_CONTENT_TYPE
        );
    }
}
