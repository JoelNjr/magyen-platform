package com.magyen.platform.finance.presentation.transaction.mapper;

import com.magyen.platform.finance.application.dto.GetFinancialTransactionQuery;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionResult;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionsResult;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionResult;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.presentation.transaction.request.RegisterFinancialTransactionRequest;
import com.magyen.platform.finance.presentation.transaction.response.FinancialTransactionResponse;
import com.magyen.platform.finance.presentation.transaction.response.GetFinancialTransactionsResponse;

import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 */
public class FinancialTransactionPresentationMapper {

    public RegisterFinancialTransactionCommand toCommand(RegisterFinancialTransactionRequest request) {
        Objects.requireNonNull(request, "RegisterFinancialTransactionRequest must not be null");

        if (request.type() == null || request.type().isBlank()) {
            throw new FinanceDomainException("Transaction type must not be blank");
        }

        return new RegisterFinancialTransactionCommand(
                FinancialTransactionType.of(request.type()),
                request.amount(),
                request.transactionDate(),
                request.category(),
                request.description(),
                request.observation(),
                parseSourceType(request.sourceType()),
                request.sourceId()
        );
    }

    public FinancialTransactionResponse toResponse(RegisterFinancialTransactionResult result) {
        Objects.requireNonNull(result, "RegisterFinancialTransactionResult must not be null");

        return new FinancialTransactionResponse(
                result.transactionId(),
                result.type().name(),
                result.amount(),
                result.transactionDate(),
                result.category(),
                result.description(),
                result.observation(),
                result.sourceType().name(),
                result.sourceId()
        );
    }

    public GetFinancialTransactionQuery toGetFinancialTransactionQuery(UUID transactionId) {
        Objects.requireNonNull(transactionId, "Transaction id must not be null");
        return new GetFinancialTransactionQuery(transactionId);
    }

    public FinancialTransactionResponse toResponse(GetFinancialTransactionResult result) {
        Objects.requireNonNull(result, "GetFinancialTransactionResult must not be null");

        return new FinancialTransactionResponse(
                result.transactionId(),
                result.type().name(),
                result.amount(),
                result.transactionDate(),
                result.category(),
                result.description(),
                result.observation(),
                result.sourceType().name(),
                result.sourceId()
        );
    }

    public GetFinancialTransactionsResponse toResponse(GetFinancialTransactionsResult result) {
        Objects.requireNonNull(result, "GetFinancialTransactionsResult must not be null");

        return new GetFinancialTransactionsResponse(
                result.transactions().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    private FinancialTransactionSourceType parseSourceType(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) {
            return FinancialTransactionSourceType.MANUAL;
        }
        return FinancialTransactionSourceType.of(sourceType);
    }
}
