package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetHistoricalCommercialOrdersForPlotterQuery;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Lista pedidos entregados o cerrados de un mes de confirmación.
 * <p>
 * No incluye pedidos abiertos y no modifica su estado.
 */
public class GetHistoricalCommercialOrdersForPlotterUseCase {

    private final PlotterCommercialOrderPort plotterCommercialOrderPort;

    public GetHistoricalCommercialOrdersForPlotterUseCase(PlotterCommercialOrderPort plotterCommercialOrderPort) {
        this.plotterCommercialOrderPort = Objects.requireNonNull(
                plotterCommercialOrderPort,
                "Plotter commercial order port must not be null"
        );
    }

    public List<PlotterCommercialOrderView> execute(GetHistoricalCommercialOrdersForPlotterQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        if (query.month() < 1 || query.month() > 12) {
            throw new PlotterDomainException("Historical order month must be between 1 and 12");
        }

        LocalDate fromDate;
        try {
            fromDate = LocalDate.of(query.year(), query.month(), 1);
        } catch (DateTimeException exception) {
            throw new PlotterDomainException("Historical order month is invalid");
        }
        LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
        return plotterCommercialOrderPort.findOrdersEligibleForHistoricalPlotterJob(fromDate, toDate);
    }
}
