package com.magyen.platform.finance.presentation.payroll.controller;

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
import com.magyen.platform.finance.application.usecase.CancelPayrollPeriodUseCase;
import com.magyen.platform.finance.application.usecase.GeneratePayrollPeriodsUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollPeriodUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollPeriodsUseCase;
import com.magyen.platform.finance.application.usecase.PayPayrollPeriodUseCase;
import com.magyen.platform.finance.presentation.payroll.mapper.PayrollPeriodPresentationMapper;
import com.magyen.platform.finance.presentation.payroll.request.GeneratePayrollPeriodsRequest;
import com.magyen.platform.finance.presentation.payroll.request.PayPayrollPeriodRequest;
import com.magyen.platform.finance.presentation.payroll.response.CancelPayrollPeriodResponse;
import com.magyen.platform.finance.presentation.payroll.response.GeneratePayrollPeriodsResponse;
import com.magyen.platform.finance.presentation.payroll.response.GetPayrollPeriodsResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayPayrollPeriodResponse;
import com.magyen.platform.finance.presentation.payroll.response.PayrollPeriodResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de períodos de nómina.
 * <p>
 * Generar un período no crea movimientos del ledger.
 * Solo el pago explícito genera un {@code FinancialTransaction}.
 */
@RestController
@RequestMapping("/api/v1/finance/payroll/periods")
public class PayrollPeriodController {

    private final GeneratePayrollPeriodsUseCase generatePayrollPeriodsUseCase;
    private final GetPayrollPeriodsUseCase getPayrollPeriodsUseCase;
    private final GetPayrollPeriodUseCase getPayrollPeriodUseCase;
    private final PayPayrollPeriodUseCase payPayrollPeriodUseCase;
    private final CancelPayrollPeriodUseCase cancelPayrollPeriodUseCase;
    private final PayrollPeriodPresentationMapper presentationMapper;

    public PayrollPeriodController(
            GeneratePayrollPeriodsUseCase generatePayrollPeriodsUseCase,
            GetPayrollPeriodsUseCase getPayrollPeriodsUseCase,
            GetPayrollPeriodUseCase getPayrollPeriodUseCase,
            PayPayrollPeriodUseCase payPayrollPeriodUseCase,
            CancelPayrollPeriodUseCase cancelPayrollPeriodUseCase,
            PayrollPeriodPresentationMapper presentationMapper
    ) {
        this.generatePayrollPeriodsUseCase = generatePayrollPeriodsUseCase;
        this.getPayrollPeriodsUseCase = getPayrollPeriodsUseCase;
        this.getPayrollPeriodUseCase = getPayrollPeriodUseCase;
        this.payPayrollPeriodUseCase = payPayrollPeriodUseCase;
        this.cancelPayrollPeriodUseCase = cancelPayrollPeriodUseCase;
        this.presentationMapper = presentationMapper;
    }

    /**
     * Generación controlada e idempotente. No crea movimientos del ledger.
     */
    @PostMapping("/generate")
    public ResponseEntity<GeneratePayrollPeriodsResponse> generatePeriods(
            @RequestBody GeneratePayrollPeriodsRequest request
    ) {
        GeneratePayrollPeriodsCommand command = presentationMapper.toGenerateCommand(request);
        GeneratePayrollPeriodsResult result = generatePayrollPeriodsUseCase.execute(command);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @GetMapping
    public ResponseEntity<GetPayrollPeriodsResponse> getPeriods() {
        GetPayrollPeriodsQuery query = presentationMapper.toListQuery();
        GetPayrollPeriodsResult result = getPayrollPeriodsUseCase.execute(query);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @GetMapping("/{periodId}")
    public ResponseEntity<PayrollPeriodResponse> getPeriod(@PathVariable UUID periodId) {
        GetPayrollPeriodQuery query = presentationMapper.toGetQuery(periodId);
        GetPayrollPeriodResult result = getPayrollPeriodUseCase.execute(query);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @PatchMapping("/{periodId}/pay")
    public ResponseEntity<PayPayrollPeriodResponse> payPeriod(
            @PathVariable UUID periodId,
            @RequestBody(required = false) PayPayrollPeriodRequest request
    ) {
        PayPayrollPeriodCommand command = presentationMapper.toPayCommand(periodId, request);
        PayPayrollPeriodResult result = payPayrollPeriodUseCase.execute(command);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @PatchMapping("/{periodId}/cancel")
    public ResponseEntity<CancelPayrollPeriodResponse> cancelPeriod(@PathVariable UUID periodId) {
        CancelPayrollPeriodCommand command = presentationMapper.toCancelCommand(periodId);
        CancelPayrollPeriodResult result = cancelPayrollPeriodUseCase.execute(command);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }
}
