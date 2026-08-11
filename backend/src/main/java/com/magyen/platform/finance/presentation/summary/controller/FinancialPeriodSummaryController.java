package com.magyen.platform.finance.presentation.summary.controller;

import com.magyen.platform.finance.application.dto.GetFinancialPeriodSummaryQuery;
import com.magyen.platform.finance.application.dto.GetFinancialPeriodSummaryResult;
import com.magyen.platform.finance.application.usecase.GetFinancialPeriodSummaryUseCase;
import com.magyen.platform.finance.presentation.summary.mapper.FinancialPeriodSummaryPresentationMapper;
import com.magyen.platform.finance.presentation.summary.response.GetFinancialPeriodSummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Expone el resumen del ledger financiero por período.
 * <p>
 * Solo refleja {@code FinancialTransaction}. No mezcla compromisos PENDING.
 */
@RestController
@RequestMapping("/api/v1/finance/summary")
public class FinancialPeriodSummaryController {

    private final GetFinancialPeriodSummaryUseCase getFinancialPeriodSummaryUseCase;
    private final FinancialPeriodSummaryPresentationMapper presentationMapper;

    public FinancialPeriodSummaryController(
            GetFinancialPeriodSummaryUseCase getFinancialPeriodSummaryUseCase,
            FinancialPeriodSummaryPresentationMapper presentationMapper
    ) {
        this.getFinancialPeriodSummaryUseCase = getFinancialPeriodSummaryUseCase;
        this.presentationMapper = presentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetFinancialPeriodSummaryResponse> getPeriodSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetFinancialPeriodSummaryQuery query = presentationMapper.toQuery(fromDate, toDate);
        GetFinancialPeriodSummaryResult result = getFinancialPeriodSummaryUseCase.execute(query);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }
}
