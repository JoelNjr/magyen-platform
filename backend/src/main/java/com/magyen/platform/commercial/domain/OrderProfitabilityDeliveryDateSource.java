package com.magyen.platform.commercial.domain;

/**
 * Origen de la fecha usada para mostrar (no para calcular) la entrega en rentabilidad individual.
 * <p>
 * {@code ACTUAL} = {@code actualDeliveryDate} persistida.
 * {@code HISTORICAL_FALLBACK} = pedido DELIVERED sin fecha real; la UI debe mostrar la fecha programada.
 * {@code CURRENT_MONTH} = no entregado; pertenece al mes calendario actual.
 */
public enum OrderProfitabilityDeliveryDateSource {

    ACTUAL,
    HISTORICAL_FALLBACK,
    CURRENT_MONTH
}
