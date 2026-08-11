package com.magyen.platform.commercial.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port de lectura de cobranza de una Orden comercial (pagos Finance).
 * <p>
 * {@code collectedAmount} es la suma de Payments por orderId.
 * {@code outstandingAmount} es orderValue - collected (no se usa el resumen comercial de anticipos).
 */
public interface OrderPaymentCollectionPort {

    OrderPaymentCollection getCollection(UUID orderId);

    record OrderPaymentCollection(
            BigDecimal collectedAmount,
            BigDecimal outstandingAmount
    ) {
    }
}
