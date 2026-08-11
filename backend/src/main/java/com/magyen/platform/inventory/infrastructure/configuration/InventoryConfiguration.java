package com.magyen.platform.inventory.infrastructure.configuration;

import com.magyen.platform.inventory.application.usecase.ConsumeInventoryMaterialUseCase;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.application.usecase.DecreaseInventoryStockUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemsUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryMovementBySourceUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryMovementsUseCase;
import com.magyen.platform.inventory.application.usecase.IncreaseInventoryStockUseCase;
import com.magyen.platform.inventory.application.usecase.RegisterInventoryMovementUseCase;
import com.magyen.platform.inventory.application.usecase.UpdateInventoryMinimumStockUseCase;
import com.magyen.platform.inventory.application.usecase.UpdateInventoryUnitCostUseCase;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.PaperRollNumberGenerator;
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
    public CreateInventoryItemUseCase createInventoryItemUseCase(
            InventoryItemRepository inventoryItemRepository,
            PaperRollNumberGenerator paperRollNumberGenerator
    ) {
        return new CreateInventoryItemUseCase(inventoryItemRepository, paperRollNumberGenerator);
    }

    @Bean
    public GetInventoryItemsUseCase getInventoryItemsUseCase(InventoryItemRepository inventoryItemRepository) {
        return new GetInventoryItemsUseCase(inventoryItemRepository);
    }

    @Bean
    public GetInventoryItemUseCase getInventoryItemUseCase(InventoryItemRepository inventoryItemRepository) {
        return new GetInventoryItemUseCase(inventoryItemRepository);
    }

    @Bean
    public GetInventoryMovementsUseCase getInventoryMovementsUseCase(
            InventoryItemRepository inventoryItemRepository,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        return new GetInventoryMovementsUseCase(inventoryItemRepository, inventoryMovementRepository);
    }

    @Bean
    public GetInventoryMovementBySourceUseCase getInventoryMovementBySourceUseCase(
            InventoryMovementRepository inventoryMovementRepository
    ) {
        return new GetInventoryMovementBySourceUseCase(inventoryMovementRepository);
    }

    @Bean
    public UpdateInventoryMinimumStockUseCase updateInventoryMinimumStockUseCase(
            InventoryItemRepository inventoryItemRepository
    ) {
        return new UpdateInventoryMinimumStockUseCase(inventoryItemRepository);
    }

    @Bean
    public UpdateInventoryUnitCostUseCase updateInventoryUnitCostUseCase(
            InventoryItemRepository inventoryItemRepository
    ) {
        return new UpdateInventoryUnitCostUseCase(inventoryItemRepository);
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

    @Bean
    public RegisterInventoryMovementUseCase registerInventoryMovementUseCase(
            InventoryItemRepository inventoryItemRepository
    ) {
        return new RegisterInventoryMovementUseCase(inventoryItemRepository);
    }

    @Bean
    public ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase(
            InventoryItemRepository inventoryItemRepository,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        return new ConsumeInventoryMaterialUseCase(inventoryItemRepository, inventoryMovementRepository);
    }
}
