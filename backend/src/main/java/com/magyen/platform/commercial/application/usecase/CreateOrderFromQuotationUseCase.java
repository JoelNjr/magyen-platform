package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationItem;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.QuotationStatus;
import com.magyen.platform.commercial.domain.exception.OrderAlreadyExistsForQuotationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que coordina la creación de una Orden a partir de una Cotización aprobada.
 * <p>
 * El número de orden es el consecutivo ya reservado por la cotización de origen.
 * No se acepta un número escrito por el cliente ni se abre una secuencia nueva.
 */
public class CreateOrderFromQuotationUseCase {

    private final QuotationRepository quotationRepository;
    private final OrderRepository orderRepository;

    public CreateOrderFromQuotationUseCase(
            QuotationRepository quotationRepository,
            OrderRepository orderRepository
    ) {
        this.quotationRepository = Objects.requireNonNull(
                quotationRepository,
                "Quotation repository must not be null"
        );
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
    }

    public CreateOrderFromQuotationResult execute(CreateOrderFromQuotationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        if (quotation.getStatus() != QuotationStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Quotation must be approved to create an order. Current status: " + quotation.getStatus()
            );
        }

        if (orderRepository.findByQuotationId(quotation.getId()).isPresent()) {
            throw new OrderAlreadyExistsForQuotationException();
        }

        LocalDate confirmationDate = command.confirmationDate() != null
                ? command.confirmationDate()
                : LocalDate.now();

        if (confirmationDate.isBefore(quotation.getCreationDate())) {
            throw new IllegalArgumentException(
                    "Confirmation date must not be before quotation date"
            );
        }

        Order order = Order.create(
                reservedOrderNumber(quotation),
                quotation.getCustomerId(),
                quotation.getId(),
                confirmationDate,
                DeliveryCommitment.of(command.deliveryDate()),
                quotation.getSellerId(),
                command.observations(),
                command.description(),
                mapItems(quotation.getItems()),
                quotation.getDiscount()
        );

        Order savedOrder = orderRepository.save(order);

        return new CreateOrderFromQuotationResult(
                savedOrder.getId(),
                savedOrder.getOrderNumber().getValue(),
                savedOrder.getStatus(),
                savedOrder.getConfirmationDate()
        );
    }

    private void validateCommand(CreateOrderFromQuotationCommand command) {
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");
        Objects.requireNonNull(command.deliveryDate(), "Delivery date must not be null");
    }

    private OrderNumber reservedOrderNumber(Quotation quotation) {
        if (quotation.getQuotationNumber() == null) {
            throw new IllegalArgumentException(
                    "Quotation has no commercial number reserved; an order cannot be created"
            );
        }
        return OrderNumber.fromQuotationNumber(quotation.getQuotationNumber());
    }

    private List<OrderItem> mapItems(List<QuotationItem> quotationItems) {
        return quotationItems.stream()
                .map(this::mapItem)
                .toList();
    }

    private OrderItem mapItem(QuotationItem quotationItem) {
        return OrderItem.reconstitute(
                UUID.randomUUID(),
                quotationItem.getProductName(),
                quotationItem.getQuantity(),
                quotationItem.getFabric(),
                quotationItem.getSecondaryFabric(),
                quotationItem.getColor(),
                quotationItem.getUnitPrice(),
                quotationItem.getProductSpecification(),
                List.of()
        );
    }
}
