package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Vista previa de sincronización cotización → orden. No muta nada.
 */
public record PreviewQuotationOrderSynchronizationResult(
        boolean orderExists,
        UUID orderId,
        String orderNumber,
        String orderStatus,
        boolean applyAllowed,
        String unavailableReason,
        boolean legacyUntraced,
        boolean paymentFloorViolation,
        boolean sizeConstraintViolation,
        List<MatchedItemChange> matchedChanges,
        List<QuotationOnlyAddition> additions,
        List<OrphanRemoval> orphanRemovals,
        List<ManualItemPreserved> manualsPreserved,
        BigDecimal currentSubtotal,
        BigDecimal proposedSubtotal,
        BigDecimal currentDiscount,
        BigDecimal proposedDiscount,
        BigDecimal currentTotal,
        BigDecimal proposedTotal,
        BigDecimal collectedAmount,
        BigDecimal proposedOutstanding
) {

    public record MatchedItemChange(
            UUID orderItemId,
            UUID quotationItemId,
            String productName,
            int currentQuantity,
            int proposedQuantity,
            BigDecimal currentUnitPrice,
            BigDecimal proposedUnitPrice,
            BigDecimal currentSubtotal,
            BigDecimal proposedSubtotal,
            List<SizeBreakdownResult> sizes
    ) {
    }

    public record QuotationOnlyAddition(
            UUID quotationItemId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }

    public record OrphanRemoval(
            UUID orderItemId,
            UUID quotationItemId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal,
            List<SizeBreakdownResult> sizes
    ) {
    }

    public record ManualItemPreserved(
            UUID orderItemId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
