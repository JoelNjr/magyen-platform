package com.magyen.platform.finance.presentation.payroll.controller;

import com.magyen.platform.finance.application.dto.ActivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.ActivatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeFinancialSummaryQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeFinancialSummaryResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeePerformanceQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeePerformanceResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeesQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeesResult;
import com.magyen.platform.finance.application.dto.UpdatePayrollEmployeeCompensationCommand;
import com.magyen.platform.finance.application.dto.UpdatePayrollEmployeeCompensationResult;
import com.magyen.platform.finance.application.usecase.ActivatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.DeactivatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeCommissionsUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeFinancialSummaryUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeePerformanceUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeProductionEarningsUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeesUseCase;
import com.magyen.platform.finance.application.usecase.UpdatePayrollEmployeeCompensationUseCase;
import com.magyen.platform.finance.presentation.payroll.mapper.PayrollEmployeePresentationMapper;
import com.magyen.platform.finance.presentation.payroll.request.CreatePayrollEmployeeRequest;
import com.magyen.platform.finance.presentation.payroll.request.UpdatePayrollEmployeeCompensationRequest;
import com.magyen.platform.finance.presentation.payroll.response.ActivatePayrollEmployeeResponse;
import com.magyen.platform.finance.presentation.payroll.response.DeactivatePayrollEmployeeResponse;
import com.magyen.platform.finance.presentation.payroll.response.GetPayrollEmployeesResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollEmployeeCommissionsResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollEmployeeFinancialSummaryResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollEmployeePerformanceResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollEmployeeProductionEarningsResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollEmployeeResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Expone la API REST de empleados de nómina.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 * Crear un empleado no crea movimientos del ledger ni períodos.
 */
@RestController
@RequestMapping("/api/v1/finance/payroll/employees")
public class PayrollEmployeeController {

    private final CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;
    private final GetPayrollEmployeeUseCase getPayrollEmployeeUseCase;
    private final GetPayrollEmployeesUseCase getPayrollEmployeesUseCase;
    private final GetPayrollEmployeeProductionEarningsUseCase getPayrollEmployeeProductionEarningsUseCase;
    private final GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase;
    private final GetPayrollEmployeePerformanceUseCase getPayrollEmployeePerformanceUseCase;
    private final GetPayrollEmployeeFinancialSummaryUseCase getPayrollEmployeeFinancialSummaryUseCase;
    private final UpdatePayrollEmployeeCompensationUseCase updatePayrollEmployeeCompensationUseCase;
    private final ActivatePayrollEmployeeUseCase activatePayrollEmployeeUseCase;
    private final DeactivatePayrollEmployeeUseCase deactivatePayrollEmployeeUseCase;
    private final PayrollEmployeePresentationMapper payrollEmployeePresentationMapper;

    public PayrollEmployeeController(
            CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase,
            GetPayrollEmployeeUseCase getPayrollEmployeeUseCase,
            GetPayrollEmployeesUseCase getPayrollEmployeesUseCase,
            GetPayrollEmployeeProductionEarningsUseCase getPayrollEmployeeProductionEarningsUseCase,
            GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase,
            GetPayrollEmployeePerformanceUseCase getPayrollEmployeePerformanceUseCase,
            GetPayrollEmployeeFinancialSummaryUseCase getPayrollEmployeeFinancialSummaryUseCase,
            UpdatePayrollEmployeeCompensationUseCase updatePayrollEmployeeCompensationUseCase,
            ActivatePayrollEmployeeUseCase activatePayrollEmployeeUseCase,
            DeactivatePayrollEmployeeUseCase deactivatePayrollEmployeeUseCase,
            PayrollEmployeePresentationMapper payrollEmployeePresentationMapper
    ) {
        this.createPayrollEmployeeUseCase = createPayrollEmployeeUseCase;
        this.getPayrollEmployeeUseCase = getPayrollEmployeeUseCase;
        this.getPayrollEmployeesUseCase = getPayrollEmployeesUseCase;
        this.getPayrollEmployeeProductionEarningsUseCase = getPayrollEmployeeProductionEarningsUseCase;
        this.getPayrollEmployeeCommissionsUseCase = getPayrollEmployeeCommissionsUseCase;
        this.getPayrollEmployeePerformanceUseCase = getPayrollEmployeePerformanceUseCase;
        this.getPayrollEmployeeFinancialSummaryUseCase = getPayrollEmployeeFinancialSummaryUseCase;
        this.updatePayrollEmployeeCompensationUseCase = updatePayrollEmployeeCompensationUseCase;
        this.activatePayrollEmployeeUseCase = activatePayrollEmployeeUseCase;
        this.deactivatePayrollEmployeeUseCase = deactivatePayrollEmployeeUseCase;
        this.payrollEmployeePresentationMapper = payrollEmployeePresentationMapper;
    }

    @PostMapping
    public ResponseEntity<PayrollEmployeeResponse> createEmployee(
            @RequestBody CreatePayrollEmployeeRequest request
    ) {
        CreatePayrollEmployeeCommand command = payrollEmployeePresentationMapper.toCommand(request);
        CreatePayrollEmployeeResult result = createPayrollEmployeeUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payrollEmployeePresentationMapper.toResponse(result));
    }

    @GetMapping
    public ResponseEntity<GetPayrollEmployeesResponse> getEmployees(
            @RequestParam(required = false) Boolean active
    ) {
        GetPayrollEmployeesQuery query = payrollEmployeePresentationMapper.toListQuery(active);
        GetPayrollEmployeesResult result = getPayrollEmployeesUseCase.execute(query);
        return ResponseEntity.ok(payrollEmployeePresentationMapper.toResponse(result));
    }

    @GetMapping("/performance")
    public ResponseEntity<PayrollEmployeePerformanceResponse> getSellerPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetPayrollEmployeePerformanceQuery query =
                payrollEmployeePresentationMapper.toPerformanceQuery(fromDate, toDate);
        GetPayrollEmployeePerformanceResult result = getPayrollEmployeePerformanceUseCase.execute(query);
        return ResponseEntity.ok(payrollEmployeePresentationMapper.toResponse(result));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<PayrollEmployeeResponse> getEmployee(@PathVariable UUID employeeId) {
        GetPayrollEmployeeQuery query = payrollEmployeePresentationMapper.toGetQuery(employeeId);
        GetPayrollEmployeeResult result = getPayrollEmployeeUseCase.execute(query);
        return ResponseEntity.ok(payrollEmployeePresentationMapper.toResponse(result));
    }

    @GetMapping("/{employeeId}/production-earnings")
    public ResponseEntity<PayrollEmployeeProductionEarningsResponse> getProductionEarnings(
            @PathVariable UUID employeeId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate
    ) {
        GetPayrollEmployeeProductionEarningsQuery query =
                payrollEmployeePresentationMapper.toEarningsQuery(employeeId, fromDate, toDate);
        GetPayrollEmployeeProductionEarningsResult result =
                getPayrollEmployeeProductionEarningsUseCase.execute(query);
        return ResponseEntity.ok(payrollEmployeePresentationMapper.toResponse(result));
    }

    @GetMapping("/{employeeId}/commissions")
    public ResponseEntity<PayrollEmployeeCommissionsResponse> getCommissions(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetPayrollEmployeeCommissionsQuery query =
                payrollEmployeePresentationMapper.toCommissionsQuery(employeeId, fromDate, toDate);
        GetPayrollEmployeeCommissionsResult result = getPayrollEmployeeCommissionsUseCase.execute(query);
        return ResponseEntity.ok(payrollEmployeePresentationMapper.toResponse(result));
    }

    @GetMapping("/{employeeId}/summary")
    public ResponseEntity<PayrollEmployeeFinancialSummaryResponse> getFinancialSummary(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetPayrollEmployeeFinancialSummaryQuery query =
                payrollEmployeePresentationMapper.toSummaryQuery(employeeId, fromDate, toDate);
        GetPayrollEmployeeFinancialSummaryResult result = getPayrollEmployeeFinancialSummaryUseCase.execute(query);
        return ResponseEntity.ok(payrollEmployeePresentationMapper.toResponse(result));
    }

    @PutMapping("/{employeeId}/compensation")
    public ResponseEntity<PayrollEmployeeResponse> updateCompensation(
            @PathVariable UUID employeeId,
            @RequestBody UpdatePayrollEmployeeCompensationRequest request
    ) {
        UpdatePayrollEmployeeCompensationCommand command =
                payrollEmployeePresentationMapper.toUpdateCommand(employeeId, request);
        UpdatePayrollEmployeeCompensationResult result =
                updatePayrollEmployeeCompensationUseCase.execute(command);
        return ResponseEntity.ok(payrollEmployeePresentationMapper.toResponse(result));
    }

    @PatchMapping("/{employeeId}/activate")
    public ResponseEntity<ActivatePayrollEmployeeResponse> activateEmployee(
            @PathVariable UUID employeeId
    ) {
        ActivatePayrollEmployeeCommand command =
                payrollEmployeePresentationMapper.toActivateCommand(employeeId);
        ActivatePayrollEmployeeResult result = activatePayrollEmployeeUseCase.execute(command);
        return ResponseEntity.ok(payrollEmployeePresentationMapper.toResponse(result));
    }

    @PatchMapping("/{employeeId}/deactivate")
    public ResponseEntity<DeactivatePayrollEmployeeResponse> deactivateEmployee(
            @PathVariable UUID employeeId
    ) {
        DeactivatePayrollEmployeeCommand command =
                payrollEmployeePresentationMapper.toDeactivateCommand(employeeId);
        DeactivatePayrollEmployeeResult result = deactivatePayrollEmployeeUseCase.execute(command);
        return ResponseEntity.ok(payrollEmployeePresentationMapper.toResponse(result));
    }
}
