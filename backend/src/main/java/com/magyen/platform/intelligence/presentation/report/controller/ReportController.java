package com.magyen.platform.intelligence.presentation.report.controller;

import com.magyen.platform.intelligence.application.dto.GetInventoryReportResult;
import com.magyen.platform.intelligence.application.dto.GetPaymentsReportResult;
import com.magyen.platform.intelligence.application.dto.GetProductionReportResult;
import com.magyen.platform.intelligence.application.dto.GetSalesReportResult;
import com.magyen.platform.intelligence.application.usecase.GetInventoryReportUseCase;
import com.magyen.platform.intelligence.application.usecase.GetPaymentsReportUseCase;
import com.magyen.platform.intelligence.application.usecase.GetProductionReportUseCase;
import com.magyen.platform.intelligence.application.usecase.GetSalesReportUseCase;
import com.magyen.platform.intelligence.presentation.report.mapper.IntelligencePresentationMapper;
import com.magyen.platform.intelligence.presentation.report.response.GetInventoryReportResponse;
import com.magyen.platform.intelligence.presentation.report.response.GetPaymentsReportResponse;
import com.magyen.platform.intelligence.presentation.report.response.GetProductionReportResponse;
import com.magyen.platform.intelligence.presentation.report.response.GetSalesReportResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la API REST de reportes consolidados.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final GetSalesReportUseCase getSalesReportUseCase;
    private final GetProductionReportUseCase getProductionReportUseCase;
    private final GetInventoryReportUseCase getInventoryReportUseCase;
    private final GetPaymentsReportUseCase getPaymentsReportUseCase;
    private final IntelligencePresentationMapper intelligencePresentationMapper;

    public ReportController(
            GetSalesReportUseCase getSalesReportUseCase,
            GetProductionReportUseCase getProductionReportUseCase,
            GetInventoryReportUseCase getInventoryReportUseCase,
            GetPaymentsReportUseCase getPaymentsReportUseCase,
            IntelligencePresentationMapper intelligencePresentationMapper
    ) {
        this.getSalesReportUseCase = getSalesReportUseCase;
        this.getProductionReportUseCase = getProductionReportUseCase;
        this.getInventoryReportUseCase = getInventoryReportUseCase;
        this.getPaymentsReportUseCase = getPaymentsReportUseCase;
        this.intelligencePresentationMapper = intelligencePresentationMapper;
    }

    @GetMapping("/sales")
    public ResponseEntity<GetSalesReportResponse> getSalesReport() {
        GetSalesReportResult result = getSalesReportUseCase.execute();
        GetSalesReportResponse response = intelligencePresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/production")
    public ResponseEntity<GetProductionReportResponse> getProductionReport() {
        GetProductionReportResult result = getProductionReportUseCase.execute();
        GetProductionReportResponse response = intelligencePresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inventory")
    public ResponseEntity<GetInventoryReportResponse> getInventoryReport() {
        GetInventoryReportResult result = getInventoryReportUseCase.execute();
        GetInventoryReportResponse response = intelligencePresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments")
    public ResponseEntity<GetPaymentsReportResponse> getPaymentsReport() {
        GetPaymentsReportResult result = getPaymentsReportUseCase.execute();
        GetPaymentsReportResponse response = intelligencePresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
