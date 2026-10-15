package com.magyen.platform.plotter.presentation.plotterjob.controller;

import com.magyen.platform.plotter.application.dto.GetHistoricalCommercialOrdersForPlotterQuery;
import com.magyen.platform.plotter.application.usecase.GetHistoricalCommercialOrdersForPlotterUseCase;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetHistoricalCommercialOrdersForPlotterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pedidos entregados o cerrados para registrar un trabajo INTERNAL_MAGYEN atrasado.
 * <p>
 * El mes filtra por fecha de confirmación. No reabre pedidos.
 */
@RestController
@RequestMapping("/api/v1/plotter/historical-commercial-orders")
public class PlotterHistoricalCommercialOrderController {

    private final GetHistoricalCommercialOrdersForPlotterUseCase getHistoricalCommercialOrdersForPlotterUseCase;

    public PlotterHistoricalCommercialOrderController(
            GetHistoricalCommercialOrdersForPlotterUseCase getHistoricalCommercialOrdersForPlotterUseCase
    ) {
        this.getHistoricalCommercialOrdersForPlotterUseCase = getHistoricalCommercialOrdersForPlotterUseCase;
    }

    @GetMapping
    public ResponseEntity<GetHistoricalCommercialOrdersForPlotterResponse> getHistoricalCommercialOrders(
            @RequestParam int year,
            @RequestParam int month
    ) {
        var orders = getHistoricalCommercialOrdersForPlotterUseCase
                .execute(new GetHistoricalCommercialOrdersForPlotterQuery(year, month))
                .stream()
                .map(order -> new GetHistoricalCommercialOrdersForPlotterResponse.HistoricalCommercialOrderResponse(
                        order.orderId(),
                        order.orderNumber(),
                        order.description(),
                        order.customerId(),
                        order.customerName(),
                        order.confirmationDate(),
                        order.deliveryDate(),
                        order.status()
                ))
                .toList();
        return ResponseEntity.ok(new GetHistoricalCommercialOrdersForPlotterResponse(orders));
    }
}
