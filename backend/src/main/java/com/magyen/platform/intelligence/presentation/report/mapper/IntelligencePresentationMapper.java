package com.magyen.platform.intelligence.presentation.report.mapper;

import com.magyen.platform.intelligence.application.dto.GetInventoryReportResult;
import com.magyen.platform.intelligence.application.dto.GetPaymentsReportResult;
import com.magyen.platform.intelligence.application.dto.GetProductionReportResult;
import com.magyen.platform.intelligence.application.dto.GetSalesReportResult;
import com.magyen.platform.intelligence.presentation.report.response.GetInventoryReportResponse;
import com.magyen.platform.intelligence.presentation.report.response.GetInventoryReportResponse.LowStockItemResponse;
import com.magyen.platform.intelligence.presentation.report.response.GetPaymentsReportResponse;
import com.magyen.platform.intelligence.presentation.report.response.GetProductionReportResponse;
import com.magyen.platform.intelligence.presentation.report.response.GetSalesReportResponse;
import com.magyen.platform.production.domain.ProductionStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class IntelligencePresentationMapper {

    public GetSalesReportResponse toResponse(GetSalesReportResult result) {
        Objects.requireNonNull(result, "GetSalesReportResult must not be null");

        return new GetSalesReportResponse(
                result.totalSold(),
                result.orderCount(),
                result.averagePerSale()
        );
    }

    public GetProductionReportResponse toResponse(GetProductionReportResult result) {
        Objects.requireNonNull(result, "GetProductionReportResult must not be null");

        Map<String, Long> countByStatus = new LinkedHashMap<>();
        for (Map.Entry<ProductionStatus, Long> entry : result.countByStatus().entrySet()) {
            countByStatus.put(entry.getKey().name(), entry.getValue());
        }

        return new GetProductionReportResponse(Map.copyOf(countByStatus));
    }

    public GetInventoryReportResponse toResponse(GetInventoryReportResult result) {
        Objects.requireNonNull(result, "GetInventoryReportResult must not be null");

        List<LowStockItemResponse> items = result.items().stream()
                .map(item -> new LowStockItemResponse(
                        item.inventoryItemId(),
                        item.materialCode(),
                        item.name(),
                        item.category(),
                        item.unitOfMeasure(),
                        item.stock(),
                        item.minimumStock()
                ))
                .toList();

        return new GetInventoryReportResponse(items);
    }

    public GetPaymentsReportResponse toResponse(GetPaymentsReportResult result) {
        Objects.requireNonNull(result, "GetPaymentsReportResult must not be null");

        return new GetPaymentsReportResponse(
                result.totalReceived(),
                result.paymentCount(),
                result.averagePerPayment()
        );
    }
}
