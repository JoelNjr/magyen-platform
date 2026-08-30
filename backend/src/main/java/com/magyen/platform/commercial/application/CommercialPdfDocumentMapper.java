package com.magyen.platform.commercial.application;

import com.magyen.platform.commercial.application.dto.CommercialDocumentProductLine;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.dto.GetQuotationResult;
import com.magyen.platform.commercial.application.dto.OrderItemResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationResult;
import com.magyen.platform.commercial.application.dto.QuotationItemResult;
import com.magyen.platform.commercial.application.dto.QuotationPdfDocument;
import com.magyen.platform.commercial.application.dto.RemissionPdfDocument;
import com.magyen.platform.commercial.application.dto.SizeBreakdownResult;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort.OrderPaymentCollection;
import com.magyen.platform.commercial.domain.QuotationNumberFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Ensambla modelos de PDF a partir de lecturas comerciales existentes.
 * <p>
 * No recalcula totales. No inventa campos ausentes.
 */
public final class CommercialPdfDocumentMapper {

    private CommercialPdfDocumentMapper() {
    }

    public static QuotationPdfDocument toQuotationDocument(GetQuotationResult quotation, String customerName) {
        Objects.requireNonNull(quotation, "Quotation result must not be null");
        List<CommercialDocumentProductLine> lines = quotation.items() == null
                ? List.of()
                : quotation.items().stream().map(CommercialPdfDocumentMapper::toLine).toList();
        return new QuotationPdfDocument(
                QuotationNumberFormat.display(quotation.quotationNumber()),
                quotation.creationDate(),
                quotation.deliveryDate(),
                blankToNull(customerName),
                blankToNull(quotation.sellerName()),
                blankToNull(quotation.observations()),
                List.copyOf(lines),
                quotation.subtotalAmount(),
                quotation.discountAmount(),
                quotation.totalAmount()
        );
    }

    public static RemissionPdfDocument toRemissionDocument(
            GetOrderResult order,
            OrderPaymentCollection collection
    ) {
        Objects.requireNonNull(order, "Order result must not be null");
        Objects.requireNonNull(collection, "Payment collection must not be null");
        List<CommercialDocumentProductLine> lines = order.items() == null
                ? List.of()
                : order.items().stream().map(CommercialPdfDocumentMapper::toLine).toList();
        return new RemissionPdfDocument(
                blankToNull(order.orderNumber()),
                blankToNull(order.description()),
                order.confirmationDate(),
                order.deliveryCommitment() == null ? null : order.deliveryCommitment().promisedDeliveryDate(),
                blankToNull(order.customerName()),
                blankToNull(order.sellerName()),
                blankToNull(order.observations()),
                List.copyOf(lines),
                order.totalAmount(),
                collection.collectedAmount(),
                collection.outstandingAmount()
        );
    }

    private static CommercialDocumentProductLine toLine(QuotationItemResult item) {
        return new CommercialDocumentProductLine(
                blankToNull(item.productName()),
                specificationValue(item.productSpecification(), ProductSpecificationResult::garmentType),
                specificationValue(item.productSpecification(), ProductSpecificationResult::itemObservations),
                item.quantity(),
                null,
                blankToNull(item.fabric()),
                blankToNull(item.secondaryFabric()),
                blankToNull(item.color()),
                specificationValue(item.productSpecification(), ProductSpecificationResult::collarType),
                specificationValue(item.productSpecification(), ProductSpecificationResult::sleeveType),
                cuffLabel(item.productSpecification()),
                extraSpecifications(item.productSpecification()),
                item.unitPrice(),
                item.subtotal()
        );
    }

    private static CommercialDocumentProductLine toLine(OrderItemResult item) {
        return new CommercialDocumentProductLine(
                blankToNull(item.productName()),
                specificationValue(item.productSpecification(), ProductSpecificationResult::garmentType),
                specificationValue(item.productSpecification(), ProductSpecificationResult::itemObservations),
                item.quantity(),
                formatSizes(item.sizes()),
                blankToNull(item.fabric()),
                blankToNull(item.secondaryFabric()),
                blankToNull(item.color()),
                specificationValue(item.productSpecification(), ProductSpecificationResult::collarType),
                specificationValue(item.productSpecification(), ProductSpecificationResult::sleeveType),
                cuffLabel(item.productSpecification()),
                extraSpecifications(item.productSpecification()),
                item.unitPrice(),
                item.subtotal()
        );
    }

    private static String formatSizes(List<SizeBreakdownResult> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return null;
        }
        return sizes.stream()
                .map(size -> size.size() + ": " + size.quantity())
                .collect(Collectors.joining("  ·  "));
    }

    private static String cuffLabel(ProductSpecificationResult specification) {
        if (specification == null || specification.cuffRequired() == null) {
            return null;
        }
        return specification.cuffRequired() ? "Sí" : "No";
    }

    private static String extraSpecifications(ProductSpecificationResult specification) {
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
            ProductSpecificationResult specification,
            java.util.function.Function<ProductSpecificationResult, String> extractor
    ) {
        if (specification == null) {
            return null;
        }
        return blankToNull(extractor.apply(specification));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
