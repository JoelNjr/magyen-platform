package com.magyen.platform.home.infrastructure.inventory;

import com.magyen.platform.home.application.port.InventoryDashboardPort;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemsUseCase;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Adaptador Home → Inventory para alertas de stock bajo y rollos de papel.
 * <p>
 * Reutiliza {@link GetInventoryItemResult#lowStock()} y
 * {@link GetInventoryItemResult#plotterPaperRoll()} — no duplica reglas de dominio.
 * <p>
 * Orden (Inventory no define orden de low-stock): urgencia relativa
 * {@code stock - minimumStock} ascendente, luego stock, código/RP, id.
 */
public class InventoryDashboardAdapter implements InventoryDashboardPort {

    private static final Comparator<GetInventoryItemResult> LOW_STOCK_ORDER = Comparator
            .comparing(InventoryDashboardAdapter::stockRelativeToMinimum)
            .thenComparing(GetInventoryItemResult::stock, Comparator.nullsLast(BigDecimal::compareTo))
            .thenComparing(
                    item -> item.paperRollNumber() != null ? item.paperRollNumber() : item.materialCode(),
                    Comparator.nullsLast(String::compareTo)
            )
            .thenComparing(item -> item.inventoryItemId().toString());

    private final GetInventoryItemsUseCase getInventoryItemsUseCase;

    public InventoryDashboardAdapter(GetInventoryItemsUseCase getInventoryItemsUseCase) {
        this.getInventoryItemsUseCase = Objects.requireNonNull(
                getInventoryItemsUseCase,
                "Get inventory items use case must not be null"
        );
    }

    @Override
    public HomeInventoryAlertsSnapshot getCurrentInventoryAlerts() {
        List<GetInventoryItemResult> allItems = getInventoryItemsUseCase.execute().items();

        List<InventoryAlertItem> inventoryAlerts = allItems.stream()
                .filter(GetInventoryItemResult::lowStock)
                .sorted(LOW_STOCK_ORDER)
                .map(this::toInventoryAlertItem)
                .toList();

        List<PaperRollAlertItem> paperRollAlerts = allItems.stream()
                .filter(GetInventoryItemResult::lowStock)
                .filter(GetInventoryItemResult::plotterPaperRoll)
                .sorted(LOW_STOCK_ORDER)
                .map(this::toPaperRollAlertItem)
                .toList();

        return new HomeInventoryAlertsSnapshot(
                new InventoryAlertsSection(inventoryAlerts.size(), inventoryAlerts),
                new PaperRollAlertsSection(paperRollAlerts.size(), paperRollAlerts)
        );
    }

    /**
     * Distancia al mínimo: valores más negativos = más por debajo del mínimo = primero.
     */
    private static BigDecimal stockRelativeToMinimum(GetInventoryItemResult item) {
        if (item.stock() == null || item.minimumStock() == null) {
            return BigDecimal.ZERO;
        }
        return item.stock().subtract(item.minimumStock());
    }

    private InventoryAlertItem toInventoryAlertItem(GetInventoryItemResult item) {
        return new InventoryAlertItem(
                item.inventoryItemId(),
                item.materialCode(),
                item.name(),
                item.description(),
                item.materialType() == null ? null : item.materialType().name(),
                item.paperRollNumber(),
                item.stock(),
                item.unitOfMeasure(),
                item.minimumStock(),
                item.lowStock()
        );
    }

    private PaperRollAlertItem toPaperRollAlertItem(GetInventoryItemResult item) {
        return new PaperRollAlertItem(
                item.inventoryItemId(),
                item.materialCode(),
                item.name(),
                item.paperRollNumber(),
                item.stock(),
                item.unitOfMeasure(),
                item.minimumStock(),
                item.lowStock()
        );
    }
}
