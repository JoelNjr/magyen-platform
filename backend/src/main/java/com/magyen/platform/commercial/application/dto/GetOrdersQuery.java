package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;

/**
 * Filtro opcional de pedidos por fecha de confirmación.
 * <p>
 * Sin fechas se listan todos. Sin búsqueda, elegibilidad ni límite se conserva
 * el listado histórico completo.
 * {@code acceptsDirectCost} restringe a órdenes que la rentabilidad puede recibir
 * un gasto atribuido. {@code limit} acota el resultado y lo ordena del más reciente.
 */
public record GetOrdersQuery(
        LocalDate fromDate,
        LocalDate toDate,
        String search,
        Boolean acceptsDirectCost,
        Integer limit
) {
    public GetOrdersQuery(LocalDate fromDate, LocalDate toDate) {
        this(fromDate, toDate, null, null, null);
    }
}
