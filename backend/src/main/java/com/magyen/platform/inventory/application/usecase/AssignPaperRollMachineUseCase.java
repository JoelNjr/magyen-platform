package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.AssignPaperRollMachineCommand;
import com.magyen.platform.inventory.application.dto.AssignPaperRollMachineResult;
import com.magyen.platform.inventory.application.port.PaperRollMachinePort;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Fija la máquina de un rollo que todavía no tiene asignación.
 * <p>
 * No crea una compra, un movimiento ni un asiento. No permite cambiar una asignación existente.
 */
public class AssignPaperRollMachineUseCase {

    private final InventoryItemRepository inventoryItemRepository;
    private final PaperRollMachinePort paperRollMachinePort;

    public AssignPaperRollMachineUseCase(
            InventoryItemRepository inventoryItemRepository,
            PaperRollMachinePort paperRollMachinePort
    ) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
        this.paperRollMachinePort = Objects.requireNonNull(
                paperRollMachinePort,
                "Paper roll machine port must not be null"
        );
    }

    @Transactional
    public AssignPaperRollMachineResult execute(AssignPaperRollMachineCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.inventoryItemId(), "Inventory item id must not be null");

        InventoryItem inventoryItem = inventoryItemRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + command.inventoryItemId()
                ));
        if (!inventoryItem.isPlotterPaperRoll()) {
            throw new InventoryDomainException("Only Plotter paper rolls can be assigned to a Plotter machine");
        }

        BigDecimal stockBefore = inventoryItem.getStock();
        BigDecimal unitCostBefore = inventoryItem.getUnitCost();
        UUID machineId = paperRollMachinePort.requireActiveMachine(command.plotterMachineId());
        inventoryItem.assignPlotterMachine(machineId);
        InventoryItem saved = inventoryItemRepository.save(inventoryItem);

        if (saved.getStock().compareTo(stockBefore) != 0
                || !Objects.equals(saved.getUnitCost(), unitCostBefore)) {
            throw new InventoryDomainException("Assigning a Plotter machine must not change stock or cost");
        }

        return new AssignPaperRollMachineResult(
                saved.getId(),
                saved.getPaperRollNumber(),
                saved.getPlotterMachineId(),
                saved.getStock(),
                saved.getUnitCost()
        );
    }
}
