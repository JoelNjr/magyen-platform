package com.magyen.platform.plotter.application.port;

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

    Optional<PlotterCommercialOrderView> findOrder(UUID orderId);

    Optional<String> findCustomerName(UUID customerId);
}
