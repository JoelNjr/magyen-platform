package com.magyen.platform.production.infrastructure.configuration;

import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.inventory.application.usecase.ConsumeInventoryMaterialUseCase;
import com.magyen.platform.production.application.ProductionSnapshotFactory;
import com.magyen.platform.production.application.port.ProductionMaterialConsumptionInventoryPort;
import com.magyen.platform.production.application.usecase.AddProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.AssignProductionOperationOperatorUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.CreateProductionOrderFromOrderUseCase;
import com.magyen.platform.production.application.usecase.GetProductionMaterialConsumptionsUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOrdersUseCase;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionMaterialConsumptionUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.infrastructure.inventory.ProductionMaterialConsumptionInventoryAdapter;
import com.magyen.platform.production.infrastructure.persistence.mapper.ProductionPersistenceMapper;
import com.magyen.platform.production.presentation.productionorder.mapper.ProductionPresentationMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los beans del módulo de producción que no se registran por estereotipos Spring.
 */
@Configuration
public class ProductionConfiguration {

    @Bean
    public ProductionPresentationMapper productionPresentationMapper() {
        return new ProductionPresentationMapper();
    }

    @Bean
    public ProductionPersistenceMapper productionPersistenceMapper() {
        return new ProductionPersistenceMapper();
    }

    @Bean
    public ProductionSnapshotFactory productionSnapshotFactory() {
        return new ProductionSnapshotFactory();
    }

    @Bean
    public CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase(
            GetOrderUseCase getOrderUseCase,
            ProductionSnapshotFactory productionSnapshotFactory,
            ProductionOrderRepository productionOrderRepository
    ) {
        return new CreateProductionOrderFromOrderUseCase(
                getOrderUseCase,
                productionSnapshotFactory,
                productionOrderRepository
        );
    }

    @Bean
    public AddProductionOperationUseCase addProductionOperationUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new AddProductionOperationUseCase(productionOrderRepository);
    }

    @Bean
    public AssignProductionOperationOperatorUseCase assignProductionOperationOperatorUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new AssignProductionOperationOperatorUseCase(productionOrderRepository);
    }

    @Bean
    public StartProductionOperationUseCase startProductionOperationUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new StartProductionOperationUseCase(productionOrderRepository);
    }

    @Bean
    public CompleteProductionOperationUseCase completeProductionOperationUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new CompleteProductionOperationUseCase(productionOrderRepository);
    }

    @Bean
    public PlanProductionOrderUseCase planProductionOrderUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new PlanProductionOrderUseCase(productionOrderRepository);
    }

    @Bean
    public StartProductionOrderUseCase startProductionOrderUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new StartProductionOrderUseCase(productionOrderRepository);
    }

    @Bean
    public CompleteProductionOrderUseCase completeProductionOrderUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new CompleteProductionOrderUseCase(productionOrderRepository);
    }

    @Bean
    public GetProductionOrdersUseCase getProductionOrdersUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new GetProductionOrdersUseCase(productionOrderRepository);
    }

    @Bean
    public GetProductionOrderUseCase getProductionOrderUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new GetProductionOrderUseCase(productionOrderRepository);
    }

    @Bean
    public ProductionMaterialConsumptionInventoryPort productionMaterialConsumptionInventoryPort(
            ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase
    ) {
        return new ProductionMaterialConsumptionInventoryAdapter(consumeInventoryMaterialUseCase);
    }

    @Bean
    public RegisterProductionMaterialConsumptionUseCase registerProductionMaterialConsumptionUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionMaterialConsumptionInventoryPort productionMaterialConsumptionInventoryPort
    ) {
        return new RegisterProductionMaterialConsumptionUseCase(
                productionOrderRepository,
                productionMaterialConsumptionInventoryPort
        );
    }

    @Bean
    public GetProductionMaterialConsumptionsUseCase getProductionMaterialConsumptionsUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new GetProductionMaterialConsumptionsUseCase(productionOrderRepository);
    }
}
