package com.magyen.platform.commercial.application;

/**
 * Nombres de archivo de documentos comerciales. Usa el identificador de negocio, nunca un UUID.
 */
public final class CommercialDocumentFilename {

    private CommercialDocumentFilename() {
    }

    public static String quotation(String quotationNumberDisplay) {
        String identifier = sanitize(quotationNumberDisplay);
        if (identifier.isBlank()) {
            return "Cotizacion.pdf";
        }
        return "Cotizacion_" + identifier + ".pdf";
    }

    public static String remission(String orderNumber) {
        String identifier = sanitize(orderNumber);
        if (identifier.isBlank()) {
            return "Remision.pdf";
        }
        return "Remision_" + identifier + ".pdf";
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replaceAll("[\\\\/:*?\"<>|]", "-")
                .replaceAll("\\s+", "-");
    }
}
