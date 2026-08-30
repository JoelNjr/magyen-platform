package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Resultado del caso de uso que consulta una Orden completa.
 */
public record GetOrderResult(
        UUID orderId,
        String orderNumber,
        String description,
        UUID customerId,
        String customerName,
        UUID quotationId,
        Long quotationNumber,
        String quotationNumberDisplay,
        LocalDate confirmationDate,
        OrderStatus status,
        DeliveryCommitmentResult deliveryCommitment,
        PaymentSummaryResult paymentSummary,
        UUID sellerId,
        String sellerName,
        String observations,
        List<OrderItemResult> items,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
