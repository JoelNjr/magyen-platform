package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GenerateRecurringFinancialObligationOccurrencesCommand;
import com.magyen.platform.finance.application.dto.GenerateRecurringFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Genera de forma controlada e idempotente ocurrencias PENDING para obligaciones activas.
 * <p>
 * No crea {@code FinancialTransaction}. No usa scheduler.
 * El monto se congela como snapshot al momento de la generación.
 */
public class GenerateRecurringFinancialObligationOccurrencesUseCase {

    private static final int MAX_RANGE_DAYS_INCLUSIVE = 366;

    private final RecurringFinancialObligationRepository recurringFinancialObligationRepository;
    private final RecurringFinancialObligationOccurrenceRepository occurrenceRepository;

    public GenerateRecurringFinancialObligationOccurrencesUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository,
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository
    ) {
        this.recurringFinancialObligationRepository = Objects.requireNonNull(
                recurringFinancialObligationRepository,
                "Recurring financial obligation repository must not be null"
        );
        this.occurrenceRepository = Objects.requireNonNull(
                occurrenceRepository,
                "Occurrence repository must not be null"
        );
    }

    @Transactional
    public GenerateRecurringFinancialObligationOccurrencesResult execute(
            GenerateRecurringFinancialObligationOccurrencesCommand command
    ) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        LocalDate fromDate = command.fromDate();
        LocalDate toDate = command.toDate();

        List<RecurringFinancialObligation> obligations = recurringFinancialObligationRepository.findAll();
        Set<String> existingKeys = loadExistingOccurrenceKeys(fromDate, toDate);

        int created = 0;
        int alreadyExisting = 0;
        int skippedInactive = 0;
        int skippedOutsideValidity = 0;
        List<GetRecurringFinancialObligationOccurrenceResult> createdOccurrences = new ArrayList<>();

        for (RecurringFinancialObligation obligation : obligations) {
            if (!obligation.isActive()) {
                skippedInactive++;
                continue;
            }

            LocalDate effectiveFrom = fromDate.isBefore(obligation.getStartDate())
                    ? obligation.getStartDate()
                    : fromDate;
            LocalDate effectiveTo = obligation.getEndDate() != null && obligation.getEndDate().isBefore(toDate)
                    ? obligation.getEndDate()
                    : toDate;

            if (effectiveFrom.isAfter(effectiveTo)) {
                skippedOutsideValidity++;
                continue;
            }

            List<LocalDate> dueDates = obligation.resolveOccurrenceDueDates(fromDate, toDate);
            if (dueDates.isEmpty()) {
                skippedOutsideValidity++;
                continue;
            }

            for (LocalDate dueDate : dueDates) {
                String key = occurrenceKey(obligation.getId(), dueDate);
                if (existingKeys.contains(key)) {
                    alreadyExisting++;
                    continue;
                }

                RecurringFinancialObligationOccurrence occurrence =
                        RecurringFinancialObligationOccurrence.createPending(
                                obligation.getId(),
                                dueDate,
                                obligation.getExpectedAmount(),
                                null
                        );

                try {
                    RecurringFinancialObligationOccurrence saved = occurrenceRepository.save(occurrence);
                    existingKeys.add(key);
                    created++;
                    createdOccurrences.add(
                            RecurringFinancialObligationOccurrenceReadMapper.toGetResult(saved)
                    );
                } catch (DataIntegrityViolationException exception) {
                    alreadyExisting++;
                    existingKeys.add(key);
                }
            }
        }

        return new GenerateRecurringFinancialObligationOccurrencesResult(
                fromDate,
                toDate,
                obligations.size(),
                created,
                alreadyExisting,
                skippedInactive,
                skippedOutsideValidity,
                List.copyOf(createdOccurrences)
        );
    }

    private Set<String> loadExistingOccurrenceKeys(LocalDate fromDate, LocalDate toDate) {
        Set<String> keys = new HashSet<>();
        for (RecurringFinancialObligationOccurrence occurrence :
                occurrenceRepository.findByDueDateBetween(fromDate, toDate)) {
            keys.add(occurrenceKey(occurrence.getRecurringObligationId(), occurrence.getDueDate()));
        }
        return keys;
    }

    private static String occurrenceKey(java.util.UUID obligationId, LocalDate dueDate) {
        return obligationId + "|" + dueDate;
    }

    private void validateCommand(GenerateRecurringFinancialObligationOccurrencesCommand command) {
        if (command.fromDate() == null) {
            throw new FinanceDomainException("From date must not be null");
        }
        if (command.toDate() == null) {
            throw new FinanceDomainException("To date must not be null");
        }
        if (command.toDate().isBefore(command.fromDate())) {
            throw new FinanceDomainException("From date must not be after to date");
        }

        long inclusiveDays = ChronoUnit.DAYS.between(command.fromDate(), command.toDate()) + 1;
        if (inclusiveDays > MAX_RANGE_DAYS_INCLUSIVE) {
            throw new FinanceDomainException(
                    "Generation range must not exceed " + MAX_RANGE_DAYS_INCLUSIVE + " days"
            );
        }
    }
}
