package com.magyen.platform.production.application;

/**
 * Nombre de archivo del PDF de Orden de Producción. Usa el número de pedido comercial, nunca un UUID.
 */
public final class ProductionDocumentFilename {

    private ProductionDocumentFilename() {
    }

    public static String productionOrder(String orderNumber) {
        String identifier = sanitize(orderNumber);
        if (identifier.isBlank()) {
            return "Orden-de-Produccion.pdf";
        }
        return "Orden-de-Produccion_" + identifier + ".pdf";
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
