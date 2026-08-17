package com.magyen.platform.plotter.infrastructure.inventory;

import com.magyen.platform.inventory.application.dto.GetInventoryMovementBySourceQuery;
import com.magyen.platform.inventory.application.usecase.GetInventoryMovementBySourceUseCase;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.plotter.application.port.PlotterInventoryCostPort;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador Plotter → Inventory para el snapshot histórico del OUT de papel.
 */
public class PlotterInventoryCostAdapter implements PlotterInventoryCostPort {

    private final GetInventoryMovementBySourceUseCase getInventoryMovementBySourceUseCase;

    public PlotterInventoryCostAdapter(
            GetInventoryMovementBySourceUseCase getInventoryMovementBySourceUseCase
    ) {
        this.getInventoryMovementBySourceUseCase = Objects.requireNonNull(
                getInventoryMovementBySourceUseCase,
                "Get inventory movement by source use case must not be null"
        );
    }

    @Override
    public Optional<PlotterInventoryCostSnapshot> findCostByPlotterJobId(UUID plotterJobId) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        return getInventoryMovementBySourceUseCase.execute(
                new GetInventoryMovementBySourceQuery(InventoryMovementSourceType.PLOTTER, plotterJobId)
        ).map(movement -> new PlotterInventoryCostSnapshot(
                plotterJobId,
                movement.unitCost(),
                movement.totalCost()
        ));
    }
}
