package com.magyen.platform.commercial.application.port;

import com.magyen.platform.commercial.application.dto.QuotationPdfDocument;
import com.magyen.platform.commercial.application.dto.RemissionPdfDocument;

/**
 * Puerto de generación de documentos PDF comerciales.
 * <p>
 * Recibe modelos de lectura ya ensamblados. No consulta repositorios.
 */
public interface CommercialDocumentPdfPort {

    byte[] renderQuotation(QuotationPdfDocument document);

    byte[] renderRemission(RemissionPdfDocument document);
}
