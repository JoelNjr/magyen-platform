package com.magyen.platform.plotter.domain;

import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root de un pago de cliente sobre un {@link PlotterJob}.
 * <p>
 * Representa dinero realmente recibido. No modifica el estado del trabajo.
 * El ingreso en el ledger Finance se sincroniza desde Application.
 */
public class PlotterPayment {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int MAX_OBSERVATIONS_LENGTH = 2000;

    private final UUID id;
    private final UUID customerId;
    private final UUID plotterJobId;
    private final UUID registrationKey;
    private final BigDecimal amount;
    private final LocalDate paymentDate;
    private final String observations;

    private PlotterPayment(
            UUID id,
            UUID customerId,
            UUID plotterJobId,
            UUID registrationKey,
            BigDecimal amount,
            LocalDate paymentDate,
            String observations
    ) {
        this.id = Objects.requireNonNull(id, "Plotter payment id must not be null");
        this.customerId = customerId;
        this.plotterJobId = plotterJobId;
        this.registrationKey = registrationKey;
        if (customerId == null && plotterJobId == null) {
            throw new PlotterDomainException("Plotter payment requires a customer or a historical job");
        }
        this.amount = normalizeAmount(amount);
        this.paymentDate = Objects.requireNonNull(paymentDate, "Payment date must not be null");
        this.observations = normalizeObservations(observations);
    }

    public static PlotterPayment create(
            UUID plotterJobId,
            BigDecimal amount,
            LocalDate paymentDate,
            String observations
    ) {
        return createForJob(null, plotterJobId, amount, paymentDate, observations);
    }

    /**
     * Pago histórico o compatible, todavía asociado a un trabajo y a su cliente.
     */
    public static PlotterPayment createForJob(
            UUID customerId,
            UUID plotterJobId,
            BigDecimal amount,
            LocalDate paymentDate,
            String observations
    ) {
        return new PlotterPayment(
                UUID.randomUUID(),
                customerId,
                Objects.requireNonNull(plotterJobId, "Plotter job id must not be null"),
                null,
                amount,
                paymentDate,
                observations
        );
    }

    /**
     * Pago de la cuenta corriente. No liquida un trabajo concreto.
     * {@code registrationKey} hace idempotente un reintento del mismo cobro.
     */
    public static PlotterPayment createForCustomer(
            UUID customerId,
            BigDecimal amount,
            LocalDate paymentDate,
            String observations,
            UUID registrationKey
    ) {
        if (registrationKey == null) {
            throw new PlotterDomainException("Plotter payment registration key is required");
        }
        return new PlotterPayment(
                UUID.randomUUID(),
                Objects.requireNonNull(customerId, "Customer id must not be null"),
                null,
                registrationKey,
                amount,
                paymentDate,
                observations
        );
    }

    public static PlotterPayment reconstitute(
            UUID id,
            UUID plotterJobId,
            BigDecimal amount,
            LocalDate paymentDate,
            String observations
    ) {
        return reconstitute(id, null, plotterJobId, null, amount, paymentDate, observations);
    }

    public static PlotterPayment reconstitute(
            UUID id,
            UUID customerId,
            UUID plotterJobId,
            UUID registrationKey,
            BigDecimal amount,
            LocalDate paymentDate,
            String observations
    ) {
        return new PlotterPayment(
                id,
                customerId,
                plotterJobId,
                registrationKey,
                amount,
                paymentDate,
                observations
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getPlotterJobId() {
        return plotterJobId;
    }

    public UUID getRegistrationKey() {
        return registrationKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getObservations() {
        return observations;
    }

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "Payment amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlotterDomainException("Plotter payment amount must be greater than zero");
        }
        return amount.setScale(SCALE, ROUNDING_MODE);
    }

    private static String normalizeObservations(String observations) {
        if (observations == null || observations.isBlank()) {
            return null;
        }
        String trimmed = observations.trim();
        if (trimmed.length() > MAX_OBSERVATIONS_LENGTH) {
            throw new PlotterDomainException(
                    "Plotter payment observations must not exceed " + MAX_OBSERVATIONS_LENGTH + " characters"
            );
        }
        return trimmed;
    }
}
