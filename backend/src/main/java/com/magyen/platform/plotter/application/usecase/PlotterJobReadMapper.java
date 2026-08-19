package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.domain.PlotterJob;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Mapeo compartido de {@link PlotterJob} a resultados de aplicación, con identidad de negocio.
 */
final class PlotterJobReadMapper {

    private PlotterJobReadMapper() {
    }

    static CreatePlotterJobResult toCreateResult(
            PlotterJob plotterJob,
            PlotterCommercialOrderPort commercialOrderPort
    ) {
        Identity identity = resolveIdentity(plotterJob, commercialOrderPort);
        return new CreatePlotterJobResult(
                plotterJob.getId(),
                plotterJob.getJobType(),
                plotterJob.getCustomerId(),
                identity.customerName(),
                plotterJob.getOrderId(),
                identity.orderNumber(),
                identity.orderDescription(),
                plotterJob.getCreationDate(),
                plotterJob.getPaperInventoryItemId(),
                plotterJob.getPrintedMeters(),
                plotterJob.getPricePerMeter(),
                plotterJob.getTotalAmount(),
                plotterJob.getStatus(),
                plotterJob.getObservations()
        );
    }

    static GetPlotterJobResult toGetResult(
            PlotterJob plotterJob,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount,
            PlotterCommercialOrderPort commercialOrderPort
    ) {
        Identity identity = resolveIdentity(plotterJob, commercialOrderPort);
        return new GetPlotterJobResult(
                plotterJob.getId(),
                plotterJob.getJobType(),
                plotterJob.getCustomerId(),
                identity.customerName(),
                plotterJob.getOrderId(),
                identity.orderNumber(),
                identity.orderDescription(),
                plotterJob.getCreationDate(),
                plotterJob.getPaperInventoryItemId(),
                plotterJob.getPrintedMeters(),
                plotterJob.getPricePerMeter(),
                plotterJob.getTotalAmount(),
                paidAmount,
                outstandingAmount,
                plotterJob.getStatus(),
                plotterJob.getObservations()
        );
    }

    private static Identity resolveIdentity(
            PlotterJob plotterJob,
            PlotterCommercialOrderPort commercialOrderPort
    ) {
        Objects.requireNonNull(commercialOrderPort, "Commercial order port must not be null");
        if (plotterJob.getOrderId() != null) {
            PlotterCommercialOrderView order = commercialOrderPort.findOrder(plotterJob.getOrderId()).orElse(null);
            if (order != null) {
                return new Identity(order.customerName(), order.orderNumber(), order.description());
            }
        }
        String customerName = plotterJob.getCustomerId() == null
                ? null
                : commercialOrderPort.findCustomerName(plotterJob.getCustomerId()).orElse(null);
        return new Identity(customerName, null, null);
    }

    private record Identity(String customerName, String orderNumber, String orderDescription) {
    }
}
