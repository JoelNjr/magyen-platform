package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.shared.domain.Money;

import java.math.BigDecimal;
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
    private final QuotationNumber quotationNumber;
    private final UUID customerId;
    private final LocalDate creationDate;
    private LocalDate deliveryDate;
    private QuotationStatus status;
    private final UUID sellerId;
    private String observations;
    private final List<QuotationItem> items;
    private Money total;

    private Quotation(
            UUID id,
            QuotationNumber quotationNumber,
            UUID customerId,
            LocalDate creationDate,
            LocalDate deliveryDate,
            QuotationStatus status,
            UUID sellerId,
            String observations,
            List<QuotationItem> items
    ) {
        this.id = Objects.requireNonNull(id, "Quotation id must not be null");
        this.quotationNumber = quotationNumber;
        this.customerId = Objects.requireNonNull(customerId, "Customer id must not be null");
        this.creationDate = Objects.requireNonNull(creationDate, "Creation date must not be null");
        this.deliveryDate = Objects.requireNonNull(deliveryDate, "Delivery date must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.sellerId = Objects.requireNonNull(sellerId, "Seller id must not be null");
        this.observations = observations;
        this.items = new ArrayList<>(items);
        recalculateTotal();
    }

    /**
     * Crea una cotización con identidad técnica nueva y número comercial asignado.
     */
    public static Quotation create(
            QuotationNumber quotationNumber,
            UUID customerId,
            LocalDate creationDate,
            LocalDate deliveryDate,
            UUID sellerId,
            String observations
    ) {
        Objects.requireNonNull(quotationNumber, "Quotation number must not be null");
        validateDeliveryDate(creationDate, deliveryDate);

        return new Quotation(
                UUID.randomUUID(),
                quotationNumber,
                customerId,
                creationDate,
                deliveryDate,
                QuotationStatus.DRAFT,
                sellerId,
                observations,
                List.of()
        );
    }

    /**
     * Reconstruye una cotización desde persistencia. No aplica lógica de creación de negocio.
     * <p>
     * {@code quotationNumber} puede ser {@code null} mientras existan filas históricas
     * sin número comercial asignado (transición previa al backfill).
     */
    public static Quotation reconstitute(
            UUID id,
            QuotationNumber quotationNumber,
            UUID customerId,
            LocalDate creationDate,
            LocalDate deliveryDate,
            QuotationStatus status,
            UUID sellerId,
            String observations,
            List<QuotationItem> items
    ) {
        validateDeliveryDate(creationDate, deliveryDate);

        return new Quotation(
                id,
                quotationNumber,
                customerId,
                creationDate,
                deliveryDate,
                status,
                sellerId,
                observations,
                items
        );
    }

    public void addItem(String productName, int quantity, String fabric, String color, Money unitPrice) {
        addItem(productName, quantity, fabric, color, unitPrice, ProductSpecification.empty());
    }

    public void addItem(
            String productName,
            int quantity,
            String fabric,
            String color,
            Money unitPrice,
            ProductSpecification productSpecification
    ) {
        if (status != QuotationStatus.DRAFT) {
            throw new QuotationDomainException(
                    "Items can only be added while the quotation is draft. Current status: " + status
            );
        }

        validateQuantity(quantity);
        validateUnitPrice(unitPrice);

        QuotationItem item = QuotationItem.create(
                productName,
                quantity,
                fabric,
                color,
                unitPrice,
                productSpecification
        );
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

    /**
     * Aprueba la cotización tras la aceptación del cliente.
     * <p>
     * Transición válida: {@link QuotationStatus#DRAFT} → {@link QuotationStatus#APPROVED}.
     */
    public void approve() {
        if (status == QuotationStatus.APPROVED) {
            throw new QuotationDomainException("An approved quotation cannot be approved again");
        }

        if (status == QuotationStatus.CLOSED) {
            throw new QuotationDomainException("A closed quotation cannot be approved");
        }

        if (status != QuotationStatus.DRAFT) {
            throw new QuotationDomainException(
                    "Only a draft quotation can be approved. Current status: " + status
            );
        }

        ensureHasAtLeastOneProduct();

        if (total.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new QuotationDomainException("Quotation total must be greater than zero");
        }

        this.status = QuotationStatus.APPROVED;
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

    public void updateObservations(String newObservations) {
        this.observations = newObservations;
    }

    public UUID getId() {
        return id;
    }

    /**
     * Número comercial consecutivo. Puede ser {@code null} durante la transición
     * mientras existan cotizaciones históricas sin backfill.
     */
    public QuotationNumber getQuotationNumber() {
        return quotationNumber;
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

    public UUID getSellerId() {
        return sellerId;
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
        if (unitPrice.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
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

}
