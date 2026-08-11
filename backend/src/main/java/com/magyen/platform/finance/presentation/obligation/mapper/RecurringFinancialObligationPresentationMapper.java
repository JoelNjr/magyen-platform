package com.magyen.platform.finance.presentation.obligation.mapper;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.DeactivateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.DeactivateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationsQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationsResult;
import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationResult;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.presentation.obligation.request.CreateRecurringFinancialObligationRequest;
import com.magyen.platform.finance.presentation.obligation.request.UpdateRecurringFinancialObligationRequest;
import com.magyen.platform.finance.presentation.obligation.response.DeactivateRecurringFinancialObligationResponse;
import com.magyen.platform.finance.presentation.obligation.response.GetRecurringFinancialObligationsResponse;
import com.magyen.platform.finance.presentation.obligation.response.RecurringFinancialObligationResponse;

import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 */
public class RecurringFinancialObligationPresentationMapper {

    public CreateRecurringFinancialObligationCommand toCommand(
            CreateRecurringFinancialObligationRequest request
    ) {
        Objects.requireNonNull(request, "CreateRecurringFinancialObligationRequest must not be null");

        return new CreateRecurringFinancialObligationCommand(
                request.name(),
                parseType(request.type()),
                request.expectedAmount(),
                parseFrequency(request.frequency()),
                request.dueDay(),
                request.startDate(),
                request.endDate(),
                request.description(),
                request.observation()
        );
    }

    public UpdateRecurringFinancialObligationCommand toUpdateCommand(
            UUID obligationId,
            UpdateRecurringFinancialObligationRequest request
    ) {
        Objects.requireNonNull(obligationId, "Obligation id must not be null");
        Objects.requireNonNull(request, "UpdateRecurringFinancialObligationRequest must not be null");

        return new UpdateRecurringFinancialObligationCommand(
                obligationId,
                request.name(),
                parseType(request.type()),
                request.expectedAmount(),
                parseFrequency(request.frequency()),
                request.dueDay(),
                request.startDate(),
                request.endDate(),
                request.description(),
                request.observation()
        );
    }

    public GetRecurringFinancialObligationQuery toGetQuery(UUID obligationId) {
        Objects.requireNonNull(obligationId, "Obligation id must not be null");
        return new GetRecurringFinancialObligationQuery(obligationId);
    }

    public GetRecurringFinancialObligationsQuery toListQuery(Boolean activeOnly) {
        return new GetRecurringFinancialObligationsQuery(activeOnly);
    }

    public DeactivateRecurringFinancialObligationCommand toDeactivateCommand(UUID obligationId) {
        Objects.requireNonNull(obligationId, "Obligation id must not be null");
        return new DeactivateRecurringFinancialObligationCommand(obligationId);
    }

    public RecurringFinancialObligationResponse toResponse(CreateRecurringFinancialObligationResult result) {
        Objects.requireNonNull(result, "CreateRecurringFinancialObligationResult must not be null");
        return new RecurringFinancialObligationResponse(
                result.obligationId(),
                result.name(),
                result.type().name(),
                result.expectedAmount(),
                result.frequency().name(),
                result.dueDay(),
                result.startDate(),
                result.endDate(),
                result.active(),
                result.description(),
                result.observation()
        );
    }

    public RecurringFinancialObligationResponse toResponse(GetRecurringFinancialObligationResult result) {
        Objects.requireNonNull(result, "GetRecurringFinancialObligationResult must not be null");
        return new RecurringFinancialObligationResponse(
                result.obligationId(),
                result.name(),
                result.type().name(),
                result.expectedAmount(),
                result.frequency().name(),
                result.dueDay(),
                result.startDate(),
                result.endDate(),
                result.active(),
                result.description(),
                result.observation()
        );
    }

    public RecurringFinancialObligationResponse toResponse(UpdateRecurringFinancialObligationResult result) {
        Objects.requireNonNull(result, "UpdateRecurringFinancialObligationResult must not be null");
        return new RecurringFinancialObligationResponse(
                result.obligationId(),
                result.name(),
                result.type().name(),
                result.expectedAmount(),
                result.frequency().name(),
                result.dueDay(),
                result.startDate(),
                result.endDate(),
                result.active(),
                result.description(),
                result.observation()
        );
    }

    public GetRecurringFinancialObligationsResponse toResponse(GetRecurringFinancialObligationsResult result) {
        Objects.requireNonNull(result, "GetRecurringFinancialObligationsResult must not be null");
        return new GetRecurringFinancialObligationsResponse(
                result.obligations().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    public DeactivateRecurringFinancialObligationResponse toResponse(
            DeactivateRecurringFinancialObligationResult result
    ) {
        Objects.requireNonNull(result, "DeactivateRecurringFinancialObligationResult must not be null");
        return new DeactivateRecurringFinancialObligationResponse(
                result.obligationId(),
                result.active()
        );
    }

    private RecurringObligationType parseType(String type) {
        if (type == null || type.isBlank()) {
            throw new FinanceDomainException("Obligation type must not be blank");
        }
        return RecurringObligationType.of(type);
    }

    private RecurringObligationFrequency parseFrequency(String frequency) {
        if (frequency == null || frequency.isBlank()) {
            throw new FinanceDomainException("Frequency must not be blank");
        }
        return RecurringObligationFrequency.of(frequency);
    }
}
