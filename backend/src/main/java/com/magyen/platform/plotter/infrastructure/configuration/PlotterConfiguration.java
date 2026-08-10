package com.magyen.platform.plotter.infrastructure.configuration;

import com.magyen.platform.inventory.application.usecase.ConsumeInventoryMaterialUseCase;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.plotter.application.port.PlotterJobInventoryPort;
import com.magyen.platform.plotter.application.usecase.CreatePlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobsUseCase;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.infrastructure.inventory.PlotterJobInventoryAdapter;
import com.magyen.platform.plotter.infrastructure.persistence.mapper.PlotterPersistenceMapper;
import com.magyen.platform.plotter.presentation.plotterjob.mapper.PlotterPresentationMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los beans del módulo de Plotter que no se registran por estereotipos Spring.
 */
@Configuration
public class PlotterConfiguration {

    @Bean
    public PlotterPresentationMapper plotterPresentationMapper() {
        return new PlotterPresentationMapper();
    }

    @Bean
    public PlotterPersistenceMapper plotterPersistenceMapper() {
        return new PlotterPersistenceMapper();
    }

    @Bean
    public PlotterJobInventoryPort plotterJobInventoryPort(
            InventoryItemRepository inventoryItemRepository,
            ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase
    ) {
        return new PlotterJobInventoryAdapter(
                inventoryItemRepository,
                consumeInventoryMaterialUseCase
        );
    }

    @Bean
    public CreatePlotterJobUseCase createPlotterJobUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterJobInventoryPort plotterJobInventoryPort
    ) {
        return new CreatePlotterJobUseCase(plotterJobRepository, plotterJobInventoryPort);
    }

    @Bean
    public GetPlotterJobUseCase getPlotterJobUseCase(PlotterJobRepository plotterJobRepository) {
        return new GetPlotterJobUseCase(plotterJobRepository);
    }

    @Bean
    public GetPlotterJobsUseCase getPlotterJobsUseCase(PlotterJobRepository plotterJobRepository) {
        return new GetPlotterJobsUseCase(plotterJobRepository);
    }
}
