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
        UUID customerId,
        UUID quotationId,
        LocalDate confirmationDate,
        OrderStatus status,
        DeliveryCommitmentResult deliveryCommitment,
        PaymentSummaryResult paymentSummary,
        String salesperson,
        String observations,
        List<OrderItemResult> items,
        BigDecimal totalAmount
) {
}
