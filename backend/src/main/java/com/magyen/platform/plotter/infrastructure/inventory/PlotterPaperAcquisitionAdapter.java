package com.magyen.platform.plotter.infrastructure.inventory;

import com.magyen.platform.inventory.application.dto.GetPaperAcquisitionsQuery;
import com.magyen.platform.inventory.application.usecase.GetPaperAcquisitionsUseCase;
import com.magyen.platform.plotter.application.port.PlotterPaperAcquisitionPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Adaptador Plotter → Inventory para compras de papel.
 */
public class PlotterPaperAcquisitionAdapter implements PlotterPaperAcquisitionPort {

    private final GetPaperAcquisitionsUseCase getPaperAcquisitionsUseCase;

    public PlotterPaperAcquisitionAdapter(GetPaperAcquisitionsUseCase getPaperAcquisitionsUseCase) {
        this.getPaperAcquisitionsUseCase = Objects.requireNonNull(
                getPaperAcquisitionsUseCase,
                "Get paper acquisitions use case must not be null"
        );
    }

    @Override
    public List<PaperAcquisitionSnapshot> findPaperAcquisitions(LocalDate fromDate, LocalDate toDate) {
        return getPaperAcquisitionsUseCase.execute(new GetPaperAcquisitionsQuery(fromDate, toDate))
                .acquisitions()
                .stream()
                .map(item -> new PaperAcquisitionSnapshot(item.purchaseDate(), item.totalCost()))
                .toList();
    }
}
