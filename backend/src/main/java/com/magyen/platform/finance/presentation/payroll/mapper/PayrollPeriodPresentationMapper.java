package com.magyen.platform.finance.presentation.payroll.mapper;

import com.magyen.platform.finance.application.dto.CancelPayrollPeriodCommand;
import com.magyen.platform.finance.application.dto.CancelPayrollPeriodResult;
import com.magyen.platform.finance.application.dto.GeneratePayrollPeriodsCommand;
import com.magyen.platform.finance.application.dto.GeneratePayrollPeriodsResult;
import com.magyen.platform.finance.application.dto.GetPayrollPeriodQuery;
import com.magyen.platform.finance.application.dto.GetPayrollPeriodResult;
import com.magyen.platform.finance.application.dto.GetPayrollPeriodsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollPeriodsResult;
import com.magyen.platform.finance.application.dto.PayPayrollPeriodCommand;
import com.magyen.platform.finance.application.dto.PayPayrollPeriodResult;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.presentation.payroll.request.GeneratePayrollPeriodsRequest;
import com.magyen.platform.finance.presentation.payroll.request.PayPayrollPeriodRequest;
import com.magyen.platform.finance.presentation.payroll.response.CancelPayrollPeriodResponse;
import com.magyen.platform.finance.presentation.payroll.response.GeneratePayrollPeriodsResponse;
import com.magyen.platform.finance.presentation.payroll.response.GetPayrollPeriodsResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayPayrollPeriodResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollPeriodResponse;

import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP y DTOs de Application para períodos de nómina.
 */
public class PayrollPeriodPresentationMapper {

    public GeneratePayrollPeriodsCommand toGenerateCommand(GeneratePayrollPeriodsRequest request) {
        Objects.requireNonNull(request, "Generate request must not be null");
        return new GeneratePayrollPeriodsCommand(request.fromDate(), request.toDate());
    }

    public GetPayrollPeriodQuery toGetQuery(UUID periodId) {
        Objects.requireNonNull(periodId, "Period id must not be null");
        return new GetPayrollPeriodQuery(periodId);
    }

    public GetPayrollPeriodsQuery toListQuery() {
        return GetPayrollPeriodsQuery.all();
    }

    public PayPayrollPeriodCommand toPayCommand(UUID periodId, PayPayrollPeriodRequest request) {
        Objects.requireNonNull(periodId, "Period id must not be null");
        if (request == null) {
            return new PayPayrollPeriodCommand(periodId);
        }
        return new PayPayrollPeriodCommand(periodId, request.paidAt(), request.observation());
    }

    public CancelPayrollPeriodCommand toCancelCommand(UUID periodId) {
        Objects.requireNonNull(periodId, "Period id must not be null");
        return new CancelPayrollPeriodCommand(periodId);
    }

    public GeneratePayrollPeriodsResponse toResponse(GeneratePayrollPeriodsResult result) {
        Objects.requireNonNull(result, "Generate result must not be null");
        return new GeneratePayrollPeriodsResponse(
                result.requestedFrom(),
                result.requestedTo(),
                result.employeesEvaluated(),
                result.created(),
                result.alreadyExisting(),
                result.skippedInactive(),
                result.skippedProductionBased(),
                result.skippedOutsideValidity(),
                result.createdPeriods().stream().map(this::toResponse).toList()
        );
    }

    public PayrollPeriodResponse toResponse(GetPayrollPeriodResult result) {
        Objects.requireNonNull(result, "Get result must not be null");
        return mapPeriod(
                result.periodId(),
                result.employeeId(),
                result.employeeDisplayName(),
                result.periodStart(),
                result.periodEnd(),
                result.expectedPaymentDate(),
                result.amountSnapshot(),
                result.status().name(),
                result.actualPaymentDate(),
                result.paidAt(),
                result.financialTransactionId()
        );
    }

    public GetPayrollPeriodsResponse toResponse(GetPayrollPeriodsResult result) {
        Objects.requireNonNull(result, "List result must not be null");
        return new GetPayrollPeriodsResponse(
                result.periods().stream().map(this::toResponse).toList()
        );
    }

    public PayPayrollPeriodResponse toResponse(PayPayrollPeriodResult result) {
        Objects.requireNonNull(result, "Pay result must not be null");
        return new PayPayrollPeriodResponse(
                result.periodId(),
                result.employeeId(),
                result.periodStart(),
                result.periodEnd(),
                result.amountSnapshot(),
                result.status().name(),
                result.actualPaymentDate(),
                result.paidAt(),
                result.financialTransactionId(),
                result.transactionAmount(),
                result.transactionCategory()
        );
    }

    public CancelPayrollPeriodResponse toResponse(CancelPayrollPeriodResult result) {
        Objects.requireNonNull(result, "Cancel result must not be null");
        return new CancelPayrollPeriodResponse(result.periodId(), result.status().name());
    }

    private PayrollPeriodResponse mapPeriod(
            UUID periodId,
            UUID employeeId,
            String employeeDisplayName,
            java.time.LocalDate periodStart,
            java.time.LocalDate periodEnd,
            java.time.LocalDate expectedPaymentDate,
            java.math.BigDecimal amountSnapshot,
            String status,
            java.time.LocalDate actualPaymentDate,
            java.time.LocalDateTime paidAt,
            UUID financialTransactionId
    ) {
        if (status == null || status.isBlank()) {
            throw new FinanceDomainException("Period status must not be blank");
        }
        return new PayrollPeriodResponse(
                periodId,
                employeeId,
                employeeDisplayName,
                periodStart,
                periodEnd,
                expectedPaymentDate,
                amountSnapshot,
                status,
                actualPaymentDate,
                paidAt,
                financialTransactionId
        );
    }
}
