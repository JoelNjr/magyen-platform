package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;

/**
 * Período inclusive para rentabilidad Home por entrega programada.
 */
public record GetOrderProfitabilityByPromisedDeliveryPeriodQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
