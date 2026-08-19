package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;

/**
 * Línea de producto para un PDF comercial. Sin identidades técnicas.
 */
public record CommercialDocumentProductLine(
        String productName,
        String garmentType,
        String description,
        int quantity,
        String sizes,
        String mainFabric,
        String secondaryFabric,
        String color,
        String collarType,
        String sleeveType,
        String cuffLabel,
        String extraSpecifications,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
