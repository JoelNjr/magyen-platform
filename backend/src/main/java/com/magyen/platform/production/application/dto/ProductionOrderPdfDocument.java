package com.magyen.platform.production.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Modelo de lectura para el PDF de Orden de Producción.
 * <p>
 * El número de documento es el del pedido comercial. El snapshot productivo
 * no tiene consecutivo propio. {@code referenceImage} es opcional y puede ser nulo.
 */
public record ProductionOrderPdfDocument(
        String orderNumber,
        String orderDescription,
        String customerName,
        LocalDate creationDate,
        String statusLabel,
        String priorityLabel,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        LocalDate actualStartDate,
        LocalDate actualCompletionDate,
        String observations,
        List<ProductionDocumentProductLine> lines,
        List<ProductionDocumentOperationLine> operations,
        byte[] referenceImage
) {
}
