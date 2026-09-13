package com.magyen.platform.commercial.presentation.quotation.mapper;

import com.magyen.platform.commercial.application.dto.ApplyQuotationChangesToOrderCommand;
import com.magyen.platform.commercial.application.dto.ApplyQuotationChangesToOrderResult;
import com.magyen.platform.commercial.application.dto.ApplyQuotationDiscountCommand;
import com.magyen.platform.commercial.application.dto.ApplyQuotationDiscountResult;
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
import com.magyen.platform.commercial.application.dto.RemoveQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.RemoveQuotationItemResult;
import com.magyen.platform.commercial.application.dto.PreviewQuotationOrderSynchronizationQuery;
import com.magyen.platform.commercial.application.dto.PreviewQuotationOrderSynchronizationResult;
import com.magyen.platform.commercial.application.dto.SizeBreakdownResult;
import com.magyen.platform.commercial.application.dto.UpdateQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.UpdateQuotationItemResult;
import com.magyen.platform.commercial.presentation.quotation.request.ApplyQuotationDiscountRequest;
import com.magyen.platform.commercial.presentation.quotation.request.AddQuotationItemRequest;
import com.magyen.platform.commercial.presentation.quotation.request.CreateQuotationRequest;
import com.magyen.platform.commercial.presentation.quotation.request.ProductSpecificationRequest;
import com.magyen.platform.commercial.presentation.order.response.SizeBreakdownResponse;
import com.magyen.platform.commercial.presentation.quotation.response.ApplyQuotationChangesToOrderResponse;
import com.magyen.platform.commercial.presentation.quotation.response.ApplyQuotationDiscountResponse;
import com.magyen.platform.commercial.presentation.quotation.response.AddQuotationItemResponse;
import com.magyen.platform.commercial.presentation.quotation.response.ApproveQuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.CreateQuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.GetQuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.PreviewQuotationOrderSynchronizationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.GetQuotationsResponse;
import com.magyen.platform.commercial.presentation.quotation.response.GetQuotationsResponse.QuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.ProductSpecificationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.QuotationItemResponse;
import com.magyen.platform.commercial.presentation.quotation.response.RemoveQuotationItemResponse;
import com.magyen.platform.commercial.presentation.quotation.response.UpdateQuotationItemResponse;

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

    public ApplyQuotationDiscountCommand toApplyDiscountCommand(
            UUID quotationId,
            ApplyQuotationDiscountRequest request
    ) {
        Objects.requireNonNull(quotationId, "Quotation id must not be null");
        Objects.requireNonNull(request, "ApplyQuotationDiscountRequest must not be null");
        return new ApplyQuotationDiscountCommand(quotationId, request.discountAmount());
    }

    public ApplyQuotationDiscountResponse toApplyDiscountResponse(ApplyQuotationDiscountResult result) {
        Objects.requireNonNull(result, "ApplyQuotationDiscountResult must not be null");
        return new ApplyQuotationDiscountResponse(
                result.quotationId(),
                result.subtotalAmount(),
                result.discountAmount(),
                result.totalAmount()
        );
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

    public UpdateQuotationItemCommand toUpdateItemCommand(
            UUID quotationId,
            UUID itemId,
            AddQuotationItemRequest request
    ) {
        Objects.requireNonNull(quotationId, "Quotation id must not be null");
        Objects.requireNonNull(itemId, "Item id must not be null");
        Objects.requireNonNull(request, "AddQuotationItemRequest must not be null");

        return new UpdateQuotationItemCommand(
                quotationId,
                itemId,
                request.productName(),
                request.quantity(),
                request.fabric(),
                request.secondaryFabric(),
                request.color(),
                request.unitPrice(),
                toProductSpecificationCommand(request.productSpecification())
        );
    }

    public UpdateQuotationItemResponse toUpdateItemResponse(UpdateQuotationItemResult result) {
        Objects.requireNonNull(result, "UpdateQuotationItemResult must not be null");

        return new UpdateQuotationItemResponse(
                result.quotationId(),
                result.itemId(),
                result.totalAmount()
        );
    }

    public RemoveQuotationItemCommand toRemoveItemCommand(UUID quotationId, UUID itemId) {
        Objects.requireNonNull(quotationId, "Quotation id must not be null");
        Objects.requireNonNull(itemId, "Item id must not be null");

        return new RemoveQuotationItemCommand(quotationId, itemId);
    }

    public RemoveQuotationItemResponse toRemoveItemResponse(RemoveQuotationItemResult result) {
        Objects.requireNonNull(result, "RemoveQuotationItemResult must not be null");

        return new RemoveQuotationItemResponse(
                result.quotationId(),
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
                result.subtotalAmount(),
                result.discountAmount(),
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

    public PreviewQuotationOrderSynchronizationQuery toPreviewSynchronizationQuery(UUID quotationId) {
        Objects.requireNonNull(quotationId, "Quotation id must not be null");
        return new PreviewQuotationOrderSynchronizationQuery(quotationId);
    }

    public ApplyQuotationChangesToOrderCommand toApplyQuotationChangesToOrderCommand(UUID quotationId) {
        Objects.requireNonNull(quotationId, "Quotation id must not be null");
        return new ApplyQuotationChangesToOrderCommand(quotationId);
    }

    public PreviewQuotationOrderSynchronizationResponse toResponse(
            PreviewQuotationOrderSynchronizationResult result
    ) {
        Objects.requireNonNull(result, "Preview result must not be null");
        return new PreviewQuotationOrderSynchronizationResponse(
                result.orderExists(),
                result.orderId(),
                result.orderNumber(),
                result.orderStatus(),
                result.applyAllowed(),
                result.unavailableReason(),
                result.legacyUntraced(),
                result.paymentFloorViolation(),
                result.sizeConstraintViolation(),
                result.matchedChanges().stream().map(this::toMatchedChange).toList(),
                result.additions().stream().map(this::toAddition).toList(),
                result.orphanRemovals().stream().map(this::toOrphan).toList(),
                result.manualsPreserved().stream().map(this::toManual).toList(),
                result.currentSubtotal(),
                result.proposedSubtotal(),
                result.currentDiscount(),
                result.proposedDiscount(),
                result.currentTotal(),
                result.proposedTotal(),
                result.collectedAmount(),
                result.proposedOutstanding()
        );
    }

    public ApplyQuotationChangesToOrderResponse toResponse(ApplyQuotationChangesToOrderResult result) {
        Objects.requireNonNull(result, "Apply result must not be null");
        return new ApplyQuotationChangesToOrderResponse(
                result.orderId(),
                result.subtotalAmount(),
                result.discountAmount(),
                result.totalAmount()
        );
    }

    private PreviewQuotationOrderSynchronizationResponse.MatchedItemChangeResponse toMatchedChange(
            PreviewQuotationOrderSynchronizationResult.MatchedItemChange change
    ) {
        return new PreviewQuotationOrderSynchronizationResponse.MatchedItemChangeResponse(
                change.orderItemId(),
                change.quotationItemId(),
                change.productName(),
                change.currentQuantity(),
                change.proposedQuantity(),
                change.currentUnitPrice(),
                change.proposedUnitPrice(),
                change.currentSubtotal(),
                change.proposedSubtotal(),
                change.sizes().stream().map(this::toSizeResponse).toList()
        );
    }

    private PreviewQuotationOrderSynchronizationResponse.QuotationOnlyAdditionResponse toAddition(
            PreviewQuotationOrderSynchronizationResult.QuotationOnlyAddition addition
    ) {
        return new PreviewQuotationOrderSynchronizationResponse.QuotationOnlyAdditionResponse(
                addition.quotationItemId(),
                addition.productName(),
                addition.quantity(),
                addition.unitPrice(),
                addition.subtotal()
        );
    }

    private PreviewQuotationOrderSynchronizationResponse.OrphanRemovalResponse toOrphan(
            PreviewQuotationOrderSynchronizationResult.OrphanRemoval removal
    ) {
        return new PreviewQuotationOrderSynchronizationResponse.OrphanRemovalResponse(
                removal.orderItemId(),
                removal.quotationItemId(),
                removal.productName(),
                removal.quantity(),
                removal.unitPrice(),
                removal.subtotal(),
                removal.sizes().stream().map(this::toSizeResponse).toList()
        );
    }

    private PreviewQuotationOrderSynchronizationResponse.ManualItemPreservedResponse toManual(
            PreviewQuotationOrderSynchronizationResult.ManualItemPreserved manual
    ) {
        return new PreviewQuotationOrderSynchronizationResponse.ManualItemPreservedResponse(
                manual.orderItemId(),
                manual.productName(),
                manual.quantity(),
                manual.unitPrice(),
                manual.subtotal()
        );
    }

    private SizeBreakdownResponse toSizeResponse(SizeBreakdownResult size) {
        return new SizeBreakdownResponse(size.size(), size.quantity());
    }
}
