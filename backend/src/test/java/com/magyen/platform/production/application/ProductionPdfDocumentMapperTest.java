package com.magyen.platform.production.application;

import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.ProductionItemResult;
import com.magyen.platform.production.application.dto.ProductionMaterialCostSummary;
import com.magyen.platform.production.application.dto.ProductionOrderPdfDocument;
import com.magyen.platform.production.application.dto.ProductionProductSpecificationResult;
import com.magyen.platform.production.application.dto.ProductionSizeBreakdownResult;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductionPdfDocumentMapperTest {

    @Test
    void mapsExistingProductionFieldsAndLeavesReferenceImageAbsent() {
        UUID productionOrderId = UUID.randomUUID();
        GetProductionOrderResult result = new GetProductionOrderResult(
                productionOrderId,
                UUID.randomUUID(),
                "14",
                "Uniformes institucionales",
                UUID.randomUUID(),
                "Colegio San José",
                LocalDate.of(2026, 8, 20),
                ProductionStatus.CREATED,
                ProductionPriority.NORMAL,
                null,
                null,
                null,
                null,
                "Observación de planta",
                List.of(new ProductionItemResult(
                        UUID.randomUUID(),
                        "Camiseta institucional",
                        20,
                        new ProductionProductSpecificationResult(
                                "Camiseta",
                                "Redondo",
                                "Manga corta sisa",
                                false,
                                true,
                                false,
                                false,
                                null,
                                false,
                                false,
                                false,
                                null,
                                "Cuello reforzado"
                        ),
                        List.of(new ProductionSizeBreakdownResult("S", 10), new ProductionSizeBreakdownResult("M", 10))
                )),
                List.of(),
                new ProductionMaterialCostSummary(null, 0, 0, 0),
                null,
                null
        );

        ProductionOrderPdfDocument document = ProductionPdfDocumentMapper.toDocument(result);

        assertEquals("14", document.orderNumber());
        assertEquals("Colegio San José", document.customerName());
        assertEquals("Creada", document.statusLabel());
        assertEquals("Normal", document.priorityLabel());
        assertEquals("Camiseta institucional", document.lines().getFirst().productName());
        assertEquals("S: 10  ·  M: 10", document.lines().getFirst().sizes());
        assertEquals("Sublimación", document.lines().getFirst().extraSpecifications());
        assertNull(document.referenceImage());
    }
}
