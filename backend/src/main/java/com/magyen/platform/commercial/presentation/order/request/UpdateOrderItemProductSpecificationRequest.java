package com.magyen.platform.commercial.presentation.order.request;

/**
 * Payload HTTP para actualizar la especificación comercial de un OrderItem.
 */
public record UpdateOrderItemProductSpecificationRequest(
        String garmentType,
        String collarType,
        String sleeveType,
        Boolean cuffRequired,
        Boolean sublimationRequired,
        Boolean embroideryRequired,
        Boolean dtfRequired,
        String decorationNotes,
        Boolean includesNames,
        Boolean includesNumbers,
        Boolean includesLogos,
        String personalizationNotes,
        String itemObservations
) {
}
