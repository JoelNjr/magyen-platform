package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Origen de negocio que provocó un {@link InventoryMovement}.
 * <p>
 * Inventory no posee el hecho de consumo de otros módulos; solo conserva
 * la referencia auditable del proceso que originó el movimiento.
 */
public enum InventoryMovementSourceType {

    /**
     * Movimiento registrado manualmente desde Inventory.
     * {@code sourceId} es opcional (normalmente {@code null}).
     */
    MANUAL,

    /**
     * Consumo originado por un {@code ProductionMaterialConsumption}.
     * Requiere {@code sourceId} = productionMaterialConsumptionId
     * (no el productionOrderId: una orden puede tener varios consumos).
     */
    PRODUCTION,

    /**
     * Consumo u origen asociado a un trabajo de plotter.
     * Requiere {@code sourceId} (plotterJobId).
     */
    PLOTTER,

    /**
     * Entrada por compra / recepción de material.
     * Requiere {@code sourceId} = purchaseId (no el inventoryItemId:
     * el mismo material puede comprarse varias veces).
     */
    PURCHASE;

    /**
     * Interpreta un origen desde entrada de negocio.
     */
    public static InventoryMovementSourceType of(String value) {
        Objects.requireNonNull(value, "Source type must not be null");
        if (value.isBlank()) {
            throw new InventoryDomainException("Source type must not be blank");
        }

        try {
            return InventoryMovementSourceType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InventoryDomainException("Invalid movement source type: " + value);
        }
    }

    /**
     * Reconstruye desde persistencia.
     * <p>
     * {@code null} o vacío se normalizan a {@link #MANUAL} porque los movimientos
     * históricos previos a esta capacidad fueron creados por el flujo manual.
     */
    public static InventoryMovementSourceType reconstitute(String value) {
        if (value == null || value.isBlank()) {
            return MANUAL;
        }
        return of(value);
    }

    public boolean requiresSourceId() {
        return this == PRODUCTION || this == PLOTTER || this == PURCHASE;
    }
}
