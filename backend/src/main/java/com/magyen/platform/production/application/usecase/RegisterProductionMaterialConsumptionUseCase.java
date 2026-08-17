package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionResult;
import com.magyen.platform.production.application.port.ProductionMaterialConsumptionInventoryPort;
import com.magyen.platform.production.application.port.ProductionMaterialConsumptionInventoryResult;
import com.magyen.platform.production.domain.ProductionMaterialConsumption;
import com.magyen.platform.production.domain.ProductionMaterialUnitOfMeasure;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Caso de uso que registra un consumo real de material en una Orden de Producción
 * y solicita el descuento correspondiente en Inventory.
 * <p>
 * Atomicidad: el consumo de Production y el OUT de Inventory comparten la misma
 * transacción de base de datos. Si Inventory falla, el consumo no queda persistido.
 * <p>
 * Idempotencia de stock: Inventory usa {@code sourceId = consumptionId}, por lo que
 * reintentos del mismo consumo no duplican el OUT.
 */
public class RegisterProductionMaterialConsumptionUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionMaterialConsumptionInventoryPort inventoryPort;

    public RegisterProductionMaterialConsumptionUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionMaterialConsumptionInventoryPort inventoryPort
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.inventoryPort = Objects.requireNonNull(
                inventoryPort,
                "Production material consumption inventory port must not be null"
        );
    }

    @Transactional
    public RegisterProductionMaterialConsumptionResult execute(
            RegisterProductionMaterialConsumptionCommand command
    ) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        ProductionMaterialUnitOfMeasure unitOfMeasure =
                ProductionMaterialUnitOfMeasure.of(command.unitOfMeasure());

        ProductionMaterialConsumption consumption = productionOrder.registerMaterialConsumption(
                command.inventoryItemId(),
                command.quantity(),
                unitOfMeasure,
                command.observation()
        );

        ProductionMaterialConsumptionInventoryResult inventoryResult = inventoryPort.consumeMaterial(
                consumption.getInventoryItemId(),
                consumption.getQuantity(),
                consumption.getUnitOfMeasure().name(),
                consumption.getId(),
                consumption.getObservation()
        );

        productionOrderRepository.save(productionOrder);

        return new RegisterProductionMaterialConsumptionResult(
                consumption.getId(),
                consumption.getProductionOrderId(),
                consumption.getInventoryItemId(),
                inventoryResult.materialName(),
                inventoryResult.materialCode(),
                consumption.getQuantity(),
                consumption.getUnitOfMeasure().name(),
                inventoryResult.unitCost(),
                inventoryResult.totalCost(),
                inventoryResult.resultingStock(),
                consumption.getConsumptionDate(),
                consumption.getObservation()
        );
    }

    private void validateCommand(RegisterProductionMaterialConsumptionCommand command) {
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
        if (command.inventoryItemId() == null) {
            throw new IllegalArgumentException("Inventory item id must not be null");
        }
        Objects.requireNonNull(command.quantity(), "Quantity must not be null");
        Objects.requireNonNull(command.unitOfMeasure(), "Unit of measure must not be null");
    }
}
