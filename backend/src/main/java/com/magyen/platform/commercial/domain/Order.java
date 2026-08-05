package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.shared.domain.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    private final String salesperson;
    private final String observations;
    private final List<OrderItem> items;
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
            String salesperson,
            String observations,
            List<OrderItem> items
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
        this.salesperson = requireNonBlank(salesperson, "Salesperson must not be blank");
        this.observations = observations;
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Items must not be null"));
        this.total = calculateTotal(this.items);
        this.paymentSummary = Objects.requireNonNull(paymentSummary, "Payment summary must not be null");

        ensureHasAtLeastOneProduct();
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
            String salesperson,
            String observations,
            List<OrderItem> items
    ) {
        Objects.requireNonNull(items, "Items must not be null");
        validateDeliveryCommitment(confirmationDate, deliveryCommitment);
        validateItems(items);

        List<OrderItem> committedItems = new ArrayList<>(items);
        Money total = calculateTotal(committedItems);
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
                salesperson,
                observations,
                committedItems
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
            String salesperson,
            String observations,
            List<OrderItem> items
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
                salesperson,
                observations,
                items
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
     * Solo permitido mientras el estado sea {@link OrderStatus#CONFIRMED}.
     */
    public void addItem(String productName, int quantity, String fabric, String color, Money unitPrice) {
        ensureCommercialContentEditable();
        validateQuantity(quantity);
        validateUnitPrice(unitPrice);

        OrderItem item = OrderItem.create(productName, quantity, fabric, color, unitPrice);
        items.add(item);
        recalculateCommercialState();
    }

    /**
     * Elimina un producto comprometido de la Orden.
     * <p>
     * Solo permitido mientras el estado sea {@link OrderStatus#CONFIRMED}.
     * La Orden nunca puede quedarse sin productos.
     */
    public void removeItem(UUID itemId) {
        ensureCommercialContentEditable();
        Objects.requireNonNull(itemId, "Item id must not be null");

        boolean removed = items.removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new OrderDomainException("Order item not found: " + itemId);
        }

        ensureHasAtLeastOneProduct();
        recalculateCommercialState();
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

    public String getSalesperson() {
        return salesperson;
    }

    public String getObservations() {
        return observations;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
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
        if (status != OrderStatus.CONFIRMED) {
            throw new OrderDomainException(
                    "Commercial content can only be modified while order status is CONFIRMED. Current status: "
                            + status
            );
        }
    }

    private void recalculateCommercialState() {
        recalculateTotal();
        synchronizePaymentSummary();
    }

    private void recalculateTotal() {
        this.total = calculateTotal(this.items);
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

    private void ensurePaymentSummaryMatchesTotal() {
        if (!paymentSummary.getCommittedTotal().equals(total)) {
            throw new OrderDomainException("Payment summary committed total must match order total");
        }
    }

    private static Money calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(), Money::add);
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

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
