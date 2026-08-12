package com.magyen.platform.home.infrastructure.inventory;

import com.magyen.platform.home.application.port.InventoryDashboardPort;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemsResult;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemsUseCase;
import com.magyen.platform.inventory.domain.InventoryItemStatus;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Clasificación paper-roll / low-stock del adaptador Home sin mutar Inventory.
 */
@ExtendWith(MockitoExtension.class)
class InventoryDashboardAdapterTest {

    @Mock
    private GetInventoryItemsUseCase getInventoryItemsUseCase;

    @Test
    void paperWithoutRpIsExcludedFromPaperRollAlertsButIncludedInInventoryAlerts() {
        GetInventoryItemResult paperWithoutRp = item(
                InventoryMaterialType.PAPER,
                "METER",
                null,
                false,
                "5.0000",
                "10.0000",
                true
        );
        when(getInventoryItemsUseCase.execute()).thenReturn(new GetInventoryItemsResult(List.of(paperWithoutRp)));

        InventoryDashboardPort.HomeInventoryAlertsSnapshot snapshot =
                new InventoryDashboardAdapter(getInventoryItemsUseCase).getCurrentInventoryAlerts();

        assertEquals(1, snapshot.inventoryAlerts().lowStockCount());
        assertEquals(0, snapshot.paperRollAlerts().lowStockCount());
    }

    @Test
    void paperWithRpButNonMeterFlagFalseIsExcludedFromPaperRollAlerts() {
        // Dominio prohíbe persistir PAPER+RP+non-METER; el adaptador confía en plotterPaperRoll.
        GetInventoryItemResult inconsistent = item(
                InventoryMaterialType.PAPER,
                "UNIT",
                "RP-999",
                false,
                "1.0000",
                "5.0000",
                true
        );
        when(getInventoryItemsUseCase.execute()).thenReturn(new GetInventoryItemsResult(List.of(inconsistent)));

        InventoryDashboardPort.HomeInventoryAlertsSnapshot snapshot =
                new InventoryDashboardAdapter(getInventoryItemsUseCase).getCurrentInventoryAlerts();

        assertEquals(1, snapshot.inventoryAlerts().lowStockCount());
        assertEquals(0, snapshot.paperRollAlerts().lowStockCount());
        assertTrue(snapshot.paperRollAlerts().items().isEmpty());
    }

    @Test
    void plotterPaperRollLowStockAppearsInBothSections() {
        GetInventoryItemResult roll = item(
                InventoryMaterialType.PAPER,
                "METER",
                "RP-001",
                true,
                "8.0000",
                "20.0000",
                true
        );
        when(getInventoryItemsUseCase.execute()).thenReturn(new GetInventoryItemsResult(List.of(roll)));

        InventoryDashboardPort.HomeInventoryAlertsSnapshot snapshot =
                new InventoryDashboardAdapter(getInventoryItemsUseCase).getCurrentInventoryAlerts();

        assertEquals(1, snapshot.inventoryAlerts().lowStockCount());
        assertEquals(1, snapshot.paperRollAlerts().lowStockCount());
        assertEquals("RP-001", snapshot.paperRollAlerts().items().getFirst().paperRollNumber());
    }

    private static GetInventoryItemResult item(
            InventoryMaterialType materialType,
            String unitOfMeasure,
            String paperRollNumber,
            boolean plotterPaperRoll,
            String stock,
            String minimumStock,
            boolean lowStock
    ) {
        return new GetInventoryItemResult(
                UUID.randomUUID(),
                "CODE-" + UUID.randomUUID().toString().substring(0, 6),
                "Material test",
                "CAT",
                unitOfMeasure,
                new BigDecimal(stock),
                new BigDecimal(minimumStock),
                InventoryItemStatus.ACTIVE,
                "desc",
                lowStock,
                null,
                materialType,
                paperRollNumber,
                plotterPaperRoll
        );
    }
}
