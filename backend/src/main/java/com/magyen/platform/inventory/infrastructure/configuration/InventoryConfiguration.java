package com.magyen.platform.inventory.infrastructure.configuration;

import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.application.usecase.DecreaseInventoryStockUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemUseCase;
import com.magyen.platform.inventory.application.usecase.IncreaseInventoryStockUseCase;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.infrastructure.persistence.mapper.InventoryPersistenceMapper;
import com.magyen.platform.inventory.presentation.inventoryitem.mapper.InventoryPresentationMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los beans del módulo de inventario que no se registran por estereotipos Spring.
 */
@Configuration
public class InventoryConfiguration {

    @Bean
    public InventoryPresentationMapper inventoryPresentationMapper() {
        return new InventoryPresentationMapper();
    }

    @Bean
    public InventoryPersistenceMapper inventoryPersistenceMapper() {
        return new InventoryPersistenceMapper();
    }

    @Bean
    public CreateInventoryItemUseCase createInventoryItemUseCase(InventoryItemRepository inventoryItemRepository) {
        return new CreateInventoryItemUseCase(inventoryItemRepository);
    }

    @Bean
    public GetInventoryItemUseCase getInventoryItemUseCase(InventoryItemRepository inventoryItemRepository) {
        return new GetInventoryItemUseCase(inventoryItemRepository);
    }

    @Bean
    public IncreaseInventoryStockUseCase increaseInventoryStockUseCase(
            InventoryItemRepository inventoryItemRepository
    ) {
        return new IncreaseInventoryStockUseCase(inventoryItemRepository);
    }

    @Bean
    public DecreaseInventoryStockUseCase decreaseInventoryStockUseCase(
            InventoryItemRepository inventoryItemRepository
    ) {
        return new DecreaseInventoryStockUseCase(inventoryItemRepository);
    }
}
