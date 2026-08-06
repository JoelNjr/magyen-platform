package com.magyen.platform.inventory.presentation.inventoryitem.mapper;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.DecreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.DecreaseInventoryStockResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockResult;
import com.magyen.platform.inventory.presentation.inventoryitem.request.CreateInventoryItemRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.DecreaseInventoryStockRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.IncreaseInventoryStockRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.response.CreateInventoryItemResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.DecreaseInventoryStockResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryItemResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.IncreaseInventoryStockResponse;

import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class InventoryPresentationMapper {

    public CreateInventoryItemCommand toCommand(CreateInventoryItemRequest request) {
        Objects.requireNonNull(request, "CreateInventoryItemRequest must not be null");

        return new CreateInventoryItemCommand(
                request.code(),
                request.name(),
                request.category(),
                request.unitOfMeasure(),
                request.stock(),
                request.minimumStock()
        );
    }

    public CreateInventoryItemResponse toResponse(CreateInventoryItemResult result) {
        Objects.requireNonNull(result, "CreateInventoryItemResult must not be null");

        return new CreateInventoryItemResponse(
                result.inventoryItemId(),
                result.materialCode(),
                result.name(),
                result.category(),
                result.unitOfMeasure(),
                result.stock(),
                result.minimumStock(),
                result.status().name()
        );
    }

    public GetInventoryItemQuery toQuery(UUID inventoryItemId) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");

        return new GetInventoryItemQuery(inventoryItemId);
    }

    public GetInventoryItemResponse toResponse(GetInventoryItemResult result) {
        Objects.requireNonNull(result, "GetInventoryItemResult must not be null");

        return new GetInventoryItemResponse(
                result.inventoryItemId(),
                result.materialCode(),
                result.name(),
                result.category(),
                result.unitOfMeasure(),
                result.stock(),
                result.minimumStock(),
                result.status().name()
        );
    }

    public IncreaseInventoryStockCommand toIncreaseStockCommand(
            UUID inventoryItemId,
            IncreaseInventoryStockRequest request
    ) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");
        Objects.requireNonNull(request, "IncreaseInventoryStockRequest must not be null");

        return new IncreaseInventoryStockCommand(inventoryItemId, request.quantity());
    }

    public IncreaseInventoryStockResponse toResponse(IncreaseInventoryStockResult result) {
        Objects.requireNonNull(result, "IncreaseInventoryStockResult must not be null");

        return new IncreaseInventoryStockResponse(
                result.inventoryItemId(),
                result.stock(),
                result.status().name()
        );
    }

    public DecreaseInventoryStockCommand toDecreaseStockCommand(
            UUID inventoryItemId,
            DecreaseInventoryStockRequest request
    ) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");
        Objects.requireNonNull(request, "DecreaseInventoryStockRequest must not be null");

        return new DecreaseInventoryStockCommand(inventoryItemId, request.quantity());
    }

    public DecreaseInventoryStockResponse toResponse(DecreaseInventoryStockResult result) {
        Objects.requireNonNull(result, "DecreaseInventoryStockResult must not be null");

        return new DecreaseInventoryStockResponse(
                result.inventoryItemId(),
                result.stock(),
                result.status().name()
        );
    }
}
