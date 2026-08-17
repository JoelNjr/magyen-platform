package com.magyen.platform.production.infrastructure.configuration;

import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.commercial.application.usecase.GetQuotationUseCase;
import com.magyen.platform.finance.application.usecase.RegisterProductionLaborPaymentExpenseUseCase;
import com.magyen.platform.inventory.application.usecase.ConsumeInventoryMaterialUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryMovementBySourceUseCase;
import com.magyen.platform.production.application.CommercialOrderIdentityResolver;
import com.magyen.platform.production.application.ProductionSnapshotFactory;
import com.magyen.platform.production.application.port.ProductionCommercialChronologyPort;
import com.magyen.platform.production.application.port.ProductionLaborEmployeePort;
import com.magyen.platform.production.application.port.ProductionLaborFinancePort;
import com.magyen.platform.production.application.port.ProductionMaterialConsumptionInventoryPort;
import com.magyen.platform.production.application.port.ProductionMaterialCostInventoryPort;
import com.magyen.platform.production.application.usecase.AddProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.AssignProductionOperationOperatorUseCase;
import com.magyen.platform.production.application.usecase.CancelProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.CreateProductionOrderFromOrderUseCase;
import com.magyen.platform.production.application.usecase.GetProductionCostsByCommercialOrderUseCase;
import com.magyen.platform.production.application.usecase.GetProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.GetProductionLaborWorksUseCase;
import com.magyen.platform.production.application.usecase.GetProductionMaterialConsumptionsUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.CreateProductionOperatorUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOperatorsUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOrdersUseCase;
import com.magyen.platform.production.application.usecase.ListEligibleProductionLaborOperatorsUseCase;
import com.magyen.platform.production.application.usecase.PayProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionMaterialConsumptionUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.domain.ProductionOperatorRepository;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.infrastructure.commercial.ProductionCommercialChronologyAdapter;
import com.magyen.platform.production.infrastructure.finance.ProductionLaborFinanceAdapter;
import com.magyen.platform.production.infrastructure.persistence.ProductionLaborOperatorAdapter;
import com.magyen.platform.production.infrastructure.persistence.mapper.ProductionOperatorPersistenceMapper;
import com.magyen.platform.production.presentation.operator.mapper.ProductionOperatorPresentationMapper;
import com.magyen.platform.production.infrastructure.inventory.ProductionMaterialConsumptionInventoryAdapter;
import com.magyen.platform.production.infrastructure.inventory.ProductionMaterialCostInventoryAdapter;
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
    public ProductionOperatorPersistenceMapper productionOperatorPersistenceMapper() {
        return new ProductionOperatorPersistenceMapper();
    }

    @Bean
    public ProductionOperatorPresentationMapper productionOperatorPresentationMapper() {
        return new ProductionOperatorPresentationMapper();
    }

    @Bean
    public CreateProductionOperatorUseCase createProductionOperatorUseCase(
            ProductionOperatorRepository productionOperatorRepository
    ) {
        return new CreateProductionOperatorUseCase(productionOperatorRepository);
    }

    @Bean
    public GetProductionOperatorsUseCase getProductionOperatorsUseCase(
            ProductionOperatorRepository productionOperatorRepository
    ) {
        return new GetProductionOperatorsUseCase(productionOperatorRepository);
    }

    @Bean
    public ProductionCommercialChronologyPort productionCommercialChronologyPort(
            GetOrderUseCase getOrderUseCase,
            GetQuotationUseCase getQuotationUseCase
    ) {
        return new ProductionCommercialChronologyAdapter(getOrderUseCase, getQuotationUseCase);
    }

    @Bean
    public ProductionSnapshotFactory productionSnapshotFactory() {
        return new ProductionSnapshotFactory();
    }

    @Bean
    public CommercialOrderIdentityResolver commercialOrderIdentityResolver(
            GetOrdersUseCase getOrdersUseCase,
            GetCustomersUseCase getCustomersUseCase
    ) {
        return new CommercialOrderIdentityResolver(getOrdersUseCase, getCustomersUseCase);
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
            ProductionOrderRepository productionOrderRepository,
            ProductionCommercialChronologyPort productionCommercialChronologyPort
    ) {
        return new PlanProductionOrderUseCase(
                productionOrderRepository,
                productionCommercialChronologyPort
        );
    }

    @Bean
    public StartProductionOrderUseCase startProductionOrderUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionCommercialChronologyPort productionCommercialChronologyPort
    ) {
        return new StartProductionOrderUseCase(
                productionOrderRepository,
                productionCommercialChronologyPort
        );
    }

    @Bean
    public CompleteProductionOrderUseCase completeProductionOrderUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionCommercialChronologyPort productionCommercialChronologyPort
    ) {
        return new CompleteProductionOrderUseCase(
                productionOrderRepository,
                productionCommercialChronologyPort
        );
    }

    @Bean
    public GetProductionOrdersUseCase getProductionOrdersUseCase(
            ProductionOrderRepository productionOrderRepository,
            CommercialOrderIdentityResolver commercialOrderIdentityResolver
    ) {
        return new GetProductionOrdersUseCase(
                productionOrderRepository,
                commercialOrderIdentityResolver
        );
    }

    @Bean
    public GetProductionOrderUseCase getProductionOrderUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionMaterialCostInventoryPort productionMaterialCostInventoryPort,
            CommercialOrderIdentityResolver commercialOrderIdentityResolver
    ) {
        return new GetProductionOrderUseCase(
                productionOrderRepository,
                productionMaterialCostInventoryPort,
                commercialOrderIdentityResolver
        );
    }

    @Bean
    public ProductionMaterialConsumptionInventoryPort productionMaterialConsumptionInventoryPort(
            ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase
    ) {
        return new ProductionMaterialConsumptionInventoryAdapter(consumeInventoryMaterialUseCase);
    }

    @Bean
    public ProductionMaterialCostInventoryPort productionMaterialCostInventoryPort(
            GetInventoryMovementBySourceUseCase getInventoryMovementBySourceUseCase
    ) {
        return new ProductionMaterialCostInventoryAdapter(getInventoryMovementBySourceUseCase);
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
            ProductionOrderRepository productionOrderRepository,
            ProductionMaterialCostInventoryPort productionMaterialCostInventoryPort
    ) {
        return new GetProductionMaterialConsumptionsUseCase(
                productionOrderRepository,
                productionMaterialCostInventoryPort
        );
    }

    @Bean
    public GetProductionCostsByCommercialOrderUseCase getProductionCostsByCommercialOrderUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionMaterialCostInventoryPort productionMaterialCostInventoryPort
    ) {
        return new GetProductionCostsByCommercialOrderUseCase(
                productionOrderRepository,
                productionMaterialCostInventoryPort
        );
    }

    @Bean
    public ProductionLaborEmployeePort productionLaborEmployeePort(
            ProductionOperatorRepository productionOperatorRepository
    ) {
        return new ProductionLaborOperatorAdapter(productionOperatorRepository);
    }

    @Bean
    public ProductionLaborFinancePort productionLaborFinancePort(
            RegisterProductionLaborPaymentExpenseUseCase registerProductionLaborPaymentExpenseUseCase
    ) {
        return new ProductionLaborFinanceAdapter(registerProductionLaborPaymentExpenseUseCase);
    }

    @Bean
    public RegisterProductionLaborWorkUseCase registerProductionLaborWorkUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionLaborEmployeePort productionLaborEmployeePort
    ) {
        return new RegisterProductionLaborWorkUseCase(
                productionOrderRepository,
                productionLaborEmployeePort
        );
    }

    @Bean
    public GetProductionLaborWorksUseCase getProductionLaborWorksUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionLaborEmployeePort productionLaborEmployeePort
    ) {
        return new GetProductionLaborWorksUseCase(
                productionOrderRepository,
                productionLaborEmployeePort
        );
    }

    @Bean
    public GetProductionLaborWorkUseCase getProductionLaborWorkUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionLaborEmployeePort productionLaborEmployeePort
    ) {
        return new GetProductionLaborWorkUseCase(
                productionOrderRepository,
                productionLaborEmployeePort
        );
    }

    @Bean
    public PayProductionLaborWorkUseCase payProductionLaborWorkUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionLaborFinancePort productionLaborFinancePort,
            ProductionLaborEmployeePort productionLaborEmployeePort
    ) {
        return new PayProductionLaborWorkUseCase(
                productionOrderRepository,
                productionLaborFinancePort,
                productionLaborEmployeePort
        );
    }

    @Bean
    public CancelProductionLaborWorkUseCase cancelProductionLaborWorkUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new CancelProductionLaborWorkUseCase(productionOrderRepository);
    }

    @Bean
    public ListEligibleProductionLaborOperatorsUseCase listEligibleProductionLaborOperatorsUseCase(
            ProductionLaborEmployeePort productionLaborEmployeePort
    ) {
        return new ListEligibleProductionLaborOperatorsUseCase(productionLaborEmployeePort);
    }
}
