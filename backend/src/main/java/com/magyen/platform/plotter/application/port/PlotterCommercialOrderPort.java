package com.magyen.platform.plotter.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Application para validar una Orden comercial atribuible a Plotter
 * y enriquecer identidad de cliente/orden en lectura.
 * <p>
 * No expone entidades JPA de Commercial.
 */
public interface PlotterCommercialOrderPort {

    PlotterCommercialOrderView requireExistingOrder(UUID orderId);

    /**
     * Exige una orden que todavía acepta un trabajo INTERNAL_MAGYEN.
     * Abierta = {@code CONFIRMED}, {@code IN_PRODUCTION} o {@code READY_FOR_DELIVERY}.
     */
    PlotterCommercialOrderView requireOrderOpenForPlotterJob(UUID orderId);

    /**
     * Órdenes que todavía pueden recibir un trabajo INTERNAL_MAGYEN.
     */
    List<PlotterCommercialOrderView> findOrdersOpenForPlotterJob();

    /**
     * Exige un pedido entregado o cerrado para un registro atrasado.
     * No reabre el pedido ni altera su estado comercial.
     */
    PlotterCommercialOrderView requireOrderEligibleForHistoricalPlotterJob(UUID orderId);

    /**
     * Pedidos entregados o cerrados cuya confirmación cae en el rango.
     * Los pedidos abiertos siguen en {@link #findOrdersOpenForPlotterJob()}.
     */
    List<PlotterCommercialOrderView> findOrdersEligibleForHistoricalPlotterJob(
            LocalDate fromDate,
            LocalDate toDate
    );

    Optional<PlotterCommercialOrderView> findOrder(UUID orderId);

    Optional<String> findCustomerName(UUID customerId);

    /**
     * Exige que el cliente exista y sea del grupo PLOTTER.
     * Un cliente Magyen o sin clasificar no puede registrar un trabajo externo.
     */
    void requireExternalPlotterCustomer(UUID customerId);
}
