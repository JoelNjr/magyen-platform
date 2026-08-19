package com.magyen.platform.commercial.application.dto;

/**
 * PDF comercial generado. El contenido ya está armado; Presentation solo lo entrega.
 */
public record CommercialDocumentPdfResult(
        byte[] content,
        String filename,
        String contentType
) {
}
