package com.magyen.platform.commercial.presentation.quotation.mapper;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.AddQuotationItemResult;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationResult;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationResult;
import com.magyen.platform.commercial.application.dto.GetQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetQuotationResult;
import com.magyen.platform.commercial.application.dto.GetQuotationsResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.ProductSpecificationResult;
import com.magyen.platform.commercial.presentation.quotation.request.AddQuotationItemRequest;
import com.magyen.platform.commercial.presentation.quotation.request.CreateQuotationRequest;
import com.magyen.platform.commercial.presentation.quotation.request.ProductSpecificationRequest;
import com.magyen.platform.commercial.presentation.quotation.response.AddQuotationItemResponse;
import com.magyen.platform.commercial.presentation.quotation.response.ApproveQuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.CreateQuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.GetQuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.GetQuotationsResponse;
import com.magyen.platform.commercial.presentation.quotation.response.GetQuotationsResponse.QuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.ProductSpecificationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.QuotationItemResponse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class QuotationPresentationMapper {

    public CreateQuotationCommand toCommand(CreateQuotationRequest request) {
        Objects.requireNonNull(request, "CreateQuotationRequest must not be null");

        return new CreateQuotationCommand(
                request.customerId(),
                request.deliveryDate(),
                request.sellerId(),
                request.observations(),
                request.quotationDate()
        );
    }

    public CreateQuotationResponse toResponse(CreateQuotationResult result) {
        Objects.requireNonNull(result, "CreateQuotationResult must not be null");

        return new CreateQuotationResponse(
                result.quotationId(),
                result.quotationNumber(),
                result.status().name(),
                result.creationDate()
        );
    }

    public ApproveQuotationCommand toApproveCommand(UUID quotationId) {
        Objects.requireNonNull(quotationId, "Quotation id must not be null");

        return new ApproveQuotationCommand(quotationId);
    }

    public ApproveQuotationResponse toApproveResponse(ApproveQuotationResult result) {
        Objects.requireNonNull(result, "ApproveQuotationResult must not be null");

        return new ApproveQuotationResponse(
                result.quotationId(),
                result.status().name()
        );
    }

    public AddQuotationItemCommand toAddItemCommand(UUID quotationId, AddQuotationItemRequest request) {
        Objects.requireNonNull(quotationId, "Quotation id must not be null");
        Objects.requireNonNull(request, "AddQuotationItemRequest must not be null");

        return new AddQuotationItemCommand(
                quotationId,
                request.productName(),
                request.quantity(),
                request.fabric(),
                request.secondaryFabric(),
                request.color(),
                request.unitPrice(),
                toProductSpecificationCommand(request.productSpecification())
        );
    }

    public AddQuotationItemResponse toAddItemResponse(AddQuotationItemResult result) {
        Objects.requireNonNull(result, "AddQuotationItemResult must not be null");

        return new AddQuotationItemResponse(
                result.quotationId(),
                result.itemId(),
                result.totalAmount()
        );
    }

    public GetQuotationsResponse toResponse(GetQuotationsResult result) {
        Objects.requireNonNull(result, "GetQuotationsResult must not be null");

        List<QuotationResponse> quotations = result.quotations().stream()
                .map(quotation -> new QuotationResponse(
                        quotation.quotationId(),
                        quotation.quotationNumber(),
                        quotation.customerId(),
                        quotation.creationDate(),
                        quotation.deliveryDate(),
                        quotation.status().name(),
                        quotation.sellerId(),
                        quotation.sellerName(),
                        quotation.observations(),
                        quotation.totalAmount()
                ))
                .toList();

        return new GetQuotationsResponse(quotations);
    }

    public GetQuotationCommand toGetQuotationCommand(UUID quotationId) {
        Objects.requireNonNull(quotationId, "Quotation id must not be null");

        return new GetQuotationCommand(quotationId);
    }

    public GetQuotationResponse toResponse(GetQuotationResult result) {
        Objects.requireNonNull(result, "GetQuotationResult must not be null");

        List<QuotationItemResponse> items = result.items().stream()
                .map(item -> new QuotationItemResponse(
                        item.itemId(),
                        item.productName(),
                        item.quantity(),
                        item.fabric(),
                        item.secondaryFabric(),
                        item.color(),
                        item.unitPrice(),
                        item.subtotal(),
                        toProductSpecificationResponse(item.productSpecification())
                ))
                .toList();

        return new GetQuotationResponse(
                result.quotationId(),
                result.quotationNumber(),
                result.customerId(),
                result.creationDate(),
                result.deliveryDate(),
                result.status().name(),
                result.sellerId(),
                result.sellerName(),
                result.observations(),
                items,
                result.totalAmount(),
                result.orderId()
        );
    }

    private ProductSpecificationCommand toProductSpecificationCommand(ProductSpecificationRequest request) {
        if (request == null) {
            return null;
        }

        return new ProductSpecificationCommand(
                request.garmentType(),
                request.collarType(),
                request.sleeveType(),
                request.cuffRequired(),
                booleanOrFalse(request.sublimationRequired()),
                booleanOrFalse(request.embroideryRequired()),
                booleanOrFalse(request.dtfRequired()),
                request.decorationNotes(),
                booleanOrFalse(request.includesNames()),
                booleanOrFalse(request.includesNumbers()),
                booleanOrFalse(request.includesLogos()),
                request.personalizationNotes(),
                request.itemObservations()
        );
    }

    private ProductSpecificationResponse toProductSpecificationResponse(ProductSpecificationResult result) {
        ProductSpecificationResult resolved = result == null
                ? new ProductSpecificationResult(
                        null, null, null, null,
                        false, false, false, null,
                        false, false, false, null, null
                )
                : result;

        return new ProductSpecificationResponse(
                resolved.garmentType(),
                resolved.collarType(),
                resolved.sleeveType(),
                resolved.cuffRequired(),
                resolved.sublimationRequired(),
                resolved.embroideryRequired(),
                resolved.dtfRequired(),
                resolved.decorationNotes(),
                resolved.includesNames(),
                resolved.includesNumbers(),
                resolved.includesLogos(),
                resolved.personalizationNotes(),
                resolved.itemObservations()
        );
    }

    private boolean booleanOrFalse(Boolean value) {
        return value != null && value;
    }
}
