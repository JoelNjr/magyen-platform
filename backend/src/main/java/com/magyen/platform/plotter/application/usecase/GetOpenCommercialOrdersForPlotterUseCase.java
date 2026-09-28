package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;

import java.util.List;
import java.util.Objects;

/**
 * Lista órdenes comerciales que todavía pueden recibir un trabajo INTERNAL_MAGYEN.
 * <p>
 * Abierta significa CONFIRMED, IN_PRODUCTION o READY_FOR_DELIVERY.
 * DELIVERED y CLOSED no aparecen. No existe estado CANCELLED en Order.
 */
public class GetOpenCommercialOrdersForPlotterUseCase {

    private final PlotterCommercialOrderPort plotterCommercialOrderPort;

    public GetOpenCommercialOrdersForPlotterUseCase(PlotterCommercialOrderPort plotterCommercialOrderPort) {
        this.plotterCommercialOrderPort = Objects.requireNonNull(
                plotterCommercialOrderPort,
                "Plotter commercial order port must not be null"
        );
    }

    public List<PlotterCommercialOrderView> execute() {
        return plotterCommercialOrderPort.findOrdersOpenForPlotterJob();
    }
}
