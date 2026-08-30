package com.magyen.platform.commercial.presentation.order.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta HTTP con el detalle completo de una Orden.
 */
public record GetOrderResponse(
        UUID orderId,
        String orderNumber,
        String description,
        UUID customerId,
        String customerName,
        UUID quotationId,
        Long quotationNumber,
        String quotationNumberDisplay,
        LocalDate confirmationDate,
        String status,
        DeliveryCommitmentResponse deliveryCommitment,
        PaymentSummaryResponse paymentSummary,
        UUID sellerId,
        String sellerName,
        String observations,
        List<OrderItemResponse> items,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {

    /**
     * Compromiso de entrega expuesto por la API de detalle.
     */
    public record DeliveryCommitmentResponse(
            LocalDate promisedDeliveryDate,
            String deliveryObservations
    ) {
    }

    /**
     * Resumen de pago comercial expuesto por la API de detalle.
     */
    public record PaymentSummaryResponse(
            boolean advanceAcknowledged,
            boolean finalPaymentAcknowledged,
            BigDecimal committedTotal,
            BigDecimal remainingBalance
    ) {
    }
}
