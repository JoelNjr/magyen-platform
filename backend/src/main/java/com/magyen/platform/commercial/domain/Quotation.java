package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.shared.domain.Money;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del módulo comercial.
 * <p>
 * Mantiene la consistencia de la cotización y sus productos asociados.
 */
public class Quotation {

    private final UUID id;
    private final UUID customerId;
    private final LocalDate creationDate;
    private LocalDate deliveryDate;
    private QuotationStatus status;
    private String salesperson;
    private String observations;
    private final List<QuotationItem> items;
    private Money total;

    private Quotation(
            UUID id,
            UUID customerId,
            LocalDate creationDate,
            LocalDate deliveryDate,
            QuotationStatus status,
            String salesperson,
            String observations,
            List<QuotationItem> items
    ) {
        this.id = Objects.requireNonNull(id, "Quotation id must not be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer id must not be null");
        this.creationDate = Objects.requireNonNull(creationDate, "Creation date must not be null");
        this.deliveryDate = Objects.requireNonNull(deliveryDate, "Delivery date must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.salesperson = requireNonBlank(salesperson, "Salesperson must not be blank");
        this.observations = observations;
        this.items = new ArrayList<>(items);
        recalculateTotal();
    }

    public static Quotation create(
            UUID customerId,
            LocalDate creationDate,
            LocalDate deliveryDate,
            String salesperson,
            String observations
    ) {
        validateDeliveryDate(creationDate, deliveryDate);

        return new Quotation(
                UUID.randomUUID(),
                customerId,
                creationDate,
                deliveryDate,
                QuotationStatus.DRAFT,
                salesperson,
                observations,
                List.of()
        );
    }

    public static Quotation reconstitute(
            UUID id,
            UUID customerId,
            LocalDate creationDate,
            LocalDate deliveryDate,
            QuotationStatus status,
            String salesperson,
            String observations,
            List<QuotationItem> items
    ) {
        validateDeliveryDate(creationDate, deliveryDate);

        return new Quotation(
                id,
                customerId,
                creationDate,
                deliveryDate,
                status,
                salesperson,
                observations,
                items
        );
    }

    public void addItem(String productName, int quantity, String fabric, String color, Money unitPrice) {
        validateQuantity(quantity);
        validateUnitPrice(unitPrice);

        QuotationItem item = QuotationItem.create(productName, quantity, fabric, color, unitPrice);
        items.add(item);
        recalculateTotal();
    }

    public void removeItem(UUID itemId) {
        Objects.requireNonNull(itemId, "Item id must not be null");

        boolean removed = items.removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new QuotationDomainException("Quotation item not found: " + itemId);
        }

        if (status != QuotationStatus.DRAFT) {
            ensureHasAtLeastOneProduct();
        }
        recalculateTotal();
    }

    public void changeStatus(QuotationStatus newStatus) {
        Objects.requireNonNull(newStatus, "Status must not be null");

        if (status == newStatus) {
            return;
        }

        validateStatusTransition(status, newStatus);
        ensureHasAtLeastOneProductWhenLeavingDraft(newStatus);

        this.status = newStatus;
    }

    public void updateDeliveryDate(LocalDate newDeliveryDate) {
        Objects.requireNonNull(newDeliveryDate, "Delivery date must not be null");
        validateDeliveryDate(creationDate, newDeliveryDate);
        this.deliveryDate = newDeliveryDate;
    }

    public void updateSalesperson(String newSalesperson) {
        this.salesperson = requireNonBlank(newSalesperson, "Salesperson must not be blank");
    }

    public void updateObservations(String newObservations) {
        this.observations = newObservations;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public String getSalesperson() {
        return salesperson;
    }

    public String getObservations() {
        return observations;
    }

    public List<QuotationItem> getItems() {
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
        Quotation quotation = (Quotation) other;
        return id.equals(quotation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void recalculateTotal() {
        this.total = items.stream()
                .map(QuotationItem::getSubtotal)
                .reduce(Money.zero(), Money::add);
    }

    private void ensureHasAtLeastOneProduct() {
        if (items.isEmpty()) {
            throw new QuotationDomainException("A quotation must have at least one product");
        }
    }

    private void ensureHasAtLeastOneProductWhenLeavingDraft(QuotationStatus newStatus) {
        if (status == QuotationStatus.DRAFT && newStatus != QuotationStatus.DRAFT && items.isEmpty()) {
            throw new QuotationDomainException("A quotation must have at least one product");
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new QuotationDomainException("Quantity must be greater than zero");
        }
    }

    private static void validateUnitPrice(Money unitPrice) {
        Objects.requireNonNull(unitPrice, "Unit price must not be null");
        if (unitPrice.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new QuotationDomainException("Unit price must be greater than zero");
        }
    }

    private static void validateDeliveryDate(LocalDate creationDate, LocalDate deliveryDate) {
        Objects.requireNonNull(creationDate, "Creation date must not be null");
        Objects.requireNonNull(deliveryDate, "Delivery date must not be null");

        if (deliveryDate.isBefore(creationDate)) {
            throw new QuotationDomainException("Delivery date must not be before creation date");
        }
    }

    private static void validateStatusTransition(QuotationStatus currentStatus, QuotationStatus newStatus) {
        if (currentStatus == QuotationStatus.APPROVED && newStatus == QuotationStatus.DRAFT) {
            throw new QuotationDomainException("An approved quotation cannot return to draft");
        }

        if (currentStatus == QuotationStatus.REJECTED && newStatus == QuotationStatus.APPROVED) {
            throw new QuotationDomainException("A rejected quotation cannot be approved again");
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
