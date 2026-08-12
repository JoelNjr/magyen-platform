package com.magyen.platform.home.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Sección de cuentas por cobrar del Dashboard Home (solo lectura).
 * <p>
 * Representa saldos pendientes <strong>actuales</strong> de Órdenes comerciales,
 * no un recorte por el período financiero del Dashboard.
 */
public record HomeReceivablesSummary(
        BigDecimal totalOutstandingAmount,
        BigDecimal totalCollectedAmount,
        int orderCount,
        List<HomeReceivableItem> items
) {
}
