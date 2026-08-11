package com.magyen.platform.finance.infrastructure.persistence;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class FinancialTransactionPersistenceTest {

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAmountScaleAndOptionalSourceFields() {
        UUID sourceId = UUID.randomUUID();

        FinancialTransaction saved = financialTransactionRepository.save(
                FinancialTransaction.create(
                        FinancialTransactionType.EXPENSE,
                        FinancialAmount.of(new BigDecimal("150000.1")),
                        LocalDate.of(2026, 8, 10),
                        "Servicios",
                        "Pago de energía",
                        "Factura agosto",
                        FinancialTransactionSourceType.SERVICE,
                        sourceId
                )
        );

        entityManager.flush();
        entityManager.clear();

        FinancialTransaction reloaded = financialTransactionRepository.findById(saved.getId()).orElseThrow();

        assertEquals(saved.getId(), reloaded.getId());
        assertEquals(FinancialTransactionType.EXPENSE, reloaded.getType());
        assertEquals(new BigDecimal("150000.10"), reloaded.getAmount().getValue());
        assertEquals(LocalDate.of(2026, 8, 10), reloaded.getTransactionDate());
        assertEquals("Servicios", reloaded.getCategory());
        assertEquals("Pago de energía", reloaded.getDescription());
        assertEquals("Factura agosto", reloaded.getObservation());
        assertEquals(FinancialTransactionSourceType.SERVICE, reloaded.getSourceType());
        assertEquals(sourceId, reloaded.getSourceId());
    }

    @Test
    void persistsManualSourceWithNullSourceIdAndOrdersNewestFirst() {
        FinancialTransaction older = financialTransactionRepository.save(
                FinancialTransaction.create(
                        FinancialTransactionType.INCOME,
                        FinancialAmount.of(new BigDecimal("100.00")),
                        LocalDate.of(2026, 8, 1),
                        "Ventas",
                        null,
                        null,
                        FinancialTransactionSourceType.MANUAL,
                        null
                )
        );
        FinancialTransaction newer = financialTransactionRepository.save(
                FinancialTransaction.create(
                        FinancialTransactionType.EXPENSE,
                        FinancialAmount.of(new BigDecimal("50.00")),
                        LocalDate.of(2026, 8, 10),
                        "Otros",
                        null,
                        null,
                        FinancialTransactionSourceType.MANUAL,
                        null
                )
        );

        entityManager.flush();
        entityManager.clear();

        List<FinancialTransaction> listed = financialTransactionRepository.findAllNewestFirst();
        int newerIndex = indexOf(listed, newer.getId());
        int olderIndex = indexOf(listed, older.getId());

        assertTrue(newerIndex >= 0);
        assertTrue(olderIndex >= 0);
        assertTrue(newerIndex < olderIndex);

        FinancialTransaction reloadedManual = financialTransactionRepository.findById(older.getId()).orElseThrow();
        assertEquals(FinancialTransactionSourceType.MANUAL, reloadedManual.getSourceType());
        assertNull(reloadedManual.getSourceId());
    }

    private static int indexOf(List<FinancialTransaction> transactions, UUID id) {
        for (int index = 0; index < transactions.size(); index++) {
            if (transactions.get(index).getId().equals(id)) {
                return index;
            }
        }
        return -1;
    }
}
