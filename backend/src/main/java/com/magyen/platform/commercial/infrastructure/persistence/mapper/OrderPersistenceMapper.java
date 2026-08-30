package com.magyen.platform.commercial.infrastructure.persistence.mapper;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.SizeBreakdown;
import com.magyen.platform.commercial.infrastructure.persistence.entity.OrderEntity;
import com.magyen.platform.commercial.infrastructure.persistence.entity.OrderItemEntity;
import com.magyen.platform.commercial.infrastructure.persistence.entity.OrderItemSizeEntity;
import com.magyen.platform.shared.domain.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link Order} y su modelo JPA.
 * <p>
 * No contiene reglas de negocio ni accede a la base de datos.
 */
public class OrderPersistenceMapper {

    public OrderEntity toEntity(Order order) {
        Objects.requireNonNull(order, "Order must not be null");

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(order.getId());
        orderEntity.setOrderNumber(order.getOrderNumber().getValue());
        orderEntity.setCustomerId(order.getCustomerId());
        orderEntity.setQuotationId(order.getQuotationId());
        orderEntity.setConfirmationDate(order.getConfirmationDate());
        orderEntity.setStatus(order.getStatus());
        orderEntity.setSellerId(order.getSellerId());
        orderEntity.setObservations(order.getObservations());
        orderEntity.setDescription(order.getDescription());
        orderEntity.setDiscountAmount(toAmount(order.getDiscount()));
        orderEntity.setTotalAmount(toAmount(order.getTotal()));

        mapDeliveryCommitment(orderEntity, order.getDeliveryCommitment());
        mapPaymentSummary(orderEntity, order.getPaymentSummary());

        List<OrderItemEntity> itemEntities = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemEntity itemEntity = toItemEntity(item);
            itemEntity.setOrder(orderEntity);
            itemEntities.add(itemEntity);
        }
        orderEntity.setItems(itemEntities);

        return orderEntity;
    }

    public Order toDomain(OrderEntity orderEntity) {
        Objects.requireNonNull(orderEntity, "Order entity must not be null");

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemEntity itemEntity : orderEntity.getItems()) {
            items.add(toItemDomain(itemEntity));
        }

        return Order.reconstitute(
                orderEntity.getId(),
                OrderNumber.of(orderEntity.getOrderNumber()),
                orderEntity.getCustomerId(),
                orderEntity.getQuotationId(),
                orderEntity.getConfirmationDate(),
                orderEntity.getStatus(),
                toDeliveryCommitment(orderEntity),
                toPaymentSummary(orderEntity),
                orderEntity.getSellerId(),
                orderEntity.getObservations(),
                orderEntity.getDescription(),
                items,
                toMoney(orderEntity.getDiscountAmount() == null
                        ? BigDecimal.ZERO
                        : orderEntity.getDiscountAmount())
        );
    }

    private OrderItemEntity toItemEntity(OrderItem item) {
        Objects.requireNonNull(item, "Order item must not be null");

        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setId(item.getId());
        itemEntity.setProductName(item.getProductName());
        itemEntity.setQuantity(item.getQuantity());
        itemEntity.setFabric(item.getFabric());
        itemEntity.setSecondaryFabric(item.getSecondaryFabric());
        itemEntity.setColor(item.getColor());
        itemEntity.setUnitPrice(toAmount(item.getUnitPrice()));
        itemEntity.setSubtotal(toAmount(item.getSubtotal()));
        mapProductSpecification(itemEntity, item.getProductSpecification());

        List<OrderItemSizeEntity> sizeEntities = new ArrayList<>();
        for (SizeBreakdown sizeBreakdown : item.getSizeBreakdowns()) {
            OrderItemSizeEntity sizeEntity = toSizeEntity(sizeBreakdown);
            sizeEntity.setOrderItem(itemEntity);
            sizeEntities.add(sizeEntity);
        }
        itemEntity.setSizeBreakdowns(sizeEntities);

        return itemEntity;
    }

    private OrderItem toItemDomain(OrderItemEntity itemEntity) {
        Objects.requireNonNull(itemEntity, "Order item entity must not be null");

        List<SizeBreakdown> sizeBreakdowns = new ArrayList<>();
        for (OrderItemSizeEntity sizeEntity : itemEntity.getSizeBreakdowns()) {
            sizeBreakdowns.add(toSizeDomain(sizeEntity));
        }

        return OrderItem.reconstitute(
                itemEntity.getId(),
                itemEntity.getProductName(),
                itemEntity.getQuantity(),
                itemEntity.getFabric(),
                itemEntity.getSecondaryFabric(),
                itemEntity.getColor(),
                toMoney(itemEntity.getUnitPrice()),
                toProductSpecification(itemEntity),
                sizeBreakdowns
        );
    }

    private OrderItemSizeEntity toSizeEntity(SizeBreakdown sizeBreakdown) {
        Objects.requireNonNull(sizeBreakdown, "Size breakdown must not be null");

        OrderItemSizeEntity sizeEntity = new OrderItemSizeEntity();
        sizeEntity.setId(sizeBreakdown.getId());
        sizeEntity.setSize(sizeBreakdown.getSize());
        sizeEntity.setQuantity(sizeBreakdown.getQuantity());
        return sizeEntity;
    }

    private SizeBreakdown toSizeDomain(OrderItemSizeEntity sizeEntity) {
        Objects.requireNonNull(sizeEntity, "Size entity must not be null");

        return SizeBreakdown.reconstitute(
                sizeEntity.getId(),
                sizeEntity.getSize(),
                sizeEntity.getQuantity()
        );
    }

    private void mapProductSpecification(OrderItemEntity itemEntity, ProductSpecification specification) {
        Objects.requireNonNull(itemEntity, "Order item entity must not be null");
        ProductSpecification resolved = specification == null ? ProductSpecification.empty() : specification;

        itemEntity.setGarmentType(resolved.getGarmentType());
        itemEntity.setCollarType(resolved.getCollarType());
        itemEntity.setSleeveType(resolved.getSleeveType());
        itemEntity.setCuffRequired(resolved.getCuffRequired());
        itemEntity.setSublimationRequired(resolved.isSublimationRequired());
        itemEntity.setEmbroideryRequired(resolved.isEmbroideryRequired());
        itemEntity.setDtfRequired(resolved.isDtfRequired());
        itemEntity.setDecorationNotes(resolved.getDecorationNotes());
        itemEntity.setIncludesNames(resolved.isIncludesNames());
        itemEntity.setIncludesNumbers(resolved.isIncludesNumbers());
        itemEntity.setIncludesLogos(resolved.isIncludesLogos());
        itemEntity.setPersonalizationNotes(resolved.getPersonalizationNotes());
        itemEntity.setItemObservations(resolved.getItemObservations());
    }

    private ProductSpecification toProductSpecification(OrderItemEntity itemEntity) {
        Objects.requireNonNull(itemEntity, "Order item entity must not be null");

        return ProductSpecification.reconstitute(
                itemEntity.getGarmentType(),
                itemEntity.getCollarType(),
                itemEntity.getSleeveType(),
                itemEntity.getCuffRequired(),
                itemEntity.isSublimationRequired(),
                itemEntity.isEmbroideryRequired(),
                itemEntity.isDtfRequired(),
                itemEntity.getDecorationNotes(),
                itemEntity.isIncludesNames(),
                itemEntity.isIncludesNumbers(),
                itemEntity.isIncludesLogos(),
                itemEntity.getPersonalizationNotes(),
                itemEntity.getItemObservations()
        );
    }

    private void mapPaymentSummary(OrderEntity orderEntity, PaymentSummary paymentSummary) {
        Objects.requireNonNull(orderEntity, "Order entity must not be null");
        Objects.requireNonNull(paymentSummary, "Payment summary must not be null");

        orderEntity.setAdvanceAcknowledged(paymentSummary.isAdvanceAcknowledged());
        orderEntity.setFinalPaymentAcknowledged(paymentSummary.isFinalPaymentAcknowledged());
        orderEntity.setCommittedTotal(toAmount(paymentSummary.getCommittedTotal()));
        orderEntity.setRemainingBalance(toAmount(paymentSummary.getRemainingBalance()));
    }

    private PaymentSummary toPaymentSummary(OrderEntity orderEntity) {
        Objects.requireNonNull(orderEntity, "Order entity must not be null");

        return PaymentSummary.of(
                orderEntity.isAdvanceAcknowledged(),
                orderEntity.isFinalPaymentAcknowledged(),
                toMoney(orderEntity.getCommittedTotal())
        );
    }

    private void mapDeliveryCommitment(OrderEntity orderEntity, DeliveryCommitment deliveryCommitment) {
        Objects.requireNonNull(orderEntity, "Order entity must not be null");
        Objects.requireNonNull(deliveryCommitment, "Delivery commitment must not be null");

        orderEntity.setPromisedDeliveryDate(deliveryCommitment.getPromisedDeliveryDate());
        orderEntity.setDeliveryObservations(deliveryCommitment.getDeliveryObservations());
    }

    private DeliveryCommitment toDeliveryCommitment(OrderEntity orderEntity) {
        Objects.requireNonNull(orderEntity, "Order entity must not be null");

        return DeliveryCommitment.of(
                orderEntity.getPromisedDeliveryDate(),
                orderEntity.getDeliveryObservations()
        );
    }

    private BigDecimal toAmount(Money money) {
        Objects.requireNonNull(money, "Money must not be null");
        return money.getAmount();
    }

    private Money toMoney(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        return Money.of(amount);
    }
}
