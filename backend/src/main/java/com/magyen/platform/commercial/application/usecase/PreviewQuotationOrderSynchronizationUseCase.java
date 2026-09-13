package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.PreviewQuotationOrderSynchronizationQuery;
import com.magyen.platform.commercial.application.dto.PreviewQuotationOrderSynchronizationResult;
import com.magyen.platform.commercial.application.dto.PreviewQuotationOrderSynchronizationResult.ManualItemPreserved;
import com.magyen.platform.commercial.application.dto.PreviewQuotationOrderSynchronizationResult.MatchedItemChange;
import com.magyen.platform.commercial.application.dto.PreviewQuotationOrderSynchronizationResult.OrphanRemoval;
import com.magyen.platform.commercial.application.dto.PreviewQuotationOrderSynchronizationResult.QuotationOnlyAddition;
import com.magyen.platform.commercial.application.dto.SizeBreakdownResult;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationItem;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.SizeBreakdown;
import com.magyen.platform.shared.domain.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Vista previa de aplicar la cotización persistida a su Orden. Solo lectura.
 */
public class PreviewQuotationOrderSynchronizationUseCase {

    public static final String UNAVAILABLE_NO_ASSOCIATED_ORDER = "NO_ASSOCIATED_ORDER";
    public static final String UNAVAILABLE_ORDER_FROZEN = "ORDER_FROZEN";
    public static final String UNAVAILABLE_LEGACY_UNTRACED = "LEGACY_UNTRACED";

    private final QuotationRepository quotationRepository;
    private final OrderRepository orderRepository;
    private final OrderPaymentCollectionPort orderPaymentCollectionPort;

    public PreviewQuotationOrderSynchronizationUseCase(
            QuotationRepository quotationRepository,
            OrderRepository orderRepository,
            OrderPaymentCollectionPort orderPaymentCollectionPort
    ) {
        this.quotationRepository = Objects.requireNonNull(
                quotationRepository,
                "Quotation repository must not be null"
        );
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.orderPaymentCollectionPort = Objects.requireNonNull(
                orderPaymentCollectionPort,
                "Order payment collection port must not be null"
        );
    }

    public PreviewQuotationOrderSynchronizationResult execute(PreviewQuotationOrderSynchronizationQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.quotationId(), "Quotation id must not be null");

        Quotation quotation = quotationRepository.findById(query.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + query.quotationId()
                ));

        Optional<Order> associatedOrder = orderRepository.findByQuotationId(quotation.getId());
        if (associatedOrder.isEmpty()) {
            return emptyWithoutOrder();
        }

        Order order = associatedOrder.get();
        boolean legacyUntraced = !order.hasTracedItems();
        boolean frozen = !order.getStatus().allowsCommercialContentEditing();
        String unavailableReason = null;
        if (frozen) {
            unavailableReason = UNAVAILABLE_ORDER_FROZEN;
        } else if (legacyUntraced) {
            unavailableReason = UNAVAILABLE_LEGACY_UNTRACED;
        }

        Set<UUID> quotationItemIds = quotation.getItems().stream()
                .map(QuotationItem::getId)
                .collect(Collectors.toSet());

        List<MatchedItemChange> matchedChanges = new ArrayList<>();
        List<QuotationOnlyAddition> additions = new ArrayList<>();
        List<OrphanRemoval> orphanRemovals = new ArrayList<>();
        List<ManualItemPreserved> manuals = new ArrayList<>();
        boolean sizeConstraintViolation = false;
        Money proposedSubtotal = Money.zero();

        for (OrderItem item : order.getItems()) {
            if (item.getQuotationItemId() == null) {
                manuals.add(new ManualItemPreserved(
                        item.getId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice().getAmount(),
                        item.getSubtotal().getAmount()
                ));
                proposedSubtotal = proposedSubtotal.add(item.getSubtotal());
            } else if (!quotationItemIds.contains(item.getQuotationItemId())) {
                orphanRemovals.add(new OrphanRemoval(
                        item.getId(),
                        item.getQuotationItemId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice().getAmount(),
                        item.getSubtotal().getAmount(),
                        toSizes(item)
                ));
            }
        }

        for (QuotationItem quotationItem : quotation.getItems()) {
            OrderItem matched = findByQuotationItemId(order, quotationItem.getId());
            if (matched == null) {
                additions.add(new QuotationOnlyAddition(
                        quotationItem.getId(),
                        quotationItem.getProductName(),
                        quotationItem.getQuantity(),
                        quotationItem.getUnitPrice().getAmount(),
                        quotationItem.getSubtotal().getAmount()
                ));
                proposedSubtotal = proposedSubtotal.add(quotationItem.getSubtotal());
            } else {
                if (matched.getAssignedSizeQuantity() > quotationItem.getQuantity()) {
                    sizeConstraintViolation = true;
                }
                matchedChanges.add(new MatchedItemChange(
                        matched.getId(),
                        quotationItem.getId(),
                        quotationItem.getProductName(),
                        matched.getQuantity(),
                        quotationItem.getQuantity(),
                        matched.getUnitPrice().getAmount(),
                        quotationItem.getUnitPrice().getAmount(),
                        matched.getSubtotal().getAmount(),
                        quotationItem.getSubtotal().getAmount(),
                        toSizes(matched)
                ));
                proposedSubtotal = proposedSubtotal.add(quotationItem.getSubtotal());
            }
        }

        Money proposedDiscount = quotation.getDiscount();
        BigDecimal proposedTotalAmount;
        if (proposedDiscount.isGreaterThan(proposedSubtotal)) {
            proposedTotalAmount = null;
        } else {
            proposedTotalAmount = proposedSubtotal.subtract(proposedDiscount).getAmount();
        }

        BigDecimal collectedAmount = moneyOrZero(
                orderPaymentCollectionPort.getCollection(order.getId()).collectedAmount()
        );
        boolean paymentFloorViolation = proposedTotalAmount != null
                && collectedAmount.compareTo(proposedTotalAmount) > 0;
        BigDecimal proposedOutstanding = proposedTotalAmount == null
                ? null
                : proposedTotalAmount.subtract(collectedAmount);

        boolean applyAllowed = unavailableReason == null;

        return new PreviewQuotationOrderSynchronizationResult(
                true,
                order.getId(),
                order.getOrderNumber().getValue(),
                order.getStatus().name(),
                applyAllowed,
                unavailableReason,
                legacyUntraced,
                paymentFloorViolation,
                sizeConstraintViolation,
                List.copyOf(matchedChanges),
                List.copyOf(additions),
                List.copyOf(orphanRemovals),
                List.copyOf(manuals),
                order.getSubtotal().getAmount(),
                proposedSubtotal.getAmount(),
                order.getDiscount().getAmount(),
                proposedDiscount.getAmount(),
                order.getTotal().getAmount(),
                proposedTotalAmount,
                collectedAmount,
                proposedOutstanding
        );
    }

    private static PreviewQuotationOrderSynchronizationResult emptyWithoutOrder() {
        return new PreviewQuotationOrderSynchronizationResult(
                false,
                null,
                null,
                null,
                false,
                UNAVAILABLE_NO_ASSOCIATED_ORDER,
                false,
                false,
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static OrderItem findByQuotationItemId(Order order, UUID quotationItemId) {
        return order.getItems().stream()
                .filter(item -> quotationItemId.equals(item.getQuotationItemId()))
                .findFirst()
                .orElse(null);
    }

    private static List<SizeBreakdownResult> toSizes(OrderItem item) {
        return item.getSizeBreakdowns().stream()
                .map(PreviewQuotationOrderSynchronizationUseCase::toSize)
                .toList();
    }

    private static SizeBreakdownResult toSize(SizeBreakdown sizeBreakdown) {
        return new SizeBreakdownResult(sizeBreakdown.getSize(), sizeBreakdown.getQuantity());
    }

    private static BigDecimal moneyOrZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
