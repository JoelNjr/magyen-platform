package com.magyen.platform.plotter.infrastructure.inventory;

import com.magyen.platform.inventory.application.dto.GetInkAcquisitionsQuery;
import com.magyen.platform.inventory.application.usecase.GetInkAcquisitionsUseCase;
import com.magyen.platform.plotter.application.port.PlotterInkAcquisitionPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Adaptador Plotter → Inventory para compras de tinta.
 */
public class PlotterInkAcquisitionAdapter implements PlotterInkAcquisitionPort {

    private final GetInkAcquisitionsUseCase getInkAcquisitionsUseCase;

    public PlotterInkAcquisitionAdapter(GetInkAcquisitionsUseCase getInkAcquisitionsUseCase) {
        this.getInkAcquisitionsUseCase = Objects.requireNonNull(
                getInkAcquisitionsUseCase,
                "Get ink acquisitions use case must not be null"
        );
    }

    @Override
    public List<InkAcquisitionSnapshot> findInkAcquisitions(LocalDate fromDate, LocalDate toDate) {
        return getInkAcquisitionsUseCase.execute(new GetInkAcquisitionsQuery(fromDate, toDate))
                .acquisitions()
                .stream()
                .map(item -> new InkAcquisitionSnapshot(item.purchaseDate(), item.totalCost()))
                .toList();
    }
}
