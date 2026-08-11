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
    private final UUID plotterJobId;
    private final BigDecimal amount;
    private final LocalDate paymentDate;
    private final String observations;

    private PlotterPayment(
            UUID id,
            UUID plotterJobId,
            BigDecimal amount,
            LocalDate paymentDate,
            String observations
    ) {
        this.id = Objects.requireNonNull(id, "Plotter payment id must not be null");
        this.plotterJobId = Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
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
        return new PlotterPayment(
                UUID.randomUUID(),
                plotterJobId,
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
        return new PlotterPayment(id, plotterJobId, amount, paymentDate, observations);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlotterJobId() {
        return plotterJobId;
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
