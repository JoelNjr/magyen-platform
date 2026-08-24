package com.magyen.platform.production.application.dto;

/**
 * Línea de prenda para el PDF operativo de orden de producción.
 * <p>
 * Solo incluye campos que existen en el snapshot productivo. No inventa tela ni color.
 */
public record ProductionDocumentProductLine(
        String productName,
        int quantity,
        String sizes,
        String garmentType,
        String collarType,
        String sleeveType,
        String cuffLabel,
        String extraSpecifications,
        String itemObservations
) {
}
