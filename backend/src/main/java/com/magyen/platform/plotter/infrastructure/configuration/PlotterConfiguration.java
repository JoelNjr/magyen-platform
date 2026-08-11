package com.magyen.platform.plotter.infrastructure.configuration;

import com.magyen.platform.finance.application.usecase.RegisterPlotterPaymentIncomeUseCase;
import com.magyen.platform.inventory.application.usecase.ConsumeInventoryMaterialUseCase;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.plotter.application.port.PlotterJobInventoryPort;
import com.magyen.platform.plotter.application.port.PlotterPaymentFinancePort;
import com.magyen.platform.plotter.application.usecase.CreatePlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobsUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterPaymentsUseCase;
import com.magyen.platform.plotter.application.usecase.RegisterPlotterPaymentUseCase;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.infrastructure.finance.PlotterPaymentFinanceAdapter;
import com.magyen.platform.plotter.infrastructure.inventory.PlotterJobInventoryAdapter;
import com.magyen.platform.plotter.infrastructure.persistence.mapper.PlotterPaymentPersistenceMapper;
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
    public PlotterPaymentPersistenceMapper plotterPaymentPersistenceMapper() {
        return new PlotterPaymentPersistenceMapper();
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
    public PlotterPaymentFinancePort plotterPaymentFinancePort(
            RegisterPlotterPaymentIncomeUseCase registerPlotterPaymentIncomeUseCase
    ) {
        return new PlotterPaymentFinanceAdapter(registerPlotterPaymentIncomeUseCase);
    }

    @Bean
    public CreatePlotterJobUseCase createPlotterJobUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterJobInventoryPort plotterJobInventoryPort
    ) {
        return new CreatePlotterJobUseCase(plotterJobRepository, plotterJobInventoryPort);
    }

    @Bean
    public GetPlotterJobUseCase getPlotterJobUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository
    ) {
        return new GetPlotterJobUseCase(plotterJobRepository, plotterPaymentRepository);
    }

    @Bean
    public GetPlotterJobsUseCase getPlotterJobsUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository
    ) {
        return new GetPlotterJobsUseCase(plotterJobRepository, plotterPaymentRepository);
    }

    @Bean
    public RegisterPlotterPaymentUseCase registerPlotterPaymentUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterPaymentFinancePort plotterPaymentFinancePort
    ) {
        return new RegisterPlotterPaymentUseCase(
                plotterJobRepository,
                plotterPaymentRepository,
                plotterPaymentFinancePort
        );
    }

    @Bean
    public GetPlotterPaymentsUseCase getPlotterPaymentsUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository
    ) {
        return new GetPlotterPaymentsUseCase(plotterJobRepository, plotterPaymentRepository);
    }
}
