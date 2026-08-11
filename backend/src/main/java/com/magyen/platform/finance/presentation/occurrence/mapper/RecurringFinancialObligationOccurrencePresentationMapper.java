package com.magyen.platform.finance.presentation.occurrence.mapper;

import com.magyen.platform.finance.application.dto.CancelRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CancelRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.FinancialObligationOccurrenceCommitmentResult;
import com.magyen.platform.finance.application.dto.GenerateRecurringFinancialObligationOccurrencesCommand;
import com.magyen.platform.finance.application.dto.GenerateRecurringFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetOverdueFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetOverdueFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetPendingFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetPendingFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrenceQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetUpcomingFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetUpcomingFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.presentation.occurrence.request.CreateRecurringFinancialObligationOccurrenceRequest;
import com.magyen.platform.finance.presentation.occurrence.request.GenerateRecurringFinancialObligationOccurrencesRequest;
import com.magyen.platform.finance.presentation.occurrence.request.PayRecurringFinancialObligationOccurrenceRequest;
import com.magyen.platform.finance.presentation.occurrence.response.CancelRecurringFinancialObligationOccurrenceResponse;
import com.magyen.platform.finance.presentation.occurrence.response.FinancialObligationOccurrenceCommitmentResponse;
import com.magyen.platform.finance.presentation.occurrence.response.GenerateRecurringFinancialObligationOccurrencesResponse;
import com.magyen.platform.finance.presentation.occurrence.response.GetOverdueFinancialObligationOccurrencesResponse;
import com.magyen.platform.finance.presentation.occurrence.response.GetPendingFinancialObligationOccurrencesResponse;
import com.magyen.platform.finance.presentation.occurrence.response.GetRecurringFinancialObligationOccurrencesResponse;
import com.magyen.platform.finance.presentation.occurrence.response.GetUpcomingFinancialObligationOccurrencesResponse;
import com.magyen.platform.finance.presentation.occurrence.response.PayRecurringFinancialObligationOccurrenceResponse;
import com.magyen.platform.finance.presentation.occurrence.response.RecurringFinancialObligationOccurrenceResponse;

import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP y DTOs de Application para ocurrencias.
 */
public class RecurringFinancialObligationOccurrencePresentationMapper {

    public CreateRecurringFinancialObligationOccurrenceCommand toCommand(
            CreateRecurringFinancialObligationOccurrenceRequest request
    ) {
        Objects.requireNonNull(request, "Create request must not be null");
        return new CreateRecurringFinancialObligationOccurrenceCommand(
                request.recurringObligationId(),
                request.dueDate(),
                request.observation()
        );
    }

    public GenerateRecurringFinancialObligationOccurrencesCommand toGenerateCommand(
            GenerateRecurringFinancialObligationOccurrencesRequest request
    ) {
        Objects.requireNonNull(request, "Generate request must not be null");
        return new GenerateRecurringFinancialObligationOccurrencesCommand(
                request.fromDate(),
                request.toDate()
        );
    }

    public GenerateRecurringFinancialObligationOccurrencesResponse toResponse(
            GenerateRecurringFinancialObligationOccurrencesResult result
    ) {
        Objects.requireNonNull(result, "Generate result must not be null");
        return new GenerateRecurringFinancialObligationOccurrencesResponse(
                result.requestedFrom(),
                result.requestedTo(),
                result.obligationsEvaluated(),
                result.occurrencesCreated(),
                result.occurrencesAlreadyExisting(),
                result.occurrencesSkippedInactive(),
                result.occurrencesSkippedOutsideValidity(),
                result.createdOccurrences().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    public GetRecurringFinancialObligationOccurrenceQuery toGetQuery(UUID occurrenceId) {
        Objects.requireNonNull(occurrenceId, "Occurrence id must not be null");
        return new GetRecurringFinancialObligationOccurrenceQuery(occurrenceId);
    }

    public GetRecurringFinancialObligationOccurrencesQuery toListQuery(String status) {
        if (status == null || status.isBlank()) {
            return GetRecurringFinancialObligationOccurrencesQuery.all();
        }
        return new GetRecurringFinancialObligationOccurrencesQuery(
                RecurringObligationOccurrenceStatus.of(status)
        );
    }

    public PayRecurringFinancialObligationOccurrenceCommand toPayCommand(
            UUID occurrenceId,
            PayRecurringFinancialObligationOccurrenceRequest request
    ) {
        Objects.requireNonNull(occurrenceId, "Occurrence id must not be null");
        if (request == null) {
            return new PayRecurringFinancialObligationOccurrenceCommand(occurrenceId);
        }
        return new PayRecurringFinancialObligationOccurrenceCommand(
                occurrenceId,
                request.paidAt(),
                request.observation()
        );
    }

    public CancelRecurringFinancialObligationOccurrenceCommand toCancelCommand(UUID occurrenceId) {
        Objects.requireNonNull(occurrenceId, "Occurrence id must not be null");
        return new CancelRecurringFinancialObligationOccurrenceCommand(occurrenceId);
    }

    public RecurringFinancialObligationOccurrenceResponse toResponse(
            CreateRecurringFinancialObligationOccurrenceResult result
    ) {
        Objects.requireNonNull(result, "Create result must not be null");
        return mapOccurrence(
                result.occurrenceId(),
                result.recurringObligationId(),
                result.dueDate(),
                result.expectedAmount(),
                result.status().name(),
                result.paidDate(),
                result.financialTransactionId(),
                result.observation()
        );
    }

    public RecurringFinancialObligationOccurrenceResponse toResponse(
            GetRecurringFinancialObligationOccurrenceResult result
    ) {
        Objects.requireNonNull(result, "Get result must not be null");
        return mapOccurrence(
                result.occurrenceId(),
                result.recurringObligationId(),
                result.dueDate(),
                result.expectedAmount(),
                result.status().name(),
                result.paidDate(),
                result.financialTransactionId(),
                result.observation()
        );
    }

    public GetRecurringFinancialObligationOccurrencesResponse toResponse(
            GetRecurringFinancialObligationOccurrencesResult result
    ) {
        Objects.requireNonNull(result, "List result must not be null");
        return new GetRecurringFinancialObligationOccurrencesResponse(
                result.occurrences().stream().map(this::toResponse).toList()
        );
    }

    public PayRecurringFinancialObligationOccurrenceResponse toResponse(
            PayRecurringFinancialObligationOccurrenceResult result
    ) {
        Objects.requireNonNull(result, "Pay result must not be null");
        return new PayRecurringFinancialObligationOccurrenceResponse(
                result.occurrenceId(),
                result.recurringObligationId(),
                result.dueDate(),
                result.expectedAmount(),
                result.status().name(),
                result.paidDate(),
                result.financialTransactionId(),
                result.transactionAmount(),
                result.transactionCategory()
        );
    }

    public CancelRecurringFinancialObligationOccurrenceResponse toResponse(
            CancelRecurringFinancialObligationOccurrenceResult result
    ) {
        Objects.requireNonNull(result, "Cancel result must not be null");
        return new CancelRecurringFinancialObligationOccurrenceResponse(
                result.occurrenceId(),
                result.status().name()
        );
    }

    public GetPendingFinancialObligationOccurrencesQuery toPendingQuery() {
        return GetPendingFinancialObligationOccurrencesQuery.create();
    }

    public GetOverdueFinancialObligationOccurrencesQuery toOverdueQuery() {
        return GetOverdueFinancialObligationOccurrencesQuery.create();
    }

    public GetUpcomingFinancialObligationOccurrencesQuery toUpcomingQuery(Integer daysAhead) {
        return new GetUpcomingFinancialObligationOccurrencesQuery(daysAhead);
    }

    public GetPendingFinancialObligationOccurrencesResponse toResponse(
            GetPendingFinancialObligationOccurrencesResult result
    ) {
        Objects.requireNonNull(result, "Pending result must not be null");
        return new GetPendingFinancialObligationOccurrencesResponse(
                result.occurrences().stream().map(this::toCommitmentResponse).toList(),
                result.totalPendingAmount()
        );
    }

    public GetOverdueFinancialObligationOccurrencesResponse toResponse(
            GetOverdueFinancialObligationOccurrencesResult result
    ) {
        Objects.requireNonNull(result, "Overdue result must not be null");
        return new GetOverdueFinancialObligationOccurrencesResponse(
                result.occurrences().stream().map(this::toCommitmentResponse).toList(),
                result.totalOverdueAmount()
        );
    }

    public GetUpcomingFinancialObligationOccurrencesResponse toResponse(
            GetUpcomingFinancialObligationOccurrencesResult result
    ) {
        Objects.requireNonNull(result, "Upcoming result must not be null");
        return new GetUpcomingFinancialObligationOccurrencesResponse(
                result.occurrences().stream().map(this::toCommitmentResponse).toList()
        );
    }

    private FinancialObligationOccurrenceCommitmentResponse toCommitmentResponse(
            FinancialObligationOccurrenceCommitmentResult result
    ) {
        return new FinancialObligationOccurrenceCommitmentResponse(
                result.occurrenceId(),
                result.recurringObligationId(),
                result.obligationName(),
                result.obligationType().name(),
                result.dueDate(),
                result.expectedAmount(),
                result.status().name(),
                result.overdue(),
                result.daysUntilDue(),
                result.daysOverdue()
        );
    }

    private RecurringFinancialObligationOccurrenceResponse mapOccurrence(
            java.util.UUID occurrenceId,
            java.util.UUID recurringObligationId,
            java.time.LocalDate dueDate,
            java.math.BigDecimal expectedAmount,
            String status,
            java.time.LocalDateTime paidDate,
            java.util.UUID financialTransactionId,
            String observation
    ) {
        if (status == null || status.isBlank()) {
            throw new FinanceDomainException("Occurrence status must not be blank");
        }
        return new RecurringFinancialObligationOccurrenceResponse(
                occurrenceId,
                recurringObligationId,
                dueDate,
                expectedAmount,
                status,
                paidDate,
                financialTransactionId,
                observation
        );
    }
}
