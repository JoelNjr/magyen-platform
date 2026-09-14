package com.magyen.platform.inventory.presentation.inventoryitem.mapper;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.DeactivateInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.DeactivateInventoryMaterialResult;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.DecreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.DecreaseInventoryStockResult;
import com.magyen.platform.inventory.application.dto.GetInventoryCatalogResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemsQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemsResult;
import com.magyen.platform.inventory.application.dto.GetInventoryMaterialQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMaterialResult;
import com.magyen.platform.inventory.application.dto.InventoryCatalogItemResult;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementResult;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementsQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementsResult;
import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockResult;
import com.magyen.platform.inventory.application.dto.InventoryAcquisitionCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementResult;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseResult;
import com.magyen.platform.inventory.application.dto.UpdateInventoryMinimumStockCommand;
import com.magyen.platform.inventory.application.dto.UpdateInventoryUnitCostCommand;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import com.magyen.platform.inventory.presentation.inventoryitem.request.CreateInventoryItemRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.DecreaseInventoryStockRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.IncreaseInventoryStockRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.InventoryAcquisitionRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.RegisterInventoryMovementRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.RegisterInventoryPurchaseRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.UpdateInventoryMinimumStockRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.UpdateInventoryUnitCostRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.response.CreateInventoryItemResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.DeactivateInventoryMaterialResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.DecreaseInventoryStockResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryCatalogItemResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryCatalogResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryItemResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryItemsResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryMaterialResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryMovementResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryMovementsResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.IncreaseInventoryStockResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.RegisterInventoryMovementResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.RegisterInventoryPurchaseResponse;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
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
                request.minimumStock(),
                request.description(),
                request.unitCost(),
                request.materialType(),
                Boolean.TRUE.equals(request.plotterPaperRoll()),
                toAcquisitionCommand(request.acquisition())
        );
    }

    private InventoryAcquisitionCommand toAcquisitionCommand(InventoryAcquisitionRequest request) {
        if (request == null) {
            return null;
        }
        return new InventoryAcquisitionCommand(
                request.purchaseId(),
                request.quantity(),
                request.unitCost(),
                request.totalCost(),
                request.purchaseDate(),
                request.observation()
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
                result.status().name(),
                result.description(),
                result.lowStock(),
                result.unitCost(),
                result.materialType().name(),
                result.paperRollNumber(),
                result.plotterPaperRoll()
        );
    }

    public GetInventoryItemQuery toQuery(UUID inventoryItemId) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");

        return new GetInventoryItemQuery(inventoryItemId);
    }

    public GetInventoryItemsQuery toItemsQuery(String materialType, Boolean plotterPaperRoll) {
        return new GetInventoryItemsQuery(materialType, plotterPaperRoll);
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
                result.status().name(),
                result.description(),
                result.lowStock(),
                result.unitCost(),
                result.materialType().name(),
                result.paperRollNumber(),
                result.plotterPaperRoll()
        );
    }

    public GetInventoryItemsResponse toResponse(GetInventoryItemsResult result) {
        Objects.requireNonNull(result, "GetInventoryItemsResult must not be null");

        return new GetInventoryItemsResponse(
                result.items().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    public DeactivateInventoryMaterialCommand toDeactivateMaterialCommand(String materialCode) {
        return new DeactivateInventoryMaterialCommand(materialCode);
    }

    public DeactivateInventoryMaterialResponse toResponse(DeactivateInventoryMaterialResult result) {
        Objects.requireNonNull(result, "Deactivate inventory material result must not be null");
        return new DeactivateInventoryMaterialResponse(
                result.materialCode(),
                result.status().name(),
                result.deactivatedUnitCount(),
                result.aggregatedStock()
        );
    }

    public GetInventoryMaterialQuery toMaterialQuery(String materialCode) {
        if (materialCode == null || materialCode.isBlank()) {
            throw new InventoryDomainException("Material code must not be blank");
        }
        return new GetInventoryMaterialQuery(materialCode.trim());
    }

    public GetInventoryCatalogItemResponse toCatalogResponse(InventoryCatalogItemResult result) {
        Objects.requireNonNull(result, "Inventory catalog item result must not be null");

        return new GetInventoryCatalogItemResponse(
                result.materialCode(),
                result.name(),
                result.category(),
                result.materialType().name(),
                result.unitOfMeasure(),
                result.description(),
                result.aggregatedStock(),
                result.minimumStock(),
                result.minimumStockUniform(),
                result.lowStock(),
                result.status().name(),
                result.unitCost(),
                result.unitCostUniform(),
                result.physicalUnitCount(),
                result.paperMaterial(),
                result.stockHoldingItemId()
        );
    }

    public GetInventoryCatalogResponse toResponse(GetInventoryCatalogResult result) {
        Objects.requireNonNull(result, "GetInventoryCatalogResult must not be null");

        return new GetInventoryCatalogResponse(
                result.materials().stream()
                        .map(this::toCatalogResponse)
                        .toList()
        );
    }

    public GetInventoryMaterialResponse toResponse(GetInventoryMaterialResult result) {
        Objects.requireNonNull(result, "GetInventoryMaterialResult must not be null");

        return new GetInventoryMaterialResponse(
                toCatalogResponse(result.material()),
                result.units().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    public GetInventoryMovementsQuery toMovementsQuery(UUID inventoryItemId) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");

        return new GetInventoryMovementsQuery(inventoryItemId);
    }

    public GetInventoryMovementsResponse toResponse(GetInventoryMovementsResult result) {
        Objects.requireNonNull(result, "GetInventoryMovementsResult must not be null");

        return new GetInventoryMovementsResponse(
                result.movements().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    public GetInventoryMovementResponse toResponse(GetInventoryMovementResult result) {
        Objects.requireNonNull(result, "GetInventoryMovementResult must not be null");

        return new GetInventoryMovementResponse(
                result.movementId(),
                result.inventoryItemId(),
                result.movementType().name(),
                result.quantity(),
                result.unitOfMeasure(),
                result.movementDate(),
                result.observation(),
                result.resultingStock(),
                result.unitCost(),
                result.totalCost(),
                result.sourceType().name(),
                result.sourceId()
        );
    }

    public UpdateInventoryMinimumStockCommand toUpdateMinimumStockCommand(
            UUID inventoryItemId,
            UpdateInventoryMinimumStockRequest request
    ) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");
        Objects.requireNonNull(request, "UpdateInventoryMinimumStockRequest must not be null");

        return new UpdateInventoryMinimumStockCommand(inventoryItemId, request.minimumStock());
    }

    public UpdateInventoryUnitCostCommand toUpdateUnitCostCommand(
            UUID inventoryItemId,
            UpdateInventoryUnitCostRequest request
    ) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");
        Objects.requireNonNull(request, "UpdateInventoryUnitCostRequest must not be null");

        return new UpdateInventoryUnitCostCommand(inventoryItemId, request.unitCost());
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

    public RegisterInventoryMovementCommand toRegisterMovementCommand(
            UUID inventoryItemId,
            RegisterInventoryMovementRequest request
    ) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");
        Objects.requireNonNull(request, "RegisterInventoryMovementRequest must not be null");

        return new RegisterInventoryMovementCommand(
                inventoryItemId,
                parseMovementType(request.movementType()),
                request.quantity(),
                request.unitOfMeasure(),
                request.observation(),
                parseSourceType(request.sourceType()),
                request.sourceId()
        );
    }

    public RegisterInventoryMovementResponse toResponse(RegisterInventoryMovementResult result) {
        Objects.requireNonNull(result, "RegisterInventoryMovementResult must not be null");

        return new RegisterInventoryMovementResponse(
                result.movementId(),
                result.inventoryItemId(),
                result.movementType().name(),
                result.quantity(),
                result.unitOfMeasure(),
                result.resultingStock(),
                result.movementDate(),
                result.observation(),
                result.unitCost(),
                result.totalCost(),
                result.sourceType().name(),
                result.sourceId()
        );
    }

    public RegisterInventoryPurchaseCommand toRegisterPurchaseCommand(
            UUID inventoryItemId,
            RegisterInventoryPurchaseRequest request
    ) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");
        Objects.requireNonNull(request, "RegisterInventoryPurchaseRequest must not be null");

        return new RegisterInventoryPurchaseCommand(
                inventoryItemId,
                request.purchaseId(),
                request.quantity(),
                request.unitCost(),
                request.purchaseDate(),
                request.observation()
        );
    }

    public RegisterInventoryPurchaseResponse toResponse(RegisterInventoryPurchaseResult result) {
        Objects.requireNonNull(result, "RegisterInventoryPurchaseResult must not be null");

        return new RegisterInventoryPurchaseResponse(
                result.purchaseId(),
                result.inventoryItemId(),
                result.materialName(),
                result.materialCode(),
                result.movementId(),
                result.financialTransactionId(),
                result.quantity(),
                result.unitOfMeasure(),
                result.unitCost(),
                result.totalCost(),
                result.resultingStock(),
                result.purchaseDate(),
                result.movementDate(),
                result.observation(),
                result.financeCategory(),
                result.alreadyProcessed()
        );
    }

    private InventoryMovementType parseMovementType(String movementType) {
        if (movementType == null || movementType.isBlank()) {
            throw new InventoryDomainException("Movement type must not be blank");
        }

        try {
            return InventoryMovementType.valueOf(movementType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InventoryDomainException("Invalid movement type: " + movementType);
        }
    }

    /**
     * Omite o deja en blanco {@code sourceType} → {@link InventoryMovementSourceType#MANUAL}.
     * Los clientes legacy del UI de Inventory siguen funcionando sin enviar el campo.
     */
    private InventoryMovementSourceType parseSourceType(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) {
            return InventoryMovementSourceType.MANUAL;
        }
        return InventoryMovementSourceType.of(sourceType);
    }
}
