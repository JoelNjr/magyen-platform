package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CancelRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CancelRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Cancela una ocurrencia PENDING. No genera movimientos del ledger.
 */
public class CancelRecurringFinancialObligationOccurrenceUseCase {

    private final RecurringFinancialObligationOccurrenceRepository occurrenceRepository;

    public CancelRecurringFinancialObligationOccurrenceUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository
    ) {
        this.occurrenceRepository = Objects.requireNonNull(
                occurrenceRepository,
                "Occurrence repository must not be null"
        );
    }

    @Transactional
    public CancelRecurringFinancialObligationOccurrenceResult execute(
            CancelRecurringFinancialObligationOccurrenceCommand command
    ) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.occurrenceId(), "Occurrence id must not be null");

        RecurringFinancialObligationOccurrence occurrence = occurrenceRepository
                .findById(command.occurrenceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recurring financial obligation occurrence not found: " + command.occurrenceId()
                ));

        occurrence.cancel();
        RecurringFinancialObligationOccurrence saved = occurrenceRepository.save(occurrence);

        return new CancelRecurringFinancialObligationOccurrenceResult(
                saved.getId(),
                saved.getStatus()
        );
    }
}
