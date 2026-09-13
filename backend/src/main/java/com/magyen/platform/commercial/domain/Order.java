package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.shared.domain.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate Root del compromiso comercial aceptado por el cliente.
 * <p>
 * Mantiene la consistencia de la Orden y sus productos comprometidos.
 */
public class Order {

    private final UUID id;
    private final OrderNumber orderNumber;
    private final UUID customerId;
    private final UUID quotationId;
    private final LocalDate confirmationDate;
    private OrderStatus status;
    private final DeliveryCommitment deliveryCommitment;
    private PaymentSummary paymentSummary;
    private final UUID sellerId;
    private final String observations;
    private final String description;
    private final List<OrderItem> items;
    private Money discount;
    private Money total;

    private Order(
            UUID id,
            OrderNumber orderNumber,
            UUID customerId,
            UUID quotationId,
            LocalDate confirmationDate,
            OrderStatus status,
            DeliveryCommitment deliveryCommitment,
            PaymentSummary paymentSummary,
            UUID sellerId,
            String observations,
            String description,
            List<OrderItem> items,
            Money discount
    ) {
        this.id = Objects.requireNonNull(id, "Order id must not be null");
        this.orderNumber = Objects.requireNonNull(orderNumber, "Order number must not be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer id must not be null");
        this.quotationId = Objects.requireNonNull(quotationId, "Quotation id must not be null");
        this.confirmationDate = Objects.requireNonNull(confirmationDate, "Confirmation date must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.deliveryCommitment = Objects.requireNonNull(
                deliveryCommitment,
                "Delivery commitment must not be null"
        );
        this.sellerId = Objects.requireNonNull(sellerId, "Seller id must not be null");
        this.observations = observations;
        this.description = blankToNull(description);
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Items must not be null"));
        this.discount = discount == null ? Money.zero() : discount;
        this.total = calculateTotal(this.items, this.discount);
        this.paymentSummary = Objects.requireNonNull(paymentSummary, "Payment summary must not be null");

        ensureHasAtLeastOneProduct();
        ensureUniqueQuotationItemReferences();
        ensurePaymentSummaryMatchesTotal();
    }

    /**
     * Crea una Orden en estado inicial válido {@link OrderStatus#CONFIRMED}.
     * <p>
     * El anticipo queda reconocido y el contenido comercial se considera comprometido.
     */
    public static Order create(
            OrderNumber orderNumber,
            UUID customerId,
            UUID quotationId,
            LocalDate confirmationDate,
            DeliveryCommitment deliveryCommitment,
            UUID sellerId,
            String observations,
            List<OrderItem> items
    ) {
        return create(
                orderNumber,
                customerId,
                quotationId,
                confirmationDate,
                deliveryCommitment,
                sellerId,
                observations,
                null,
                items
        );
    }

    public static Order create(
            OrderNumber orderNumber,
            UUID customerId,
            UUID quotationId,
            LocalDate confirmationDate,
            DeliveryCommitment deliveryCommitment,
            UUID sellerId,
            String observations,
            String description,
            List<OrderItem> items
    ) {
        return create(
                orderNumber,
                customerId,
                quotationId,
                confirmationDate,
                deliveryCommitment,
                sellerId,
                observations,
                description,
                items,
                Money.zero()
        );
    }

    public static Order create(
            OrderNumber orderNumber,
            UUID customerId,
            UUID quotationId,
            LocalDate confirmationDate,
            DeliveryCommitment deliveryCommitment,
            UUID sellerId,
            String observations,
            String description,
            List<OrderItem> items,
            Money discount
    ) {
        Objects.requireNonNull(items, "Items must not be null");
        validateDeliveryCommitment(confirmationDate, deliveryCommitment);
        validateItems(items);

        List<OrderItem> committedItems = new ArrayList<>(items);
        Money resolvedDiscount = discount == null ? Money.zero() : discount;
        Money total = calculateTotal(committedItems, resolvedDiscount);
        PaymentSummary paymentSummary = PaymentSummary.forConfirmedOrder(total);

        return new Order(
                UUID.randomUUID(),
                orderNumber,
                customerId,
                quotationId,
                confirmationDate,
                OrderStatus.CONFIRMED,
                deliveryCommitment,
                paymentSummary,
                sellerId,
                observations,
                description,
                committedItems,
                resolvedDiscount
        );
    }

    /**
     * Reconstruye una Orden desde persistencia. No aplica lógica de creación de negocio.
     */
    public static Order reconstitute(
            UUID id,
            OrderNumber orderNumber,
            UUID customerId,
            UUID quotationId,
            LocalDate confirmationDate,
            OrderStatus status,
            DeliveryCommitment deliveryCommitment,
            PaymentSummary paymentSummary,
            UUID sellerId,
            String observations,
            List<OrderItem> items
    ) {
        validateDeliveryCommitment(confirmationDate, deliveryCommitment);

        return reconstitute(
                id,
                orderNumber,
                customerId,
                quotationId,
                confirmationDate,
                status,
                deliveryCommitment,
                paymentSummary,
                sellerId,
                observations,
                null,
                items
        );
    }

    public static Order reconstitute(
            UUID id,
            OrderNumber orderNumber,
            UUID customerId,
            UUID quotationId,
            LocalDate confirmationDate,
            OrderStatus status,
            DeliveryCommitment deliveryCommitment,
            PaymentSummary paymentSummary,
            UUID sellerId,
            String observations,
            String description,
            List<OrderItem> items
    ) {
        return reconstitute(
                id,
                orderNumber,
                customerId,
                quotationId,
                confirmationDate,
                status,
                deliveryCommitment,
                paymentSummary,
                sellerId,
                observations,
                description,
                items,
                Money.zero()
        );
    }

    public static Order reconstitute(
            UUID id,
            OrderNumber orderNumber,
            UUID customerId,
            UUID quotationId,
            LocalDate confirmationDate,
            OrderStatus status,
            DeliveryCommitment deliveryCommitment,
            PaymentSummary paymentSummary,
            UUID sellerId,
            String observations,
            String description,
            List<OrderItem> items,
            Money discount
    ) {
        validateDeliveryCommitment(confirmationDate, deliveryCommitment);

        return new Order(
                id,
                orderNumber,
                customerId,
                quotationId,
                confirmationDate,
                status,
                deliveryCommitment,
                paymentSummary,
                sellerId,
                observations,
                description,
                items,
                discount
        );
    }

    /**
     * Inicia la producción de la Orden.
     * <p>
     * Transición válida: {@link OrderStatus#CONFIRMED} → {@link OrderStatus#IN_PRODUCTION}.
     */
    public void startProduction() {
        transitionTo(OrderStatus.CONFIRMED, OrderStatus.IN_PRODUCTION);
    }

    /**
     * Marca la Orden como lista para entrega.
     * <p>
     * Transición válida: {@link OrderStatus#IN_PRODUCTION} → {@link OrderStatus#READY_FOR_DELIVERY}.
     */
    public void markReadyForDelivery() {
        transitionTo(OrderStatus.IN_PRODUCTION, OrderStatus.READY_FOR_DELIVERY);
    }

    /**
     * Marca la Orden como entregada al cliente.
     * <p>
     * Transición válida: {@link OrderStatus#READY_FOR_DELIVERY} → {@link OrderStatus#DELIVERED}.
     */
    public void deliver() {
        transitionTo(OrderStatus.READY_FOR_DELIVERY, OrderStatus.DELIVERED);
    }

    /**
     * Reconoce el pago final del compromiso.
     * <p>
     * No registra movimientos financieros; solo actualiza el estado de pago de la Orden.
     * Es prerrequisito de negocio para {@link #close()}.
     */
    public void acknowledgeFinalPayment() {
        if (status == OrderStatus.CLOSED) {
            throw new OrderDomainException("Final payment cannot be acknowledged on a closed order");
        }
        this.paymentSummary = paymentSummary.acknowledgeFinalPayment();
    }

    /**
     * Cierra la Orden tras la entrega y el reconocimiento del pago final.
     * <p>
     * Transición válida: {@link OrderStatus#DELIVERED} → {@link OrderStatus#CLOSED}.
     */
    public void close() {
        if (status == OrderStatus.CLOSED) {
            return;
        }

        if (status != OrderStatus.DELIVERED) {
            throw new OrderDomainException(
                    "An order can only be closed from DELIVERED status. Current status: " + status
            );
        }

        if (!paymentSummary.isFinalPaymentAcknowledged()) {
            throw new OrderDomainException(
                    "An order cannot be closed without final payment acknowledgment"
            );
        }

        this.status = OrderStatus.CLOSED;
    }

    /**
     * Agrega un producto comprometido a la Orden.
     * <p>
     * El ítem manual no tiene {@code quotationItemId}. No adivina origen en cotización.
     */
    public void addItem(String productName, int quantity, String fabric, String color, Money unitPrice) {
        addItem(productName, quantity, fabric, null, color, unitPrice, ProductSpecification.empty());
    }

    public void addItem(
            String productName,
            int quantity,
            String fabric,
            String secondaryFabric,
            String color,
            Money unitPrice,
            ProductSpecification productSpecification
    ) {
        ensureCommercialContentEditable();
        validateQuantity(quantity);
        validateUnitPrice(unitPrice);

        items.add(OrderItem.create(
                productName,
                quantity,
                fabric,
                secondaryFabric,
                color,
                unitPrice,
                productSpecification,
                List.of()
        ));
        recalculateCommercialState();
    }

    /**
     * Actualiza cantidad y precio unitario de un ítem. No cambia {@code quotationItemId}.
     */
    public void updateItemCommercialCommitment(UUID itemId, int quantity, Money unitPrice) {
        ensureCommercialContentEditable();
        requireItem(itemId).updateCommercialCommitment(quantity, unitPrice);
        recalculateCommercialState();
    }

    /**
     * Reemplaza la especificación comercial de un ítem. No cambia total ni trazabilidad.
     */
    public void assignItemProductSpecification(UUID itemId, ProductSpecification productSpecification) {
        ensureCommercialContentEditable();
        requireItem(itemId).assignProductSpecification(productSpecification);
    }

    /**
     * Reemplaza las tallas de un ítem. No cambia cantidad comercial ni trazabilidad.
     */
    public void replaceItemSizes(UUID itemId, List<SizeBreakdown> sizeBreakdowns) {
        ensureCommercialContentEditable();
        requireItem(itemId).replaceSizeBreakdowns(sizeBreakdowns);
    }

    /**
     * Aplica un descuento sobre el subtotal. No altera precios unitarios.
     */
    public void applyDiscount(Money discount) {
        ensureCommercialContentEditable();
        Money resolvedDiscount = discount == null ? Money.zero() : discount;
        Money newTotal = calculateTotal(this.items, resolvedDiscount);
        this.discount = resolvedDiscount;
        this.total = newTotal;
        synchronizePaymentSummary();
    }

    /**
     * Elimina un producto comprometido de la Orden.
     * <p>
     * La Orden nunca puede quedarse sin productos.
     * No muta el QuotationItem de origen si existía trazabilidad.
     */
    public void removeItem(UUID itemId) {
        ensureCommercialContentEditable();
        Objects.requireNonNull(itemId, "Item id must not be null");

        boolean exists = items.stream().anyMatch(item -> item.getId().equals(itemId));
        if (!exists) {
            throw new OrderDomainException("Order item not found: " + itemId);
        }
        if (items.size() == 1) {
            throw new OrderDomainException("An order must have at least one product");
        }

        items.removeIf(item -> item.getId().equals(itemId));
        recalculateCommercialState();
    }

    /**
     * Rechaza un total comercial inferior al monto ya pagado. No modifica pagos.
     */
    public void ensureTotalCoversAmountPaid(Money amountAlreadyPaid) {
        Objects.requireNonNull(amountAlreadyPaid, "Amount already paid must not be null");
        if (amountAlreadyPaid.isGreaterThan(total)) {
            throw new OrderDomainException(
                    "The new order total cannot be lower than the amount already paid. New total: "
                            + total.getAmount()
                            + ", already paid: "
                            + amountAlreadyPaid.getAmount()
            );
        }
    }

    /**
     * Indica si al menos un ítem tiene trazabilidad a un QuotationItem.
     */
    public boolean hasTracedItems() {
        return items.stream().anyMatch(item -> item.getQuotationItemId() != null);
    }

    /**
     * Aplica el contenido comercial actual de una cotización sobre ítems trazados.
     * <p>
     * Solo usa {@code QuotationItem.id ↔ OrderItem.quotationItemId}.
     * No toca ítems manuales ({@code quotationItemId == null}).
     * No modifica tallas. No adivina origen. Si alguna validación falla, no persiste
     * (el llamador no debe guardar). Las validaciones que pueden rechazar se ejecutan
     * antes de mutar el agregado.
     */
    public void applyQuotationCommercialSource(List<QuotationItem> quotationItems, Money discount) {
        ensureCommercialContentEditable();
        Objects.requireNonNull(quotationItems, "Quotation items must not be null");
        if (!hasTracedItems()) {
            throw new OrderDomainException(
                    "Quotation changes cannot be applied to an order without item traceability"
            );
        }

        Set<UUID> quotationItemIds = new HashSet<>();
        for (QuotationItem quotationItem : quotationItems) {
            Objects.requireNonNull(quotationItem, "Quotation item must not be null");
            if (!quotationItemIds.add(quotationItem.getId())) {
                throw new OrderDomainException(
                        "Duplicate quotation item in synchronization source: " + quotationItem.getId()
                );
            }
        }

        List<OrderItem> orphans = new ArrayList<>();
        List<OrderItem> manuals = new ArrayList<>();
        for (OrderItem item : items) {
            UUID tracedId = item.getQuotationItemId();
            if (tracedId == null) {
                manuals.add(item);
            } else if (!quotationItemIds.contains(tracedId)) {
                orphans.add(item);
            }
        }

        List<QuotationItem> additions = new ArrayList<>();
        List<QuotationItem> matchedSources = new ArrayList<>();
        for (QuotationItem quotationItem : quotationItems) {
            OrderItem matched = findItemByQuotationItemId(quotationItem.getId());
            if (matched == null) {
                additions.add(quotationItem);
            } else {
                matchedSources.add(quotationItem);
            }
        }

        int remainingCount = items.size() - orphans.size() + additions.size();
        if (remainingCount < 1) {
            throw new OrderDomainException("An order must have at least one product");
        }

        for (QuotationItem matchedSource : matchedSources) {
            OrderItem matchedItem = findItemByQuotationItemId(matchedSource.getId());
            if (matchedItem.getAssignedSizeQuantity() > matchedSource.getQuantity()) {
                throw new OrderDomainException(
                        "Total size quantity must not exceed order item quantity. "
                                + "Assigned: " + matchedItem.getAssignedSizeQuantity()
                                + ", item quantity: " + matchedSource.getQuantity()
                );
            }
        }

        Money proposedSubtotal = Money.zero();
        for (OrderItem manual : manuals) {
            proposedSubtotal = proposedSubtotal.add(manual.getSubtotal());
        }
        for (QuotationItem matchedSource : matchedSources) {
            proposedSubtotal = proposedSubtotal.add(
                    matchedSource.getUnitPrice().multiply(matchedSource.getQuantity())
            );
        }
        for (QuotationItem addition : additions) {
            proposedSubtotal = proposedSubtotal.add(
                    addition.getUnitPrice().multiply(addition.getQuantity())
            );
        }
        Money resolvedDiscount = discount == null ? Money.zero() : discount;
        if (resolvedDiscount.isGreaterThan(proposedSubtotal)) {
            throw new OrderDomainException("Discount must not exceed order subtotal");
        }

        for (QuotationItem matchedSource : matchedSources) {
            findItemByQuotationItemId(matchedSource.getId()).applyQuotationCommercialSource(matchedSource);
        }
        for (QuotationItem addition : additions) {
            items.add(OrderItem.createFromQuotation(addition));
        }
        for (OrderItem orphan : orphans) {
            items.removeIf(item -> item.getId().equals(orphan.getId()));
        }

        this.discount = resolvedDiscount;
        recalculateCommercialState();
        ensureUniqueQuotationItemReferences();
        ensureHasAtLeastOneProduct();
    }

    public UUID getId() {
        return id;
    }

    public OrderNumber getOrderNumber() {
        return orderNumber;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getQuotationId() {
        return quotationId;
    }

    public LocalDate getConfirmationDate() {
        return confirmationDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public DeliveryCommitment getDeliveryCommitment() {
        return deliveryCommitment;
    }

    public PaymentSummary getPaymentSummary() {
        return paymentSummary;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getObservations() {
        return observations;
    }

    public String getDescription() {
        return description;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Money getSubtotal() {
        return calculateSubtotal(items);
    }

    public Money getDiscount() {
        return discount;
    }

    public Money getTotal() {
        return total;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Order order = (Order) other;
        return id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void transitionTo(OrderStatus expectedCurrentStatus, OrderStatus nextStatus) {
        if (status == nextStatus) {
            return;
        }

        if (status != expectedCurrentStatus) {
            throw new OrderDomainException(
                    "Invalid order status transition from " + status + " to " + nextStatus
            );
        }

        this.status = nextStatus;
    }

    private void ensureCommercialContentEditable() {
        if (!status.allowsCommercialContentEditing()) {
            throw new OrderDomainException(
                    "Commercial content can only be modified while order status is CONFIRMED, "
                            + "IN_PRODUCTION or READY_FOR_DELIVERY. Current status: "
                            + status
            );
        }
    }

    private OrderItem requireItem(UUID itemId) {
        Objects.requireNonNull(itemId, "Item id must not be null");
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new OrderDomainException("Order item not found: " + itemId));
    }

    private OrderItem findItemByQuotationItemId(UUID quotationItemId) {
        if (quotationItemId == null) {
            return null;
        }
        return items.stream()
                .filter(item -> quotationItemId.equals(item.getQuotationItemId()))
                .findFirst()
                .orElse(null);
    }

    private void recalculateCommercialState() {
        recalculateTotal();
        synchronizePaymentSummary();
    }

    private void recalculateTotal() {
        this.total = calculateTotal(this.items, this.discount);
    }

    private void synchronizePaymentSummary() {
        this.paymentSummary = PaymentSummary.of(
                paymentSummary.isAdvanceAcknowledged(),
                paymentSummary.isFinalPaymentAcknowledged(),
                this.total
        );
    }

    private void ensureHasAtLeastOneProduct() {
        if (items.isEmpty()) {
            throw new OrderDomainException("An order must have at least one product");
        }
    }

    private void ensureUniqueQuotationItemReferences() {
        Set<UUID> referencedQuotationItemIds = new HashSet<>();
        for (OrderItem item : items) {
            UUID quotationItemId = item.getQuotationItemId();
            if (quotationItemId != null && !referencedQuotationItemIds.add(quotationItemId)) {
                throw new OrderDomainException(
                        "An order cannot reference the same quotation item more than once: "
                                + quotationItemId
                );
            }
        }
    }

    private void ensurePaymentSummaryMatchesTotal() {
        if (!paymentSummary.getCommittedTotal().equals(total)) {
            throw new OrderDomainException("Payment summary committed total must match order total");
        }
    }

    private static Money calculateSubtotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(), Money::add);
    }

    private static Money calculateTotal(List<OrderItem> items, Money discount) {
        Money subtotal = calculateSubtotal(items);
        Money resolvedDiscount = discount == null ? Money.zero() : discount;
        if (resolvedDiscount.isGreaterThan(subtotal)) {
            throw new OrderDomainException("Discount must not exceed order subtotal");
        }
        return subtotal.subtract(resolvedDiscount);
    }

    private static void validateItems(List<OrderItem> items) {
        if (items.isEmpty()) {
            throw new OrderDomainException("An order must have at least one product");
        }

        for (OrderItem item : items) {
            Objects.requireNonNull(item, "Order item must not be null");
            validateQuantity(item.getQuantity());
            validateUnitPrice(item.getUnitPrice());
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new OrderDomainException("Quantity must be greater than zero");
        }
    }

    private static void validateUnitPrice(Money unitPrice) {
        Objects.requireNonNull(unitPrice, "Unit price must not be null");
        if (unitPrice.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderDomainException("Unit price must be greater than zero");
        }
    }

    private static void validateDeliveryCommitment(
            LocalDate confirmationDate,
            DeliveryCommitment deliveryCommitment
    ) {
        Objects.requireNonNull(confirmationDate, "Confirmation date must not be null");
        Objects.requireNonNull(deliveryCommitment, "Delivery commitment must not be null");

        if (deliveryCommitment.getPromisedDeliveryDate().isBefore(confirmationDate)) {
            throw new OrderDomainException("Promised delivery date must not be before confirmation date");
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

}
