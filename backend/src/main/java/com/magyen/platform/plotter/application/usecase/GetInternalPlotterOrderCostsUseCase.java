package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetInternalPlotterOrderCostsQuery;
import com.magyen.platform.plotter.application.dto.GetInternalPlotterOrderCostsResult;
import com.magyen.platform.plotter.application.port.PlotterInventoryCostPort;
import com.magyen.platform.plotter.application.port.PlotterInventoryCostPort.PlotterInventoryCostSnapshot;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Acumula el costo histórico de papel de trabajos INTERNAL_MAGYEN de una orden.
 * <p>
 * No crea gasto Finance. Un trabajo interno es operación de material, no venta.
 */
public class GetInternalPlotterOrderCostsUseCase {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterInventoryCostPort plotterInventoryCostPort;

    public GetInternalPlotterOrderCostsUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterInventoryCostPort plotterInventoryCostPort
    ) {
        this.plotterJobRepository = Objects.requireNonNull(
                plotterJobRepository,
                "Plotter job repository must not be null"
        );
        this.plotterInventoryCostPort = Objects.requireNonNull(
                plotterInventoryCostPort,
                "Plotter inventory cost port must not be null"
        );
    }

    @Transactional(readOnly = true)
    public GetInternalPlotterOrderCostsResult execute(GetInternalPlotterOrderCostsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.orderId(), "Order id must not be null");

        List<PlotterJob> internalJobs = plotterJobRepository.findByOrderId(query.orderId()).stream()
                .filter(job -> job.getJobType() == PlotterJobType.INTERNAL_MAGYEN)
                .toList();

        BigDecimal plotterMaterialCost = ZERO_MONEY;
        int valuedJobCount = 0;
        int unvaluedJobCount = 0;

        for (PlotterJob job : internalJobs) {
            PlotterInventoryCostSnapshot snapshot = plotterInventoryCostPort
                    .findCostByPlotterJobId(job.getId())
                    .orElse(null);
            if (snapshot == null || !snapshot.valued()) {
                unvaluedJobCount++;
                continue;
            }
            valuedJobCount++;
            plotterMaterialCost = plotterMaterialCost.add(snapshot.totalCost());
        }

        boolean attributable = !internalJobs.isEmpty() && unvaluedJobCount == 0;
        return new GetInternalPlotterOrderCostsResult(
                plotterMaterialCost.setScale(2, RoundingMode.HALF_UP),
                internalJobs.size(),
                valuedJobCount,
                unvaluedJobCount,
                attributable
        );
    }
}
