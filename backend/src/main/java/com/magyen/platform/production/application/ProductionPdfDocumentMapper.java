package com.magyen.platform.production.application;

import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.ProductionDocumentOperationLine;
import com.magyen.platform.production.application.dto.ProductionDocumentProductLine;
import com.magyen.platform.production.application.dto.ProductionItemResult;
import com.magyen.platform.production.application.dto.ProductionOperationResult;
import com.magyen.platform.production.application.dto.ProductionOrderPdfDocument;
import com.magyen.platform.production.application.dto.ProductionProductSpecificationResult;
import com.magyen.platform.production.application.dto.ProductionSizeBreakdownResult;
import com.magyen.platform.production.domain.ProductionOperationStatus;
import com.magyen.platform.production.domain.ProductionOperationType;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Ensambla el modelo de PDF de producción a partir de la lectura existente.
 * <p>
 * No inventa campos ausentes. No incluye costos ni identidades técnicas.
 * La imagen de referencia queda nula mientras no exista almacenamiento.
 */
public final class ProductionPdfDocumentMapper {

    private ProductionPdfDocumentMapper() {
    }

    public static ProductionOrderPdfDocument toDocument(GetProductionOrderResult productionOrder) {
        Objects.requireNonNull(productionOrder, "Production order result must not be null");
        List<ProductionDocumentProductLine> lines = productionOrder.items() == null
                ? List.of()
                : productionOrder.items().stream().map(ProductionPdfDocumentMapper::toLine).toList();
        List<ProductionDocumentOperationLine> operations = productionOrder.operations() == null
                ? List.of()
                : productionOrder.operations().stream().map(ProductionPdfDocumentMapper::toOperationLine).toList();
        return new ProductionOrderPdfDocument(
                blankToNull(productionOrder.orderNumber()),
                blankToNull(productionOrder.orderDescription()),
                blankToNull(productionOrder.customerName()),
                productionOrder.creationDate(),
                statusLabel(productionOrder.status()),
                priorityLabel(productionOrder.priority()),
                productionOrder.plannedStartDate(),
                productionOrder.plannedEndDate(),
                productionOrder.actualStartDate(),
                productionOrder.actualCompletionDate(),
                blankToNull(productionOrder.observations()),
                List.copyOf(lines),
                List.copyOf(operations),
                null
        );
    }

    private static ProductionDocumentProductLine toLine(ProductionItemResult item) {
        return new ProductionDocumentProductLine(
                blankToNull(item.productName()),
                item.quantity(),
                formatSizes(item.sizes()),
                specificationValue(item.productSpecification(), ProductionProductSpecificationResult::garmentType),
                specificationValue(item.productSpecification(), ProductionProductSpecificationResult::collarType),
                specificationValue(item.productSpecification(), ProductionProductSpecificationResult::sleeveType),
                cuffLabel(item.productSpecification()),
                extraSpecifications(item.productSpecification()),
                specificationValue(item.productSpecification(), ProductionProductSpecificationResult::itemObservations)
        );
    }

    private static ProductionDocumentOperationLine toOperationLine(ProductionOperationResult operation) {
        return new ProductionDocumentOperationLine(
                operationTypeLabel(operation.type()),
                operationStatusLabel(operation.status()),
                blankToNull(operation.assignedOperator()),
                operation.plannedStartDate(),
                operation.plannedEndDate(),
                blankToNull(operation.observations())
        );
    }

    private static String formatSizes(List<ProductionSizeBreakdownResult> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return null;
        }
        return sizes.stream()
                .map(size -> size.size() + ": " + size.quantity())
                .collect(Collectors.joining("  ·  "));
    }

    private static String cuffLabel(ProductionProductSpecificationResult specification) {
        if (specification == null || specification.cuffRequired() == null) {
            return null;
        }
        return specification.cuffRequired() ? "Sí" : "No";
    }

    private static String extraSpecifications(ProductionProductSpecificationResult specification) {
        if (specification == null) {
            return null;
        }
        List<String> labels = new ArrayList<>();
        if (specification.sublimationRequired()) {
            labels.add("Sublimación");
        }
        if (specification.embroideryRequired()) {
            labels.add("Bordado");
        }
        if (specification.dtfRequired()) {
            labels.add("DTF");
        }
        if (specification.includesNames()) {
            labels.add("Nombres");
        }
        if (specification.includesNumbers()) {
            labels.add("Números");
        }
        if (specification.includesLogos()) {
            labels.add("Logos");
        }
        String decoration = blankToNull(specification.decorationNotes());
        String personalization = blankToNull(specification.personalizationNotes());
        if (decoration != null) {
            labels.add("Decoración: " + decoration);
        }
        if (personalization != null) {
            labels.add("Personalización: " + personalization);
        }
        if (labels.isEmpty()) {
            return null;
        }
        return String.join("  ·  ", labels);
    }

    private static String specificationValue(
            ProductionProductSpecificationResult specification,
            java.util.function.Function<ProductionProductSpecificationResult, String> extractor
    ) {
        if (specification == null) {
            return null;
        }
        return blankToNull(extractor.apply(specification));
    }

    private static String statusLabel(ProductionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case CREATED -> "Creada";
            case PLANNED -> "Planificada";
            case IN_PROGRESS -> "En progreso";
            case COMPLETED -> "Completada";
        };
    }

    private static String priorityLabel(ProductionPriority priority) {
        if (priority == null) {
            return null;
        }
        return switch (priority) {
            case LOW -> "Baja";
            case NORMAL -> "Normal";
            case HIGH -> "Alta";
            case URGENT -> "Urgente";
        };
    }

    private static String operationTypeLabel(ProductionOperationType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case CUTTING -> "Corte";
            case CALENDERING -> "Calandrado";
            case SUBLIMATION -> "Sublimación";
            case SEWING -> "Confección";
            case QUALITY_CONTROL -> "Control de calidad";
        };
    }

    private static String operationStatusLabel(ProductionOperationStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING -> "Pendiente";
            case IN_PROGRESS -> "En progreso";
            case COMPLETED -> "Completada";
        };
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
