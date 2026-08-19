package com.magyen.platform.plotter.infrastructure.configuration;

import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.finance.application.usecase.EnsurePlotterInternalServiceLedgerUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPlotterPaymentIncomeUseCase;
import com.magyen.platform.inventory.application.usecase.ConsumeInventoryMaterialUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryMovementBySourceUseCase;
import com.magyen.platform.inventory.application.usecase.GetPaperAcquisitionsUseCase;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterInternalServiceFinancePort;
import com.magyen.platform.plotter.application.port.PlotterInventoryCostPort;
import com.magyen.platform.plotter.application.port.PlotterJobInventoryPort;
import com.magyen.platform.plotter.application.port.PlotterPaperAcquisitionPort;
import com.magyen.platform.plotter.application.port.PlotterPaymentFinancePort;
import com.magyen.platform.plotter.infrastructure.commercial.PlotterCommercialOrderAdapter;
import com.magyen.platform.plotter.application.usecase.CreatePlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetInternalPlotterOrderCostsUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobsUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterPaymentsUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterProfitabilityUseCase;
import com.magyen.platform.plotter.application.usecase.RegisterPlotterPaymentUseCase;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.infrastructure.finance.PlotterInternalServiceFinanceAdapter;
import com.magyen.platform.plotter.infrastructure.finance.PlotterPaymentFinanceAdapter;
import com.magyen.platform.plotter.infrastructure.inventory.PlotterInventoryCostAdapter;
import com.magyen.platform.plotter.infrastructure.inventory.PlotterJobInventoryAdapter;
import com.magyen.platform.plotter.infrastructure.inventory.PlotterPaperAcquisitionAdapter;
import com.magyen.platform.plotter.infrastructure.persistence.mapper.PlotterPaymentPersistenceMapper;
import com.magyen.platform.plotter.infrastructure.persistence.mapper.PlotterPersistenceMapper;
import com.magyen.platform.plotter.presentation.plotterjob.mapper.PlotterPresentationMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

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
    public PlotterInternalServiceFinancePort plotterInternalServiceFinancePort(
            EnsurePlotterInternalServiceLedgerUseCase ensurePlotterInternalServiceLedgerUseCase
    ) {
        return new PlotterInternalServiceFinanceAdapter(ensurePlotterInternalServiceLedgerUseCase);
    }

    @Bean
    public PlotterCommercialOrderPort plotterCommercialOrderPort(
            GetOrderUseCase getOrderUseCase,
            GetCustomersUseCase getCustomersUseCase
    ) {
        return new PlotterCommercialOrderAdapter(getOrderUseCase, getCustomersUseCase);
    }

    @Bean
    public PlotterInventoryCostPort plotterInventoryCostPort(
            GetInventoryMovementBySourceUseCase getInventoryMovementBySourceUseCase
    ) {
        return new PlotterInventoryCostAdapter(getInventoryMovementBySourceUseCase);
    }

    @Bean
    public PlotterPaperAcquisitionPort plotterPaperAcquisitionPort(
            GetPaperAcquisitionsUseCase getPaperAcquisitionsUseCase
    ) {
        return new PlotterPaperAcquisitionAdapter(getPaperAcquisitionsUseCase);
    }

    @Bean
    public GetInternalPlotterOrderCostsUseCase getInternalPlotterOrderCostsUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterInventoryCostPort plotterInventoryCostPort
    ) {
        return new GetInternalPlotterOrderCostsUseCase(plotterJobRepository, plotterInventoryCostPort);
    }

    @Bean
    public CreatePlotterJobUseCase createPlotterJobUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterJobInventoryPort plotterJobInventoryPort,
            PlotterCommercialOrderPort plotterCommercialOrderPort,
            PlotterInternalServiceFinancePort plotterInternalServiceFinancePort
    ) {
        return new CreatePlotterJobUseCase(
                plotterJobRepository,
                plotterJobInventoryPort,
                plotterCommercialOrderPort,
                plotterInternalServiceFinancePort
        );
    }

    @Bean
    public GetPlotterJobUseCase getPlotterJobUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterCommercialOrderPort plotterCommercialOrderPort
    ) {
        return new GetPlotterJobUseCase(
                plotterJobRepository,
                plotterPaymentRepository,
                plotterCommercialOrderPort
        );
    }

    @Bean
    public GetPlotterJobsUseCase getPlotterJobsUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterCommercialOrderPort plotterCommercialOrderPort
    ) {
        return new GetPlotterJobsUseCase(
                plotterJobRepository,
                plotterPaymentRepository,
                plotterCommercialOrderPort
        );
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

    @Bean
    public GetPlotterProfitabilityUseCase getPlotterProfitabilityUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterInventoryCostPort plotterInventoryCostPort,
            PlotterPaperAcquisitionPort plotterPaperAcquisitionPort,
            PlotterCommercialOrderPort plotterCommercialOrderPort,
            Clock clock
    ) {
        return new GetPlotterProfitabilityUseCase(
                plotterJobRepository,
                plotterInventoryCostPort,
                plotterPaperAcquisitionPort,
                plotterCommercialOrderPort,
                clock
        );
    }
}
