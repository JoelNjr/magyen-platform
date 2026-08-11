package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.production.domain.exception.ProductionLaborWorkAlreadyPaidException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Trabajo de mano de obra atribuible a una Orden de Producción.
 * <p>
 * Congela {@code calculatedAmount = quantity × unitRate} al crearse.
 * Su creación NO crea movimiento financiero; solo {@link #markPaid(UUID, LocalDateTime)}
 * vincula el gasto de caja vía Application.
 * {@code operatorEmployeeId} es referencia suave al empleado de nómina (sin FK).
 */
public class ProductionLaborWork {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;
    private static final int MAX_OPERATION_LENGTH = 255;
    private static final int MAX_UNIT_OF_MEASURE_LENGTH = 50;

    private final UUID id;
    private final UUID productionOrderId;
    private final UUID operatorEmployeeId;
    private final LocalDate workDate;
    private final String operation;
    private final BigDecimal quantity;
    private final String unitOfMeasure;
    private final BigDecimal unitRate;
    private final BigDecimal calculatedAmount;
    private final String observation;
    private ProductionLaborWorkStatus status;
    private LocalDateTime paidAt;
    private UUID financialTransactionId;

    private ProductionLaborWork(
            UUID id,
            UUID productionOrderId,
            UUID operatorEmployeeId,
            LocalDate workDate,
            String operation,
            BigDecimal quantity,
            String unitOfMeasure,
            BigDecimal unitRate,
            BigDecimal calculatedAmount,
            String observation,
            ProductionLaborWorkStatus status,
            LocalDateTime paidAt,
            UUID financialTransactionId
    ) {
        this.id = Objects.requireNonNull(id, "Labor work id must not be null");
        this.productionOrderId = Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        this.operatorEmployeeId = Objects.requireNonNull(operatorEmployeeId, "Operator employee id must not be null");
        this.workDate = Objects.requireNonNull(workDate, "Work date must not be null");
        this.operation = requireOperation(operation);
        this.quantity = requirePositiveQuantity(quantity);
        this.unitOfMeasure = requireUnitOfMeasure(unitOfMeasure);
        this.unitRate = requireNonNegativeUnitRate(unitRate);
        this.calculatedAmount = requirePositiveCalculatedAmount(calculatedAmount);
        this.observation = normalizeObservation(observation);
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.paidAt = paidAt;
        this.financialTransactionId = financialTransactionId;
        requireConsistentPaymentState();
    }

    /**
     * Crea un registro PENDING con monto calculado en servidor.
     */
    public static ProductionLaborWork create(
            UUID productionOrderId,
            UUID operatorEmployeeId,
            LocalDate workDate,
            String operation,
            BigDecimal quantity,
            String unitOfMeasure,
            BigDecimal unitRate,
            String observation
    ) {
        BigDecimal normalizedQuantity = requirePositiveQuantity(quantity);
        BigDecimal normalizedUnitRate = requireNonNegativeUnitRate(unitRate);
        BigDecimal calculatedAmount = normalizedQuantity
                .multiply(normalizedUnitRate)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);

        return new ProductionLaborWork(
                UUID.randomUUID(),
                productionOrderId,
                operatorEmployeeId,
                workDate,
                operation,
                normalizedQuantity,
                unitOfMeasure,
                normalizedUnitRate,
                calculatedAmount,
                observation,
                ProductionLaborWorkStatus.PENDING,
                null,
                null
        );
    }

    /**
     * Reconstruye desde persistencia. No aplica lógica de creación de negocio.
     */
    public static ProductionLaborWork reconstitute(
            UUID id,
            UUID productionOrderId,
            UUID operatorEmployeeId,
            LocalDate workDate,
            String operation,
            BigDecimal quantity,
            String unitOfMeasure,
            BigDecimal unitRate,
            BigDecimal calculatedAmount,
            String observation,
            ProductionLaborWorkStatus status,
            LocalDateTime paidAt,
            UUID financialTransactionId
    ) {
        return new ProductionLaborWork(
                id,
                productionOrderId,
                operatorEmployeeId,
                workDate,
                operation,
                quantity,
                unitOfMeasure,
                unitRate,
                calculatedAmount,
                observation,
                status,
                paidAt,
                financialTransactionId
        );
    }

    public void markPaid(UUID financialTransactionId, LocalDateTime paidAt) {
        if (status == ProductionLaborWorkStatus.PAID) {
            throw new ProductionLaborWorkAlreadyPaidException();
        }
        if (status != ProductionLaborWorkStatus.PENDING) {
            throw new ProductionDomainException(
                    "Only PENDING production labor work can be paid. Current status: " + status
            );
        }
        Objects.requireNonNull(financialTransactionId, "Financial transaction id must not be null");
        Objects.requireNonNull(paidAt, "Paid at must not be null");

        this.status = ProductionLaborWorkStatus.PAID;
        this.financialTransactionId = financialTransactionId;
        this.paidAt = paidAt;
    }

    public void cancel() {
        if (status == ProductionLaborWorkStatus.PAID) {
            throw new ProductionDomainException("A PAID production labor work cannot be cancelled");
        }
        if (status == ProductionLaborWorkStatus.CANCELLED) {
            throw new ProductionDomainException("Production labor work is already cancelled");
        }
        this.status = ProductionLaborWorkStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductionOrderId() {
        return productionOrderId;
    }

    public UUID getOperatorEmployeeId() {
        return operatorEmployeeId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public String getOperation() {
        return operation;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public BigDecimal getUnitRate() {
        return unitRate;
    }

    public BigDecimal getCalculatedAmount() {
        return calculatedAmount;
    }

    public String getObservation() {
        return observation;
    }

    public ProductionLaborWorkStatus getStatus() {
        return status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public UUID getFinancialTransactionId() {
        return financialTransactionId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ProductionLaborWork that = (ProductionLaborWork) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void requireConsistentPaymentState() {
        if (status == ProductionLaborWorkStatus.PAID) {
            if (paidAt == null || financialTransactionId == null) {
                throw new ProductionDomainException(
                        "PAID production labor work must have paidAt and financialTransactionId"
                );
            }
        }
        if (status == ProductionLaborWorkStatus.PENDING || status == ProductionLaborWorkStatus.CANCELLED) {
            if (paidAt != null || financialTransactionId != null) {
                throw new ProductionDomainException(
                        "Non-PAID production labor work must not have payment fields"
                );
            }
        }
    }

    private static BigDecimal requirePositiveQuantity(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantity must not be null");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductionDomainException("Labor quantity must be greater than zero");
        }
        return quantity;
    }

    private static BigDecimal requireNonNegativeUnitRate(BigDecimal unitRate) {
        Objects.requireNonNull(unitRate, "Unit rate must not be null");
        if (unitRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductionDomainException("Labor unit rate must not be negative");
        }
        return unitRate.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    private static BigDecimal requirePositiveCalculatedAmount(BigDecimal calculatedAmount) {
        Objects.requireNonNull(calculatedAmount, "Calculated amount must not be null");
        BigDecimal normalized = calculatedAmount.setScale(MONEY_SCALE, MONEY_ROUNDING);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductionDomainException("Labor calculated amount must be greater than zero");
        }
        return normalized;
    }

    private static String requireOperation(String operation) {
        if (operation == null || operation.isBlank()) {
            throw new ProductionDomainException("Labor operation must not be blank");
        }
        String trimmed = operation.trim();
        if (trimmed.length() > MAX_OPERATION_LENGTH) {
            throw new ProductionDomainException(
                    "Labor operation must not exceed " + MAX_OPERATION_LENGTH + " characters"
            );
        }
        return trimmed;
    }

    private static String requireUnitOfMeasure(String unitOfMeasure) {
        if (unitOfMeasure == null || unitOfMeasure.isBlank()) {
            throw new ProductionDomainException("Labor unit of measure must not be blank");
        }
        String trimmed = unitOfMeasure.trim();
        if (trimmed.length() > MAX_UNIT_OF_MEASURE_LENGTH) {
            throw new ProductionDomainException(
                    "Labor unit of measure must not exceed " + MAX_UNIT_OF_MEASURE_LENGTH + " characters"
            );
        }
        return trimmed;
    }

    private static String normalizeObservation(String observation) {
        if (observation == null || observation.isBlank()) {
            return null;
        }
        return observation.trim();
    }
}
