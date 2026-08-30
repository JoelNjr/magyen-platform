package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CommercialDocumentFilename;
import com.magyen.platform.commercial.application.CommercialPdfDocumentMapper;
import com.magyen.platform.commercial.application.CustomerNameResolver;
import com.magyen.platform.commercial.application.dto.CommercialDocumentPdfResult;
import com.magyen.platform.commercial.application.dto.GetQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetQuotationResult;
import com.magyen.platform.commercial.application.dto.QuotationPdfDocument;
import com.magyen.platform.commercial.application.port.CommercialDocumentPdfPort;

import java.util.Objects;

/**
 * Genera el PDF de una cotización a partir de la lectura comercial existente.
 * <p>
 * No crea documentos persistidos. No modifica la cotización.
 */
public class GenerateQuotationPdfUseCase {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final GetQuotationUseCase getQuotationUseCase;
    private final CustomerNameResolver customerNameResolver;
    private final CommercialDocumentPdfPort commercialDocumentPdfPort;

    public GenerateQuotationPdfUseCase(
            GetQuotationUseCase getQuotationUseCase,
            CustomerNameResolver customerNameResolver,
            CommercialDocumentPdfPort commercialDocumentPdfPort
    ) {
        this.getQuotationUseCase = Objects.requireNonNull(
                getQuotationUseCase,
                "Get quotation use case must not be null"
        );
        this.customerNameResolver = Objects.requireNonNull(
                customerNameResolver,
                "Customer name resolver must not be null"
        );
        this.commercialDocumentPdfPort = Objects.requireNonNull(
                commercialDocumentPdfPort,
                "Commercial document PDF port must not be null"
        );
    }

    public CommercialDocumentPdfResult execute(GetQuotationCommand command) {
        GetQuotationResult quotation = getQuotationUseCase.execute(command);
        String customerName = customerNameResolver.resolveName(quotation.customerId());
        QuotationPdfDocument document = CommercialPdfDocumentMapper.toQuotationDocument(quotation, customerName);
        byte[] content = commercialDocumentPdfPort.renderQuotation(document);
        return new CommercialDocumentPdfResult(
                content,
                CommercialDocumentFilename.quotation(
                        quotation.quotationNumber() == null ? null : String.valueOf(quotation.quotationNumber())
                ),
                PDF_CONTENT_TYPE
        );
    }
}
