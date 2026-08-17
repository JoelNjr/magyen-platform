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
 * Un trabajo interno de Plotter es una operación de material de producción,
 * no una segunda compra ni una venta. El costo de papel lo posee Inventory
 * en el snapshot del OUT. Un trabajo externo cobra al cliente (ingreso).
 */
public class PlotterJob {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final UUID id;
    private final PlotterJobType jobType;
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
            PlotterJobType jobType,
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
        this.jobType = Objects.requireNonNull(jobType, "Plotter job type must not be null");
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
        if (jobType.isInternal() && orderId == null) {
            throw new PlotterDomainException("Internal Magyen plotter jobs require a commercial order");
        }
    }

    /**
     * Crea un trabajo externo (cliente de impresión) en estado {@link PlotterJobStatus#REGISTERED}.
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
                UUID.randomUUID(),
                PlotterJobType.EXTERNAL,
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
     * Crea un trabajo de plotter. {@code orderId} es referencia blanda; no hay FK cruzada.
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
        PlotterJobType jobType = orderId == null ? PlotterJobType.EXTERNAL : PlotterJobType.INTERNAL_MAGYEN;
        BigDecimal chargedPrice = jobType.isInternal() ? BigDecimal.ZERO : pricePerMeter;
        return create(
                UUID.randomUUID(),
                jobType,
                customerId,
                orderId,
                creationDate,
                paperInventoryItemId,
                printedMeters,
                chargedPrice,
                observations
        );
    }

    public static PlotterJob create(
            UUID id,
            PlotterJobType jobType,
            UUID customerId,
            UUID orderId,
            LocalDate creationDate,
            UUID paperInventoryItemId,
            BigDecimal printedMeters,
            BigDecimal pricePerMeter,
            String observations
    ) {
        UUID jobId = id == null ? UUID.randomUUID() : id;
        PlotterJobType resolvedType = Objects.requireNonNull(jobType, "Plotter job type must not be null");
        BigDecimal normalizedMeters = requirePositiveMeters(printedMeters);
        BigDecimal normalizedPrice = requireValidPricePerMeter(
                resolvedType.isInternal() ? BigDecimal.ZERO : pricePerMeter
        );
        BigDecimal totalAmount = calculateTotalAmount(normalizedMeters, normalizedPrice);

        return new PlotterJob(
                jobId,
                resolvedType,
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
                PlotterJobType.EXTERNAL,
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
        return reconstitute(
                id,
                orderId == null ? PlotterJobType.EXTERNAL : PlotterJobType.INTERNAL_MAGYEN,
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

    public static PlotterJob reconstitute(
            UUID id,
            PlotterJobType jobType,
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
                jobType,
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

    public PlotterJobType getJobType() {
        return jobType;
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
