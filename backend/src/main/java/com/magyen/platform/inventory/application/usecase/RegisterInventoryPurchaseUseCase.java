package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseResult;
import com.magyen.platform.inventory.application.port.InventoryPurchaseFinancePort;
import com.magyen.platform.inventory.application.port.InventoryPurchaseFinancePort.InventoryPurchaseFinanceRecord;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Registra una compra/recepción de material.
 * <p>
 * En la misma transacción: Inventory IN + un único Finance EXPENSE.
 * El consumo posterior no crea otro gasto: el desembolso ya se reconoció aquí.
 */
public class RegisterInventoryPurchaseUseCase {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryPurchaseFinancePort inventoryPurchaseFinancePort;

    public RegisterInventoryPurchaseUseCase(
            InventoryItemRepository inventoryItemRepository,
            InventoryMovementRepository inventoryMovementRepository,
            InventoryPurchaseFinancePort inventoryPurchaseFinancePort
    ) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
        this.inventoryMovementRepository = Objects.requireNonNull(
                inventoryMovementRepository,
                "Inventory movement repository must not be null"
        );
        this.inventoryPurchaseFinancePort = Objects.requireNonNull(
                inventoryPurchaseFinancePort,
                "Inventory purchase finance port must not be null"
        );
    }

    @Transactional
    public RegisterInventoryPurchaseResult execute(RegisterInventoryPurchaseCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        UUID purchaseId = command.purchaseId() == null ? UUID.randomUUID() : command.purchaseId();

        return inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PURCHASE, purchaseId)
                .map(existing -> completeExistingPurchase(existing, command, purchaseId))
                .orElseGet(() -> createPurchase(command, purchaseId));
    }

    private RegisterInventoryPurchaseResult createPurchase(
            RegisterInventoryPurchaseCommand command,
            UUID purchaseId
    ) {
        InventoryItem inventoryItem = inventoryItemRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + command.inventoryItemId()
                ));

        LocalDateTime movementDate = command.purchaseDate().atStartOfDay();

        try {
            InventoryMovement movement = inventoryItem.registerPurchase(
                    command.quantity(),
                    command.unitCost(),
                    command.observation(),
                    movementDate,
                    purchaseId
            );
            inventoryItemRepository.saveWithMovement(inventoryItem, movement);

            InventoryPurchaseFinanceRecord financeRecord = recordFinanceExpense(
                    inventoryItem,
                    purchaseId,
                    movement.getTotalCost(),
                    command.purchaseDate(),
                    command.observation()
            );

            return toResult(inventoryItem, movement, financeRecord, command.purchaseDate(), false);
        } catch (DataIntegrityViolationException exception) {
            return inventoryMovementRepository
                    .findBySourceTypeAndSourceId(InventoryMovementSourceType.PURCHASE, purchaseId)
                    .map(existing -> completeExistingPurchase(existing, command, purchaseId))
                    .orElseThrow(() -> exception);
        }
    }

    private RegisterInventoryPurchaseResult completeExistingPurchase(
            InventoryMovement existing,
            RegisterInventoryPurchaseCommand command,
            UUID purchaseId
    ) {
        if (!existing.getInventoryItemId().equals(command.inventoryItemId())) {
            throw new InventoryDomainException(
                    "Purchase id is already associated with a different inventory item"
            );
        }

        InventoryItem inventoryItem = inventoryItemRepository.findById(existing.getInventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + existing.getInventoryItemId()
                ));

        InventoryPurchaseFinanceRecord financeRecord = recordFinanceExpense(
                inventoryItem,
                purchaseId,
                existing.getTotalCost(),
                command.purchaseDate() == null ? existing.getMovementDate().toLocalDate() : command.purchaseDate(),
                command.observation()
        );

        LocalDate purchaseDate = command.purchaseDate() == null
                ? existing.getMovementDate().toLocalDate()
                : command.purchaseDate();

        return toResult(inventoryItem, existing, financeRecord, purchaseDate, true);
    }

    private InventoryPurchaseFinanceRecord recordFinanceExpense(
            InventoryItem inventoryItem,
            UUID purchaseId,
            BigDecimal totalCost,
            LocalDate purchaseDate,
            String observation
    ) {
        String category = expenseCategoryFor(inventoryItem.getMaterialType());
        String description = "Compra de inventario - " + inventoryItem.getName()
                + " (" + inventoryItem.getMaterialCode().getValue() + ")";

        return inventoryPurchaseFinancePort.ensurePurchaseExpense(
                purchaseId,
                totalCost,
                purchaseDate,
                category,
                description,
                observation
        );
    }

    private void validateCommand(RegisterInventoryPurchaseCommand command) {
        Objects.requireNonNull(command.inventoryItemId(), "Inventory item id must not be null");
        Objects.requireNonNull(command.quantity(), "Quantity must not be null");
        Objects.requireNonNull(command.unitCost(), "Unit cost must not be null");
        Objects.requireNonNull(command.purchaseDate(), "Purchase date must not be null");

        if (command.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InventoryDomainException("Quantity must be greater than zero");
        }
        if (command.unitCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InventoryDomainException("Purchase unit cost must be greater than zero");
        }
    }

    static String expenseCategoryFor(InventoryMaterialType materialType) {
        return switch (materialType) {
            case PAPER -> "PAPER";
            case INK -> "INK";
            case DTF -> "DTF";
            case FABRIC, THREAD, OTHER -> "MATERIALS";
        };
    }

    private static RegisterInventoryPurchaseResult toResult(
            InventoryItem inventoryItem,
            InventoryMovement movement,
            InventoryPurchaseFinanceRecord financeRecord,
            LocalDate purchaseDate,
            boolean alreadyProcessed
    ) {
        BigDecimal totalCost = movement.getTotalCost() != null
                ? movement.getTotalCost()
                : movement.getQuantity().multiply(movement.getUnitCost()).setScale(MONEY_SCALE, MONEY_ROUNDING);

        return new RegisterInventoryPurchaseResult(
                movement.getSourceId(),
                inventoryItem.getId(),
                inventoryItem.getName(),
                inventoryItem.getMaterialCode().getValue(),
                movement.getId(),
                financeRecord.financialTransactionId(),
                movement.getQuantity(),
                movement.getUnitOfMeasure().getCode(),
                movement.getUnitCost(),
                totalCost,
                movement.getResultingStock(),
                purchaseDate,
                movement.getMovementDate(),
                movement.getObservation(),
                financeRecord.category(),
                alreadyProcessed
        );
    }
}
