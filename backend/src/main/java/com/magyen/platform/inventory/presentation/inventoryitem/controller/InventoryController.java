package com.magyen.platform.inventory.presentation.inventoryitem.controller;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.DecreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.DecreaseInventoryStockResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemsResult;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementsQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementsResult;
import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockResult;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementResult;
import com.magyen.platform.inventory.application.dto.UpdateInventoryMinimumStockCommand;
import com.magyen.platform.inventory.application.dto.UpdateInventoryUnitCostCommand;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.application.usecase.DecreaseInventoryStockUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemsUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryMovementsUseCase;
import com.magyen.platform.inventory.application.usecase.IncreaseInventoryStockUseCase;
import com.magyen.platform.inventory.application.usecase.RegisterInventoryMovementUseCase;
import com.magyen.platform.inventory.application.usecase.UpdateInventoryMinimumStockUseCase;
import com.magyen.platform.inventory.application.usecase.UpdateInventoryUnitCostUseCase;
import com.magyen.platform.inventory.presentation.inventoryitem.mapper.InventoryPresentationMapper;
import com.magyen.platform.inventory.presentation.inventoryitem.request.CreateInventoryItemRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.DecreaseInventoryStockRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.IncreaseInventoryStockRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.RegisterInventoryMovementRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.UpdateInventoryMinimumStockRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.UpdateInventoryUnitCostRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.response.CreateInventoryItemResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.DecreaseInventoryStockResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryItemResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryItemsResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryMovementsResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.IncreaseInventoryStockResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.RegisterInventoryMovementResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de inventario.
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final CreateInventoryItemUseCase createInventoryItemUseCase;
    private final GetInventoryItemsUseCase getInventoryItemsUseCase;
    private final GetInventoryItemUseCase getInventoryItemUseCase;
    private final GetInventoryMovementsUseCase getInventoryMovementsUseCase;
    private final UpdateInventoryMinimumStockUseCase updateInventoryMinimumStockUseCase;
    private final UpdateInventoryUnitCostUseCase updateInventoryUnitCostUseCase;
    private final IncreaseInventoryStockUseCase increaseInventoryStockUseCase;
    private final DecreaseInventoryStockUseCase decreaseInventoryStockUseCase;
    private final RegisterInventoryMovementUseCase registerInventoryMovementUseCase;
    private final InventoryPresentationMapper inventoryPresentationMapper;

    public InventoryController(
            CreateInventoryItemUseCase createInventoryItemUseCase,
            GetInventoryItemsUseCase getInventoryItemsUseCase,
            GetInventoryItemUseCase getInventoryItemUseCase,
            GetInventoryMovementsUseCase getInventoryMovementsUseCase,
            UpdateInventoryMinimumStockUseCase updateInventoryMinimumStockUseCase,
            UpdateInventoryUnitCostUseCase updateInventoryUnitCostUseCase,
            IncreaseInventoryStockUseCase increaseInventoryStockUseCase,
            DecreaseInventoryStockUseCase decreaseInventoryStockUseCase,
            RegisterInventoryMovementUseCase registerInventoryMovementUseCase,
            InventoryPresentationMapper inventoryPresentationMapper
    ) {
        this.createInventoryItemUseCase = createInventoryItemUseCase;
        this.getInventoryItemsUseCase = getInventoryItemsUseCase;
        this.getInventoryItemUseCase = getInventoryItemUseCase;
        this.getInventoryMovementsUseCase = getInventoryMovementsUseCase;
        this.updateInventoryMinimumStockUseCase = updateInventoryMinimumStockUseCase;
        this.updateInventoryUnitCostUseCase = updateInventoryUnitCostUseCase;
        this.increaseInventoryStockUseCase = increaseInventoryStockUseCase;
        this.decreaseInventoryStockUseCase = decreaseInventoryStockUseCase;
        this.registerInventoryMovementUseCase = registerInventoryMovementUseCase;
        this.inventoryPresentationMapper = inventoryPresentationMapper;
    }

    @PostMapping
    public ResponseEntity<CreateInventoryItemResponse> createInventoryItem(
            @RequestBody CreateInventoryItemRequest request
    ) {
        CreateInventoryItemCommand command = inventoryPresentationMapper.toCommand(request);
        CreateInventoryItemResult result = createInventoryItemUseCase.execute(command);
        CreateInventoryItemResponse response = inventoryPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<GetInventoryItemsResponse> getInventoryItems(
            @RequestParam(required = false) String materialType,
            @RequestParam(required = false) Boolean plotterPaperRoll
    ) {
        GetInventoryItemsResult result = getInventoryItemsUseCase.execute(
                inventoryPresentationMapper.toItemsQuery(materialType, plotterPaperRoll)
        );
        GetInventoryItemsResponse response = inventoryPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{inventoryItemId}")
    public ResponseEntity<GetInventoryItemResponse> getInventoryItem(
            @PathVariable UUID inventoryItemId
    ) {
        GetInventoryItemQuery query = inventoryPresentationMapper.toQuery(inventoryItemId);
        GetInventoryItemResult result = getInventoryItemUseCase.execute(query);
        GetInventoryItemResponse response = inventoryPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{inventoryItemId}/movements")
    public ResponseEntity<GetInventoryMovementsResponse> getInventoryMovements(
            @PathVariable UUID inventoryItemId
    ) {
        GetInventoryMovementsQuery query = inventoryPresentationMapper.toMovementsQuery(inventoryItemId);
        GetInventoryMovementsResult result = getInventoryMovementsUseCase.execute(query);
        GetInventoryMovementsResponse response = inventoryPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{inventoryItemId}/minimum-stock")
    public ResponseEntity<GetInventoryItemResponse> updateMinimumStock(
            @PathVariable UUID inventoryItemId,
            @RequestBody UpdateInventoryMinimumStockRequest request
    ) {
        UpdateInventoryMinimumStockCommand command = inventoryPresentationMapper.toUpdateMinimumStockCommand(
                inventoryItemId,
                request
        );
        GetInventoryItemResult result = updateInventoryMinimumStockUseCase.execute(command);
        GetInventoryItemResponse response = inventoryPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{inventoryItemId}/unit-cost")
    public ResponseEntity<GetInventoryItemResponse> updateUnitCost(
            @PathVariable UUID inventoryItemId,
            @RequestBody UpdateInventoryUnitCostRequest request
    ) {
        UpdateInventoryUnitCostCommand command = inventoryPresentationMapper.toUpdateUnitCostCommand(
                inventoryItemId,
                request
        );
        GetInventoryItemResult result = updateInventoryUnitCostUseCase.execute(command);
        GetInventoryItemResponse response = inventoryPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{inventoryItemId}/increase-stock")
    public ResponseEntity<IncreaseInventoryStockResponse> increaseInventoryStock(
            @PathVariable UUID inventoryItemId,
            @RequestBody IncreaseInventoryStockRequest request
    ) {
        IncreaseInventoryStockCommand command = inventoryPresentationMapper.toIncreaseStockCommand(
                inventoryItemId,
                request
        );
        IncreaseInventoryStockResult result = increaseInventoryStockUseCase.execute(command);
        IncreaseInventoryStockResponse response = inventoryPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{inventoryItemId}/decrease-stock")
    public ResponseEntity<DecreaseInventoryStockResponse> decreaseInventoryStock(
            @PathVariable UUID inventoryItemId,
            @RequestBody DecreaseInventoryStockRequest request
    ) {
        DecreaseInventoryStockCommand command = inventoryPresentationMapper.toDecreaseStockCommand(
                inventoryItemId,
                request
        );
        DecreaseInventoryStockResult result = decreaseInventoryStockUseCase.execute(command);
        DecreaseInventoryStockResponse response = inventoryPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{inventoryItemId}/movements")
    public ResponseEntity<RegisterInventoryMovementResponse> registerInventoryMovement(
            @PathVariable UUID inventoryItemId,
            @RequestBody RegisterInventoryMovementRequest request
    ) {
        RegisterInventoryMovementCommand command = inventoryPresentationMapper.toRegisterMovementCommand(
                inventoryItemId,
                request
        );
        RegisterInventoryMovementResult result = registerInventoryMovementUseCase.execute(command);
        RegisterInventoryMovementResponse response = inventoryPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
