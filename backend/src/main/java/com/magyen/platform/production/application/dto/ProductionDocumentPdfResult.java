package com.magyen.platform.production.application.dto;

/**
 * PDF de producción generado. El contenido ya está armado; Presentation solo lo entrega.
 */
public record ProductionDocumentPdfResult(
        byte[] content,
        String filename,
        String contentType
) {
}
