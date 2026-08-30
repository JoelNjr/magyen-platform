package com.magyen.platform.finance.presentation.transaction.controller;

import com.magyen.platform.finance.application.dto.GetFinancialTransactionQuery;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionResult;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionsQuery;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionsResult;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionResult;
import com.magyen.platform.finance.application.usecase.GetFinancialTransactionUseCase;
import com.magyen.platform.finance.application.usecase.GetFinancialTransactionsUseCase;
import com.magyen.platform.finance.application.usecase.RegisterFinancialTransactionUseCase;
import com.magyen.platform.finance.presentation.transaction.mapper.FinancialTransactionPresentationMapper;
import com.magyen.platform.finance.presentation.transaction.request.RegisterFinancialTransactionRequest;
import com.magyen.platform.finance.presentation.transaction.response.FinancialTransactionResponse;
import com.magyen.platform.finance.presentation.transaction.response.GetFinancialTransactionsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Expone la API REST del ledger financiero.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/finance/transactions")
public class FinancialTransactionController {

    private final RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase;
    private final GetFinancialTransactionUseCase getFinancialTransactionUseCase;
    private final GetFinancialTransactionsUseCase getFinancialTransactionsUseCase;
    private final FinancialTransactionPresentationMapper financialTransactionPresentationMapper;

    public FinancialTransactionController(
            RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase,
            GetFinancialTransactionUseCase getFinancialTransactionUseCase,
            GetFinancialTransactionsUseCase getFinancialTransactionsUseCase,
            FinancialTransactionPresentationMapper financialTransactionPresentationMapper
    ) {
        this.registerFinancialTransactionUseCase = registerFinancialTransactionUseCase;
        this.getFinancialTransactionUseCase = getFinancialTransactionUseCase;
        this.getFinancialTransactionsUseCase = getFinancialTransactionsUseCase;
        this.financialTransactionPresentationMapper = financialTransactionPresentationMapper;
    }

    @PostMapping
    public ResponseEntity<FinancialTransactionResponse> registerFinancialTransaction(
            @RequestBody RegisterFinancialTransactionRequest request
    ) {
        RegisterFinancialTransactionCommand command = financialTransactionPresentationMapper.toCommand(request);
        RegisterFinancialTransactionResult result = registerFinancialTransactionUseCase.execute(command);
        FinancialTransactionResponse response = financialTransactionPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<GetFinancialTransactionsResponse> getFinancialTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetFinancialTransactionsResult result = getFinancialTransactionsUseCase.execute(
                new GetFinancialTransactionsQuery(fromDate, toDate)
        );
        GetFinancialTransactionsResponse response = financialTransactionPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<FinancialTransactionResponse> getFinancialTransaction(
            @PathVariable UUID transactionId
    ) {
        GetFinancialTransactionQuery query =
                financialTransactionPresentationMapper.toGetFinancialTransactionQuery(transactionId);
        GetFinancialTransactionResult result = getFinancialTransactionUseCase.execute(query);
        FinancialTransactionResponse response = financialTransactionPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
