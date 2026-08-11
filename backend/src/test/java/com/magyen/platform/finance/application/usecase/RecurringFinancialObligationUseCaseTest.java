package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.DeactivateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationsQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationsResult;
import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationResult;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RecurringFinancialObligationUseCaseTest {

    @Autowired
    private CreateRecurringFinancialObligationUseCase createRecurringFinancialObligationUseCase;

    @Autowired
    private GetRecurringFinancialObligationUseCase getRecurringFinancialObligationUseCase;

    @Autowired
    private GetRecurringFinancialObligationsUseCase getRecurringFinancialObligationsUseCase;

    @Autowired
    private UpdateRecurringFinancialObligationUseCase updateRecurringFinancialObligationUseCase;

    @Autowired
    private DeactivateRecurringFinancialObligationUseCase deactivateRecurringFinancialObligationUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void createsGetsListsUpdatesAndDeactivatesObligation() {
        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        CreateRecurringFinancialObligationResult created = createRecurringFinancialObligationUseCase.execute(
                new CreateRecurringFinancialObligationCommand(
                        "Internet",
                        RecurringObligationType.SERVICE,
                        new BigDecimal("120000.00"),
                        RecurringObligationFrequency.MONTHLY,
                        15,
                        LocalDate.of(2026, 8, 1),
                        null,
                        "Internet del taller",
                        null
                )
        );

        assertTrue(created.active());
        assertEquals(new BigDecimal("120000.00"), created.expectedAmount());
        assertEquals(
                transactionsBefore,
                financialTransactionRepository.findAllNewestFirst().size(),
                "Creating an obligation must not create a FinancialTransaction"
        );

        GetRecurringFinancialObligationResult loaded = getRecurringFinancialObligationUseCase.execute(
                new GetRecurringFinancialObligationQuery(created.obligationId())
        );
        assertEquals("Internet", loaded.name());
        assertEquals(15, loaded.dueDay());

        CreateRecurringFinancialObligationResult inactiveSibling =
                createRecurringFinancialObligationUseCase.execute(
                        new CreateRecurringFinancialObligationCommand(
                                "Arriendo",
                                RecurringObligationType.OTHER,
                                new BigDecimal("800000.00"),
                                RecurringObligationFrequency.MONTHLY,
                                1,
                                LocalDate.of(2026, 8, 1),
                                null,
                                null,
                                null
                        )
                );
        deactivateRecurringFinancialObligationUseCase.execute(
                new DeactivateRecurringFinancialObligationCommand(inactiveSibling.obligationId())
        );

        GetRecurringFinancialObligationsResult all =
                getRecurringFinancialObligationsUseCase.execute(GetRecurringFinancialObligationsQuery.all());
        assertTrue(all.obligations().stream().anyMatch(item -> item.obligationId().equals(created.obligationId())));
        assertTrue(all.obligations().stream()
                .anyMatch(item -> item.obligationId().equals(inactiveSibling.obligationId())));

        GetRecurringFinancialObligationsResult activeOnly =
                getRecurringFinancialObligationsUseCase.execute(
                        new GetRecurringFinancialObligationsQuery(true)
                );
        assertTrue(activeOnly.obligations().stream()
                .anyMatch(item -> item.obligationId().equals(created.obligationId())));
        assertTrue(activeOnly.obligations().stream()
                .noneMatch(item -> item.obligationId().equals(inactiveSibling.obligationId())));

        UpdateRecurringFinancialObligationResult updated = updateRecurringFinancialObligationUseCase.execute(
                new UpdateRecurringFinancialObligationCommand(
                        created.obligationId(),
                        "Internet fibra",
                        RecurringObligationType.SERVICE,
                        new BigDecimal("130000.50"),
                        RecurringObligationFrequency.MONTHLY,
                        20,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2027, 8, 1),
                        "Actualizado",
                        "Obs"
                )
        );
        assertEquals("Internet fibra", updated.name());
        assertEquals(new BigDecimal("130000.50"), updated.expectedAmount());
        assertEquals(20, updated.dueDay());
        assertTrue(updated.active());

        var deactivated = deactivateRecurringFinancialObligationUseCase.execute(
                new DeactivateRecurringFinancialObligationCommand(created.obligationId())
        );
        assertFalse(deactivated.active());

        GetRecurringFinancialObligationResult stillReadable = getRecurringFinancialObligationUseCase.execute(
                new GetRecurringFinancialObligationQuery(created.obligationId())
        );
        assertFalse(stillReadable.active());
        assertEquals("Internet fibra", stillReadable.name());

        assertEquals(
                transactionsBefore,
                financialTransactionRepository.findAllNewestFirst().size(),
                "Obligation lifecycle must not create FinancialTransactions"
        );
    }

    @Test
    void rejectsNotFound() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getRecurringFinancialObligationUseCase.execute(
                        new GetRecurringFinancialObligationQuery(UUID.randomUUID())
                )
        );
        assertTrue(exception.getMessage().contains("Recurring financial obligation not found"));
    }
}
