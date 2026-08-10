package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Hecho histórico inmutable: una Orden de Producción consumió una cantidad de material.
 * <p>
 * {@code inventoryItemId} es una referencia técnica suave a Inventory (sin FK ni dependencia
 * de repositorios Inventory). La deducción de stock queda fuera de este agregado.
 */
public final class ProductionMaterialConsumption {

    private final UUID id;
    private final UUID productionOrderId;
    private final UUID inventoryItemId;
    private final BigDecimal quantity;
    private final ProductionMaterialUnitOfMeasure unitOfMeasure;
    private final LocalDateTime consumptionDate;
    private final String observation;

    ProductionMaterialConsumption(
            UUID id,
            UUID productionOrderId,
            UUID inventoryItemId,
            BigDecimal quantity,
            ProductionMaterialUnitOfMeasure unitOfMeasure,
            LocalDateTime consumptionDate,
            String observation
    ) {
        this.id = Objects.requireNonNull(id, "Consumption id must not be null");
        this.productionOrderId = Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        this.inventoryItemId = Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");
        this.quantity = requirePositiveQuantity(quantity);
        this.unitOfMeasure = Objects.requireNonNull(unitOfMeasure, "Unit of measure must not be null");
        this.consumptionDate = Objects.requireNonNull(consumptionDate, "Consumption date must not be null");
        this.observation = normalizeObservation(observation);
    }

    /**
     * Crea un consumo asociado a una Orden de Producción.
     * <p>
     * Solo invocable desde el agregado {@link ProductionOrder}.
     */
    static ProductionMaterialConsumption create(
            UUID productionOrderId,
            UUID inventoryItemId,
            BigDecimal quantity,
            ProductionMaterialUnitOfMeasure unitOfMeasure,
            String observation
    ) {
        return new ProductionMaterialConsumption(
                UUID.randomUUID(),
                productionOrderId,
                inventoryItemId,
                quantity,
                unitOfMeasure,
                LocalDateTime.now(),
                observation
        );
    }

    /**
     * Reconstruye un consumo desde persistencia. No aplica lógica de creación de negocio.
     */
    public static ProductionMaterialConsumption reconstitute(
            UUID id,
            UUID productionOrderId,
            UUID inventoryItemId,
            BigDecimal quantity,
            ProductionMaterialUnitOfMeasure unitOfMeasure,
            LocalDateTime consumptionDate,
            String observation
    ) {
        return new ProductionMaterialConsumption(
                id,
                productionOrderId,
                inventoryItemId,
                quantity,
                unitOfMeasure,
                consumptionDate,
                observation
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductionOrderId() {
        return productionOrderId;
    }

    public UUID getInventoryItemId() {
        return inventoryItemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public ProductionMaterialUnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public LocalDateTime getConsumptionDate() {
        return consumptionDate;
    }

    public String getObservation() {
        return observation;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ProductionMaterialConsumption that = (ProductionMaterialConsumption) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static BigDecimal requirePositiveQuantity(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantity must not be null");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductionDomainException("Consumption quantity must be greater than zero");
        }
        return quantity;
    }

    private static String normalizeObservation(String observation) {
        if (observation == null || observation.isBlank()) {
            return null;
        }
        return observation.trim();
    }
}
