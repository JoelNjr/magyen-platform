package com.magyen.platform.commercial.presentation.quotation.response;

import com.magyen.platform.commercial.presentation.order.response.SizeBreakdownResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Vista previa HTTP de aplicar la cotización actual a su Orden.
 */
public record PreviewQuotationOrderSynchronizationResponse(
        boolean orderExists,
        UUID orderId,
        String orderNumber,
        String orderStatus,
        boolean applyAllowed,
        String unavailableReason,
        boolean legacyUntraced,
        boolean paymentFloorViolation,
        boolean sizeConstraintViolation,
        List<MatchedItemChangeResponse> matchedChanges,
        List<QuotationOnlyAdditionResponse> additions,
        List<OrphanRemovalResponse> orphanRemovals,
        List<ManualItemPreservedResponse> manualsPreserved,
        BigDecimal currentSubtotal,
        BigDecimal proposedSubtotal,
        BigDecimal currentDiscount,
        BigDecimal proposedDiscount,
        BigDecimal currentTotal,
        BigDecimal proposedTotal,
        BigDecimal collectedAmount,
        BigDecimal proposedOutstanding
) {

    public record MatchedItemChangeResponse(
            UUID orderItemId,
            UUID quotationItemId,
            String productName,
            int currentQuantity,
            int proposedQuantity,
            BigDecimal currentUnitPrice,
            BigDecimal proposedUnitPrice,
            BigDecimal currentSubtotal,
            BigDecimal proposedSubtotal,
            List<SizeBreakdownResponse> sizes
    ) {
    }

    public record QuotationOnlyAdditionResponse(
            UUID quotationItemId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }

    public record OrphanRemovalResponse(
            UUID orderItemId,
            UUID quotationItemId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal,
            List<SizeBreakdownResponse> sizes
    ) {
    }

    public record ManualItemPreservedResponse(
            UUID orderItemId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
