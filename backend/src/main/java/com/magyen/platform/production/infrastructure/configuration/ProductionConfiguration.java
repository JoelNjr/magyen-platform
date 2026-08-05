package com.magyen.platform.production.infrastructure.configuration;

import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.production.application.usecase.CreateProductionOrderFromOrderUseCase;
import com.magyen.platform.production.domain.ProductionOrderRepository;
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
    public CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase(
            OrderRepository orderRepository,
            ProductionOrderRepository productionOrderRepository
    ) {
        return new CreateProductionOrderFromOrderUseCase(orderRepository, productionOrderRepository);
    }
}
