package com.magyen.platform.finance.presentation.payroll.controller;

import com.magyen.platform.finance.application.dto.CancelPayrollDeductionResult;
import com.magyen.platform.finance.application.dto.CreatePayrollDeductionCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollDeductionResult;
import com.magyen.platform.finance.application.dto.GetPayrollDeductionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollDeductionsResult;
import com.magyen.platform.finance.application.usecase.CancelPayrollDeductionUseCase;
import com.magyen.platform.finance.application.usecase.CreatePayrollDeductionUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollDeductionsUseCase;
import com.magyen.platform.finance.presentation.payroll.mapper.PayrollDeductionPresentationMapper;
import com.magyen.platform.finance.presentation.payroll.request.CreatePayrollDeductionRequest;
import com.magyen.platform.finance.presentation.payroll.response.CancelPayrollDeductionResponse;
import com.magyen.platform.finance.presentation.payroll.response.GetPayrollDeductionsResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollDeductionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de descuentos de nómina.
 * <p>
 * Registrar o cancelar un descuento no crea movimientos del ledger.
 */
@RestController
@RequestMapping("/api/v1/finance/payroll/employees/{employeeId}/deductions")
public class PayrollDeductionController {

    private final CreatePayrollDeductionUseCase createPayrollDeductionUseCase;
    private final GetPayrollDeductionsUseCase getPayrollDeductionsUseCase;
    private final CancelPayrollDeductionUseCase cancelPayrollDeductionUseCase;
    private final PayrollDeductionPresentationMapper payrollDeductionPresentationMapper;

    public PayrollDeductionController(
            CreatePayrollDeductionUseCase createPayrollDeductionUseCase,
            GetPayrollDeductionsUseCase getPayrollDeductionsUseCase,
            CancelPayrollDeductionUseCase cancelPayrollDeductionUseCase,
            PayrollDeductionPresentationMapper payrollDeductionPresentationMapper
    ) {
        this.createPayrollDeductionUseCase = createPayrollDeductionUseCase;
        this.getPayrollDeductionsUseCase = getPayrollDeductionsUseCase;
        this.cancelPayrollDeductionUseCase = cancelPayrollDeductionUseCase;
        this.payrollDeductionPresentationMapper = payrollDeductionPresentationMapper;
    }

    @PostMapping
    public ResponseEntity<PayrollDeductionResponse> createDeduction(
            @PathVariable UUID employeeId,
            @RequestBody CreatePayrollDeductionRequest request
    ) {
        CreatePayrollDeductionCommand command = payrollDeductionPresentationMapper.toCommand(employeeId, request);
        CreatePayrollDeductionResult result = createPayrollDeductionUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payrollDeductionPresentationMapper.toResponse(result));
    }

    @GetMapping
    public ResponseEntity<GetPayrollDeductionsResponse> getDeductions(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) String status
    ) {
        GetPayrollDeductionsQuery query = payrollDeductionPresentationMapper.toListQuery(employeeId, status);
        GetPayrollDeductionsResult result = getPayrollDeductionsUseCase.execute(query);
        return ResponseEntity.ok(payrollDeductionPresentationMapper.toResponse(result));
    }

    @PatchMapping("/{deductionId}/cancel")
    public ResponseEntity<CancelPayrollDeductionResponse> cancelDeduction(
            @PathVariable UUID employeeId,
            @PathVariable UUID deductionId
    ) {
        CancelPayrollDeductionResult result = cancelPayrollDeductionUseCase.execute(
                payrollDeductionPresentationMapper.toCancelCommand(employeeId, deductionId)
        );
        return ResponseEntity.ok(payrollDeductionPresentationMapper.toResponse(result));
    }
}
