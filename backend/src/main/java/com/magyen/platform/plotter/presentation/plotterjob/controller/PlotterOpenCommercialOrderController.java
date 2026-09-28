package com.magyen.platform.plotter.presentation.plotterjob.controller;

import com.magyen.platform.plotter.application.usecase.GetOpenCommercialOrdersForPlotterUseCase;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetOpenCommercialOrdersForPlotterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Órdenes comerciales abiertas para un trabajo INTERNAL_MAGYEN.
 */
@RestController
@RequestMapping("/api/v1/plotter/open-commercial-orders")
public class PlotterOpenCommercialOrderController {

    private final GetOpenCommercialOrdersForPlotterUseCase getOpenCommercialOrdersForPlotterUseCase;

    public PlotterOpenCommercialOrderController(
            GetOpenCommercialOrdersForPlotterUseCase getOpenCommercialOrdersForPlotterUseCase
    ) {
        this.getOpenCommercialOrdersForPlotterUseCase = getOpenCommercialOrdersForPlotterUseCase;
    }

    @GetMapping
    public ResponseEntity<GetOpenCommercialOrdersForPlotterResponse> getOpenCommercialOrders() {
        var orders = getOpenCommercialOrdersForPlotterUseCase.execute().stream()
                .map(order -> new GetOpenCommercialOrdersForPlotterResponse.OpenCommercialOrderResponse(
                        order.orderId(),
                        order.orderNumber(),
                        order.description(),
                        order.customerId(),
                        order.customerName(),
                        order.status()
                ))
                .toList();
        return ResponseEntity.ok(new GetOpenCommercialOrdersForPlotterResponse(orders));
    }
}
