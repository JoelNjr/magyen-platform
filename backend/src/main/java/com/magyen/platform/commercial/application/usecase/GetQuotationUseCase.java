package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.SellerNameResolver;
import com.magyen.platform.commercial.application.dto.GetQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetQuotationResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationResult;
import com.magyen.platform.commercial.application.dto.QuotationItemResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationItem;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que consulta una cotización completa por identificador.
 */
public class GetQuotationUseCase {

    private final QuotationRepository quotationRepository;
    private final OrderRepository orderRepository;
    private final SellerNameResolver sellerNameResolver;

    public GetQuotationUseCase(
            QuotationRepository quotationRepository,
            OrderRepository orderRepository,
            SellerNameResolver sellerNameResolver
    ) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.sellerNameResolver = Objects.requireNonNull(sellerNameResolver, "Seller name resolver must not be null");
    }

    public GetQuotationResult execute(GetQuotationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        UUID orderId = orderRepository.findByQuotationId(quotation.getId())
                .map(Order::getId)
                .orElse(null);

        return toResult(quotation, orderId);
    }

    private void validateCommand(GetQuotationCommand command) {
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");
    }

    private GetQuotationResult toResult(Quotation quotation, UUID orderId) {
        List<QuotationItemResult> items = quotation.getItems().stream()
                .map(this::toItemResult)
                .toList();

        return new GetQuotationResult(
                quotation.getId(),
                toQuotationNumberValue(quotation.getQuotationNumber()),
                quotation.getCustomerId(),
                quotation.getCreationDate(),
                quotation.getDeliveryDate(),
                quotation.getStatus(),
                quotation.getSellerId(),
                sellerNameResolver.resolveName(quotation.getSellerId()),
                quotation.getObservations(),
                items,
                quotation.getTotal().getAmount(),
                orderId
        );
    }

    private Long toQuotationNumberValue(QuotationNumber quotationNumber) {
        if (quotationNumber == null) {
            return null;
        }
        return quotationNumber.getValue();
    }

    private QuotationItemResult toItemResult(QuotationItem item) {
        return new QuotationItemResult(
                item.getId(),
                item.getProductName(),
                item.getQuantity(),
                item.getFabric(),
                item.getSecondaryFabric(),
                item.getColor(),
                item.getUnitPrice().getAmount(),
                item.getSubtotal().getAmount(),
                toProductSpecificationResult(item.getProductSpecification())
        );
    }

    private ProductSpecificationResult toProductSpecificationResult(ProductSpecification specification) {
        ProductSpecification resolved = specification == null ? ProductSpecification.empty() : specification;

        return new ProductSpecificationResult(
                resolved.getGarmentType(),
                resolved.getCollarType(),
                resolved.getSleeveType(),
                resolved.getCuffRequired(),
                resolved.isSublimationRequired(),
                resolved.isEmbroideryRequired(),
                resolved.isDtfRequired(),
                resolved.getDecorationNotes(),
                resolved.isIncludesNames(),
                resolved.isIncludesNumbers(),
                resolved.isIncludesLogos(),
                resolved.getPersonalizationNotes(),
                resolved.getItemObservations()
        );
    }
}
