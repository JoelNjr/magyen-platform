package com.magyen.platform.finance.presentation.payroll.mapper;

import com.magyen.platform.finance.application.dto.CancelPayrollDeductionCommand;
import com.magyen.platform.finance.application.dto.CancelPayrollDeductionResult;
import com.magyen.platform.finance.application.dto.CreatePayrollDeductionCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollDeductionResult;
import com.magyen.platform.finance.application.dto.GetPayrollDeductionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollDeductionsResult;
import com.magyen.platform.finance.application.dto.PayrollDeductionResult;
import com.magyen.platform.finance.domain.PayrollDeductionStatus;
import com.magyen.platform.finance.domain.PayrollDeductionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.presentation.payroll.request.CreatePayrollDeductionRequest;
import com.magyen.platform.finance.presentation.payroll.response.CancelPayrollDeductionResponse;
import com.magyen.platform.finance.presentation.payroll.response.GetPayrollDeductionsResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollDeductionResponse;

import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application (descuentos).
 */
public class PayrollDeductionPresentationMapper {

    public CreatePayrollDeductionCommand toCommand(UUID employeeId, CreatePayrollDeductionRequest request) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        Objects.requireNonNull(request, "CreatePayrollDeductionRequest must not be null");

        return new CreatePayrollDeductionCommand(
                employeeId,
                parseType(request.type()),
                request.amount(),
                request.deductionDate(),
                request.description()
        );
    }

    public GetPayrollDeductionsQuery toListQuery(UUID employeeId, String status) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        return new GetPayrollDeductionsQuery(employeeId, parseOptionalStatus(status));
    }

    public CancelPayrollDeductionCommand toCancelCommand(UUID employeeId, UUID deductionId) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        Objects.requireNonNull(deductionId, "Deduction id must not be null");
        return new CancelPayrollDeductionCommand(employeeId, deductionId);
    }

    public PayrollDeductionResponse toResponse(CreatePayrollDeductionResult result) {
        Objects.requireNonNull(result, "CreatePayrollDeductionResult must not be null");
        return toResponse(
                new PayrollDeductionResult(
                        result.deductionId(),
                        result.employeeId(),
                        result.type(),
                        result.amount(),
                        result.deductionDate(),
                        result.description(),
                        result.status(),
                        result.createdAt()
                )
        );
    }

    public PayrollDeductionResponse toResponse(PayrollDeductionResult result) {
        Objects.requireNonNull(result, "PayrollDeductionResult must not be null");
        return new PayrollDeductionResponse(
                result.deductionId(),
                result.employeeId(),
                result.type().name(),
                result.amount(),
                result.deductionDate(),
                result.description(),
                result.status().name(),
                result.createdAt()
        );
    }

    public GetPayrollDeductionsResponse toResponse(GetPayrollDeductionsResult result) {
        Objects.requireNonNull(result, "GetPayrollDeductionsResult must not be null");
        return new GetPayrollDeductionsResponse(
                result.employeeId(),
                result.employeeDisplayName(),
                result.deductions().stream().map(this::toResponse).toList(),
                result.activeCount(),
                result.activeTotal()
        );
    }

    public CancelPayrollDeductionResponse toResponse(CancelPayrollDeductionResult result) {
        Objects.requireNonNull(result, "CancelPayrollDeductionResult must not be null");
        return new CancelPayrollDeductionResponse(
                result.deductionId(),
                result.employeeId(),
                result.status().name()
        );
    }

    private PayrollDeductionType parseType(String type) {
        if (type == null || type.isBlank()) {
            throw new FinanceDomainException("Payroll deduction type must not be blank");
        }
        return PayrollDeductionType.of(type);
    }

    private PayrollDeductionStatus parseOptionalStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return PayrollDeductionStatus.of(status);
    }
}
