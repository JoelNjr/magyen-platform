package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinancialTransactionTest {

    @Test
    void createsValidIncome() {
        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.INCOME,
                FinancialAmount.of(new BigDecimal("250000.00")),
                LocalDate.of(2026, 8, 10),
                "Ventas",
                "Cobro uniforme",
                null,
                FinancialTransactionSourceType.MANUAL,
                null
        );

        assertEquals(FinancialTransactionType.INCOME, transaction.getType());
        assertEquals(new BigDecimal("250000.00"), transaction.getAmount().getValue());
        assertEquals("Ventas", transaction.getCategory());
        assertEquals(FinancialTransactionSourceType.MANUAL, transaction.getSourceType());
        assertNull(transaction.getSourceId());
    }

    @Test
    void createsValidExpense() {
        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                FinancialAmount.of(new BigDecimal("150000.00")),
                LocalDate.of(2026, 8, 10),
                "Servicios",
                "Pago de energía",
                "Factura agosto",
                FinancialTransactionSourceType.SERVICE,
                null
        );

        assertEquals(FinancialTransactionType.EXPENSE, transaction.getType());
        assertEquals(new BigDecimal("150000.00"), transaction.getAmount().getValue());
        assertEquals("Servicios", transaction.getCategory());
        assertEquals("Pago de energía", transaction.getDescription());
        assertEquals("Factura agosto", transaction.getObservation());
        assertEquals(FinancialTransactionSourceType.SERVICE, transaction.getSourceType());
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(FinanceDomainException.class, () -> FinancialAmount.of(new BigDecimal("-1.00")));
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(FinanceDomainException.class, () -> FinancialAmount.of(BigDecimal.ZERO));
    }

    @Test
    void rejectsMissingRequiredFields() {
        assertThrows(FinanceDomainException.class, () -> FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                FinancialAmount.of(new BigDecimal("10.00")),
                LocalDate.of(2026, 8, 10),
                "   ",
                null,
                null,
                FinancialTransactionSourceType.MANUAL,
                null
        ));

        assertThrows(NullPointerException.class, () -> FinancialTransaction.create(
                null,
                FinancialAmount.of(new BigDecimal("10.00")),
                LocalDate.of(2026, 8, 10),
                "Servicios",
                null,
                null,
                FinancialTransactionSourceType.MANUAL,
                null
        ));

        assertThrows(NullPointerException.class, () -> FinancialTransaction.create(
                FinancialTransactionType.INCOME,
                FinancialAmount.of(new BigDecimal("10.00")),
                null,
                "Ventas",
                null,
                null,
                FinancialTransactionSourceType.MANUAL,
                null
        ));

        assertThrows(FinanceDomainException.class, () -> FinancialTransactionType.of("UNKNOWN"));
    }

    @Test
    void acceptsSourceMetadata() {
        UUID sourceId = UUID.randomUUID();

        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.INCOME,
                FinancialAmount.of(new BigDecimal("99999.50")),
                LocalDate.of(2026, 8, 10),
                "Plotter",
                "Trabajo plotter",
                null,
                FinancialTransactionSourceType.PLOTTER,
                sourceId
        );

        assertEquals(FinancialTransactionSourceType.PLOTTER, transaction.getSourceType());
        assertEquals(sourceId, transaction.getSourceId());
        assertEquals(new BigDecimal("99999.50"), transaction.getAmount().getValue());
    }
}
