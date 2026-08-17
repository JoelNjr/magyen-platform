package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Origen de negocio que provocó un {@link FinancialTransaction}.
 * <p>
 * Finance no posee los agregados externos; solo conserva una referencia UUID blanda
 * ({@code sourceId}) para trazabilidad futura.
 */
public enum FinancialTransactionSourceType {

    /**
     * Movimiento registrado manualmente en Finance.
     * {@code sourceId} normalmente es {@code null}.
     */
    MANUAL,

    /**
     * Ingreso generado por un pago de cliente sobre una orden comercial.
     * {@code sourceId} = paymentId (no orderId; una orden puede tener varios pagos).
     */
    COMMERCIAL_ORDER,

    /**
     * Ingreso generado por un pago de cliente sobre un trabajo de Plotter.
     * {@code sourceId} = plotterPaymentId (no plotterJobId; un trabajo puede tener varios pagos).
     */
    PLOTTER,

    /**
     * Movimiento asociado a producción.
     */
    PRODUCTION,

    /**
     * Movimiento asociado a nómina.
     */
    PAYROLL,

    /**
     * Movimiento asociado a un servicio.
     */
    SERVICE,

    /**
     * Movimiento asociado a un crédito u obligación.
     */
    CREDIT,

    /**
     * Movimiento generado al pagar una ocurrencia de obligación recurrente.
     * {@code sourceId} = recurringFinancialObligationOccurrenceId.
     */
    RECURRING_OBLIGATION,

    /**
     * Gasto de caja por adquirir material de inventario.
     * {@code sourceId} = purchaseId (no inventoryItemId; un material puede comprarse varias veces).
     * No representa consumo de producción.
     */
    INVENTORY_PURCHASE;

    /**
     * Interpreta un origen desde entrada de negocio.
     */
    public static FinancialTransactionSourceType of(String value) {
        Objects.requireNonNull(value, "Source type must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Source type must not be blank");
        }

        try {
            return FinancialTransactionSourceType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid financial source type: " + value);
        }
    }

    /**
     * Reconstruye desde persistencia.
     * <p>
     * {@code null} o vacío se normalizan a {@link #MANUAL}.
     */
    public static FinancialTransactionSourceType reconstitute(String value) {
        if (value == null || value.isBlank()) {
            return MANUAL;
        }
        return of(value);
    }
}
