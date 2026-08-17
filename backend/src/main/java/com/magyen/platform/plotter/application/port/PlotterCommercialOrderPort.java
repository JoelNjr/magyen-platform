package com.magyen.platform.plotter.application.port;

import java.util.UUID;

/**
 * Puerto de Application para validar una Orden comercial atribuible a Plotter.
 * <p>
 * No expone entidades JPA de Commercial.
 */
public interface PlotterCommercialOrderPort {

    PlotterCommercialOrderView requireExistingOrder(UUID orderId);
}
