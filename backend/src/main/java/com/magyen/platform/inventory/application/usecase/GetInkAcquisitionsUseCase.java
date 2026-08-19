package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInkAcquisitionsQuery;
import com.magyen.platform.inventory.application.dto.GetInkAcquisitionsResult;
import com.magyen.platform.inventory.application.dto.InkAcquisitionItem;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Lista compras de tinta. No crea movimientos ni asientos.
 */
public class GetInkAcquisitionsUseCase {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public GetInkAcquisitionsUseCase(
            InventoryItemRepository inventoryItemRepository,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
        this.inventoryMovementRepository = Objects.requireNonNull(
                inventoryMovementRepository,
                "Inventory movement repository must not be null"
        );
    }

    public GetInkAcquisitionsResult execute(GetInkAcquisitionsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        if (query.fromDate() == null || query.toDate() == null) {
            throw new InventoryDomainException("Both fromDate and toDate must be provided together");
        }
        if (query.fromDate().isAfter(query.toDate())) {
            throw new InventoryDomainException("From date must not be after to date");
        }

        Set<UUID> inkItemIds = new HashSet<>();
        for (InventoryItem item : inventoryItemRepository.findAll()) {
            if (item.getMaterialType() == InventoryMaterialType.INK) {
                inkItemIds.add(item.getId());
            }
        }

        List<InkAcquisitionItem> acquisitions = inventoryMovementRepository
                .findBySourceType(InventoryMovementSourceType.PURCHASE)
                .stream()
                .filter(movement -> inkItemIds.contains(movement.getInventoryItemId()))
                .filter(movement -> inRange(movement.getMovementDate().toLocalDate(), query.fromDate(), query.toDate()))
                .map(GetInkAcquisitionsUseCase::toItem)
                .toList();

        return new GetInkAcquisitionsResult(acquisitions);
    }

    private static boolean inRange(LocalDate purchaseDate, LocalDate fromDate, LocalDate toDate) {
        return !purchaseDate.isBefore(fromDate) && !purchaseDate.isAfter(toDate);
    }

    private static InkAcquisitionItem toItem(InventoryMovement movement) {
        return new InkAcquisitionItem(
                movement.getSourceId(),
                movement.getInventoryItemId(),
                movement.getMovementDate().toLocalDate(),
                movement.getQuantity(),
                movement.getTotalCost()
        );
    }
}
