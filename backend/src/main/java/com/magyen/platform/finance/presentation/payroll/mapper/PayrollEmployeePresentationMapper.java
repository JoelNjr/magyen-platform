package com.magyen.platform.finance.presentation.payroll.mapper;

import com.magyen.platform.finance.application.dto.ActivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.ActivatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeesQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeesResult;
import com.magyen.platform.finance.application.dto.UpdatePayrollEmployeeCompensationCommand;
import com.magyen.platform.finance.application.dto.UpdatePayrollEmployeeCompensationResult;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.presentation.payroll.request.CreatePayrollEmployeeRequest;
import com.magyen.platform.finance.presentation.payroll.request.UpdatePayrollEmployeeCompensationRequest;
import com.magyen.platform.finance.presentation.payroll.response.ActivatePayrollEmployeeResponse;
import com.magyen.platform.finance.presentation.payroll.response.DeactivatePayrollEmployeeResponse;
import com.magyen.platform.finance.presentation.payroll.response.GetPayrollEmployeesResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollEmployeeProductionEarningsResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollEmployeeResponse;

import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application (empleados).
 */
public class PayrollEmployeePresentationMapper {

    public CreatePayrollEmployeeCommand toCommand(CreatePayrollEmployeeRequest request) {
        Objects.requireNonNull(request, "CreatePayrollEmployeeRequest must not be null");

        return new CreatePayrollEmployeeCommand(
                request.displayName(),
                parseCompensationType(request.compensationType()),
                request.fixedAmount(),
                request.effectiveFrom(),
                request.effectiveTo()
        );
    }

    public UpdatePayrollEmployeeCompensationCommand toUpdateCommand(
            UUID employeeId,
            UpdatePayrollEmployeeCompensationRequest request
    ) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        Objects.requireNonNull(request, "UpdatePayrollEmployeeCompensationRequest must not be null");

        return new UpdatePayrollEmployeeCompensationCommand(
                employeeId,
                request.displayName(),
                request.fixedAmount(),
                request.effectiveFrom(),
                request.effectiveTo()
        );
    }

    public GetPayrollEmployeeQuery toGetQuery(UUID employeeId) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        return new GetPayrollEmployeeQuery(employeeId);
    }

    public GetPayrollEmployeesQuery toListQuery(Boolean active) {
        return new GetPayrollEmployeesQuery(active);
    }

    public GetPayrollEmployeeProductionEarningsQuery toEarningsQuery(
            UUID employeeId,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate
    ) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        return new GetPayrollEmployeeProductionEarningsQuery(employeeId, fromDate, toDate);
    }

    public PayrollEmployeeProductionEarningsResponse toResponse(
            GetPayrollEmployeeProductionEarningsResult result
    ) {
        Objects.requireNonNull(result, "GetPayrollEmployeeProductionEarningsResult must not be null");
        return new PayrollEmployeeProductionEarningsResponse(
                result.employeeId(),
                result.displayName(),
                result.compensationType().name(),
                result.productionLaborApplicable(),
                result.fromDate(),
                result.toDate(),
                result.laborWorkCount(),
                result.totalQuantity(),
                result.totalCalculatedAmount(),
                result.totalPaidAmount(),
                result.totalPendingAmount()
        );
    }

    public ActivatePayrollEmployeeCommand toActivateCommand(UUID employeeId) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        return new ActivatePayrollEmployeeCommand(employeeId);
    }

    public DeactivatePayrollEmployeeCommand toDeactivateCommand(UUID employeeId) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        return new DeactivatePayrollEmployeeCommand(employeeId);
    }

    public PayrollEmployeeResponse toResponse(CreatePayrollEmployeeResult result) {
        Objects.requireNonNull(result, "CreatePayrollEmployeeResult must not be null");
        return mapEmployee(
                result.employeeId(),
                result.displayName(),
                result.active(),
                result.compensationType().name(),
                result.fixedAmount(),
                result.frequency() == null ? null : result.frequency().name(),
                result.effectiveFrom(),
                result.effectiveTo()
        );
    }

    public PayrollEmployeeResponse toResponse(GetPayrollEmployeeResult result) {
        Objects.requireNonNull(result, "GetPayrollEmployeeResult must not be null");
        return mapEmployee(
                result.employeeId(),
                result.displayName(),
                result.active(),
                result.compensationType().name(),
                result.fixedAmount(),
                result.frequency() == null ? null : result.frequency().name(),
                result.effectiveFrom(),
                result.effectiveTo()
        );
    }

    public PayrollEmployeeResponse toResponse(UpdatePayrollEmployeeCompensationResult result) {
        Objects.requireNonNull(result, "UpdatePayrollEmployeeCompensationResult must not be null");
        return mapEmployee(
                result.employeeId(),
                result.displayName(),
                result.active(),
                result.compensationType().name(),
                result.fixedAmount(),
                result.frequency() == null ? null : result.frequency().name(),
                result.effectiveFrom(),
                result.effectiveTo()
        );
    }

    public GetPayrollEmployeesResponse toResponse(GetPayrollEmployeesResult result) {
        Objects.requireNonNull(result, "GetPayrollEmployeesResult must not be null");
        return new GetPayrollEmployeesResponse(
                result.employees().stream().map(this::toResponse).toList()
        );
    }

    public ActivatePayrollEmployeeResponse toResponse(ActivatePayrollEmployeeResult result) {
        Objects.requireNonNull(result, "ActivatePayrollEmployeeResult must not be null");
        return new ActivatePayrollEmployeeResponse(result.employeeId(), result.active());
    }

    public DeactivatePayrollEmployeeResponse toResponse(DeactivatePayrollEmployeeResult result) {
        Objects.requireNonNull(result, "DeactivatePayrollEmployeeResult must not be null");
        return new DeactivatePayrollEmployeeResponse(result.employeeId(), result.active());
    }

    private PayrollEmployeeResponse mapEmployee(
            UUID employeeId,
            String displayName,
            boolean active,
            String compensationType,
            java.math.BigDecimal fixedAmount,
            String frequency,
            java.time.LocalDate effectiveFrom,
            java.time.LocalDate effectiveTo
    ) {
        boolean canSell = "FIXED_PAYROLL".equals(compensationType);
        boolean canDoProduction = "PRODUCTION_BASED".equals(compensationType);
        return new PayrollEmployeeResponse(
                employeeId,
                displayName,
                active,
                compensationType,
                fixedAmount,
                frequency,
                effectiveFrom,
                effectiveTo,
                canSell,
                canDoProduction
        );
    }

    private PayrollCompensationType parseCompensationType(String compensationType) {
        if (compensationType == null || compensationType.isBlank()) {
            throw new FinanceDomainException("Compensation type must not be blank");
        }
        return PayrollCompensationType.of(compensationType);
    }
}
