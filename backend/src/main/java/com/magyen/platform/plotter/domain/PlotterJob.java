package com.magyen.platform.plotter.domain;

import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del módulo de Plotter.
 * <p>
 * Registra el trabajo de impresión y el valor cobrado al cliente (ingreso).
 * No posee costos de material; esos se calcularán al integrar con Inventario.
 */
public class PlotterJob {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final UUID id;
    private final UUID customerId;
    private final UUID orderId;
    private final LocalDate creationDate;
    private final UUID paperInventoryItemId;
    private final BigDecimal printedMeters;
    private final BigDecimal pricePerMeter;
    private final BigDecimal totalAmount;
    private final PlotterJobStatus status;
    private final String observations;

    private PlotterJob(
            UUID id,
            UUID customerId,
            UUID orderId,
            LocalDate creationDate,
            UUID paperInventoryItemId,
            BigDecimal printedMeters,
            BigDecimal pricePerMeter,
            BigDecimal totalAmount,
            PlotterJobStatus status,
            String observations
    ) {
        this.id = Objects.requireNonNull(id, "Plotter job id must not be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer id must not be null");
        this.orderId = orderId;
        this.creationDate = Objects.requireNonNull(creationDate, "Creation date must not be null");
        this.paperInventoryItemId = Objects.requireNonNull(
                paperInventoryItemId,
                "Paper inventory item id must not be null"
        );
        this.printedMeters = requirePositiveMeters(printedMeters);
        this.pricePerMeter = requireValidPricePerMeter(pricePerMeter);
        this.totalAmount = Objects.requireNonNull(totalAmount, "Total amount must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.observations = normalizeObservations(observations);
    }

    /**
     * Crea un trabajo de plotter en estado {@link PlotterJobStatus#REGISTERED}.
     * {@code totalAmount} se calcula como metros impresos × precio por metro.
     */
    public static PlotterJob create(
            UUID customerId,
            LocalDate creationDate,
            UUID paperInventoryItemId,
            BigDecimal printedMeters,
            BigDecimal pricePerMeter,
            String observations
    ) {
        return create(
                customerId,
                null,
                creationDate,
                paperInventoryItemId,
                printedMeters,
                pricePerMeter,
                observations
        );
    }

    /**
     * Crea un trabajo de plotter atribuible opcionalmente a una Orden comercial.
     * {@code orderId} es una referencia blanda; no hay FK cruzada.
     */
    public static PlotterJob create(
            UUID customerId,
            UUID orderId,
            LocalDate creationDate,
            UUID paperInventoryItemId,
            BigDecimal printedMeters,
            BigDecimal pricePerMeter,
            String observations
    ) {
        BigDecimal normalizedMeters = requirePositiveMeters(printedMeters);
        BigDecimal normalizedPrice = requireValidPricePerMeter(pricePerMeter);
        BigDecimal totalAmount = calculateTotalAmount(normalizedMeters, normalizedPrice);

        return new PlotterJob(
                UUID.randomUUID(),
                customerId,
                orderId,
                creationDate,
                paperInventoryItemId,
                normalizedMeters,
                normalizedPrice,
                totalAmount,
                PlotterJobStatus.REGISTERED,
                observations
        );
    }

    /**
     * Reconstruye un trabajo de plotter desde persistencia.
     */
    public static PlotterJob reconstitute(
            UUID id,
            UUID customerId,
            LocalDate creationDate,
            UUID paperInventoryItemId,
            BigDecimal printedMeters,
            BigDecimal pricePerMeter,
            BigDecimal totalAmount,
            PlotterJobStatus status,
            String observations
    ) {
        return reconstitute(
                id,
                customerId,
                null,
                creationDate,
                paperInventoryItemId,
                printedMeters,
                pricePerMeter,
                totalAmount,
                status,
                observations
        );
    }

    public static PlotterJob reconstitute(
            UUID id,
            UUID customerId,
            UUID orderId,
            LocalDate creationDate,
            UUID paperInventoryItemId,
            BigDecimal printedMeters,
            BigDecimal pricePerMeter,
            BigDecimal totalAmount,
            PlotterJobStatus status,
            String observations
    ) {
        return new PlotterJob(
                id,
                customerId,
                orderId,
                creationDate,
                paperInventoryItemId,
                printedMeters,
                pricePerMeter,
                totalAmount,
                status,
                observations
        );
    }

    public static BigDecimal calculateTotalAmount(BigDecimal printedMeters, BigDecimal pricePerMeter) {
        return requirePositiveMeters(printedMeters)
                .multiply(requireValidPricePerMeter(pricePerMeter))
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public UUID getPaperInventoryItemId() {
        return paperInventoryItemId;
    }

    public BigDecimal getPrintedMeters() {
        return printedMeters;
    }

    public BigDecimal getPricePerMeter() {
        return pricePerMeter;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public PlotterJobStatus getStatus() {
        return status;
    }

    public String getObservations() {
        return observations;
    }

    private static BigDecimal requirePositiveMeters(BigDecimal printedMeters) {
        if (printedMeters == null) {
            throw new PlotterDomainException("Printed meters must not be null");
        }
        if (printedMeters.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlotterDomainException("Printed meters must be greater than zero");
        }
        return printedMeters;
    }

    private static BigDecimal requireValidPricePerMeter(BigDecimal pricePerMeter) {
        if (pricePerMeter == null) {
            throw new PlotterDomainException("Price per meter must not be null");
        }
        if (pricePerMeter.compareTo(BigDecimal.ZERO) < 0) {
            throw new PlotterDomainException("Price per meter must not be negative");
        }
        return pricePerMeter.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    private static String normalizeObservations(String observations) {
        if (observations == null) {
            return null;
        }
        String trimmed = observations.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
