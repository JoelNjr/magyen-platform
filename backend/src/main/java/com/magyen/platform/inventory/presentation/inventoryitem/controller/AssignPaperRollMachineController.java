package com.magyen.platform.inventory.presentation.inventoryitem.controller;

import com.magyen.platform.inventory.application.dto.AssignPaperRollMachineCommand;
import com.magyen.platform.inventory.application.dto.AssignPaperRollMachineResult;
import com.magyen.platform.inventory.application.usecase.AssignPaperRollMachineUseCase;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import com.magyen.platform.inventory.presentation.inventoryitem.request.AssignPaperRollMachineRequest;
import com.magyen.platform.inventory.presentation.inventoryitem.response.AssignPaperRollMachineResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/**
 * Asignación administrativa de un rollo que todavía no tiene máquina.
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class AssignPaperRollMachineController {

    private final AssignPaperRollMachineUseCase assignPaperRollMachineUseCase;

    public AssignPaperRollMachineController(AssignPaperRollMachineUseCase assignPaperRollMachineUseCase) {
        this.assignPaperRollMachineUseCase = Objects.requireNonNull(
                assignPaperRollMachineUseCase,
                "Assign paper roll machine use case must not be null"
        );
    }

    @PostMapping("/{inventoryItemId}/plotter-machine")
    public ResponseEntity<AssignPaperRollMachineResponse> assign(
            @PathVariable UUID inventoryItemId,
            @RequestBody AssignPaperRollMachineRequest request
    ) {
        if (request == null || request.plotterMachineId() == null) {
            throw new InventoryDomainException("Plotter machine must not be null");
        }
        AssignPaperRollMachineResult result = assignPaperRollMachineUseCase.execute(
                new AssignPaperRollMachineCommand(inventoryItemId, request.plotterMachineId())
        );
        return ResponseEntity.ok(new AssignPaperRollMachineResponse(
                result.inventoryItemId(),
                result.paperRollNumber(),
                result.plotterMachineId(),
                result.stock(),
                result.unitCost()
        ));
    }
}
