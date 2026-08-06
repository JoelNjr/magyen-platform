package com.magyen.platform.inventory.presentation.inventoryitem.controller;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.DecreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.DecreaseInventoryStockResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.application.usecase.DecreaseInventoryStockUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemUseCase;
import com.magyen.platform.inventory.application.usecase.IncreaseInventoryStockUseCase;
import com.magyen.platform.inventory.presentation.inventoryitem.mapper.InventoryPresentationMapper;
import com.magyen.platform.inventory.presentation.inventoryitem.request.CreateInventoryItemRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.DecreaseInventoryStockRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.request.IncreaseInventoryStockRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.response.CreateInventoryItemResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.DecreaseInventoryStockResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.GetInventoryItemResponse;
import com.magyen.platform.inventory.presentation.inventoryitem.response.IncreaseInventoryStockResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de inventario.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final CreateInventoryItemUseCase createInventoryItemUseCase;
    private final GetInventoryItemUseCase getInventoryItemUseCase;
    private final IncreaseInventoryStockUseCase increaseInventoryStockUseCase;
    private final DecreaseInventoryStockUseCase decreaseInventoryStockUseCase;
    private final InventoryPresentationMapper inventoryPresentationMapper;

    public InventoryController(
            CreateInventoryItemUseCase createInventoryItemUseCase,
            GetInventoryItemUseCase getInventoryItemUseCase,
            IncreaseInventoryStockUseCase increaseInventoryStockUseCase,
            DecreaseInventoryStockUseCase decreaseInventoryStockUseCase,
            InventoryPresentationMapper inventoryPresentationMapper
    ) {
        this.createInventoryItemUseCase = createInventoryItemUseCase;
        this.getInventoryItemUseCase = getInventoryItemUseCase;
        this.increaseInventoryStockUseCase = increaseInventoryStockUseCase;
        this.decreaseInventoryStockUseCase = decreaseInventoryStockUseCase;
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

    @GetMapping("/{inventoryItemId}")
    public ResponseEntity<GetInventoryItemResponse> getInventoryItem(
            @PathVariable UUID inventoryItemId
    ) {
        GetInventoryItemQuery query = inventoryPresentationMapper.toQuery(inventoryItemId);
        GetInventoryItemResult result = getInventoryItemUseCase.execute(query);
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
}
