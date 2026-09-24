package com.magyen.platform.finance.domain;

/**
 * Estado de una liquidación de comisión de vendedor.
 * <p>
 * V1 solo persiste liquidaciones ya pagadas. No existe un estado pendiente.
 */
public enum SellerCommissionSettlementStatus {
    PAID
}
