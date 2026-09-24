package com.magyen.platform.commercial.domain;

/**
 * Estado de lectura de la comisión mensual.
 * <p>
 * No es una liquidación persistida. {@link #HISTORICAL} es anterior a septiembre 2026
 * y no se paga en este módulo. {@link #CALCULATED} es el mes visible y todavía no pagado.
 */
public enum SellerCommissionReadStatus {
    CALCULATED,
    HISTORICAL
}
