package com.magyen.platform.finance.infrastructure.persistence;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RecurringFinancialObligationPersistenceTest {

    @Autowired
    private RecurringFinancialObligationRepository recurringFinancialObligationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAmountScaleNullableEndDateDueDayAndActiveState() {
        RecurringFinancialObligation saved = recurringFinancialObligationRepository.save(
                RecurringFinancialObligation.create(
                        "Internet",
                        RecurringObligationType.SERVICE,
                        FinancialAmount.of(new BigDecimal("120000.1")),
                        RecurringObligationFrequency.MONTHLY,
                        15,
                        LocalDate.of(2026, 8, 1),
                        null,
                        "Internet del taller",
                        "Sin observación"
                )
        );

        entityManager.flush();
        entityManager.clear();

        RecurringFinancialObligation reloaded =
                recurringFinancialObligationRepository.findById(saved.getId()).orElseThrow();

        assertEquals(saved.getId(), reloaded.getId());
        assertEquals(new BigDecimal("120000.10"), reloaded.getExpectedAmount().getValue());
        assertEquals(15, reloaded.getDueDay());
        assertNull(reloaded.getEndDate());
        assertTrue(reloaded.isActive());
        assertEquals("Internet del taller", reloaded.getDescription());

        reloaded.deactivate();
        recurringFinancialObligationRepository.update(reloaded);

        entityManager.flush();
        entityManager.clear();

        RecurringFinancialObligation deactivated =
                recurringFinancialObligationRepository.findById(saved.getId()).orElseThrow();
        assertFalse(deactivated.isActive());
        assertTrue(recurringFinancialObligationRepository.findActive().stream()
                .noneMatch(item -> item.getId().equals(saved.getId())));
    }
}
