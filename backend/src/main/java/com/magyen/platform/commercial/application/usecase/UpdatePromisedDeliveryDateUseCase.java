package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.UpdatePromisedDeliveryDateCommand;
import com.magyen.platform.commercial.application.dto.UpdatePromisedDeliveryDateResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Corrige la fecha prometida de una orden ya creada.
 * <p>
 * No modifica la fecha real, la confirmación, el estado, los totales, los descuentos,
 * los pagos ni la cotización de origen. DELIVERED y CLOSED siguen congelados.
 * Una fecha pasada es válida si no es anterior a la confirmación.
 * <p>
 * No reescribe trabajos de Plotter ni registros de producción. Esas validaciones
 * consultan la fecha prometida vigente solo cuando se registra o se modifica un trabajo.
 */
public class UpdatePromisedDeliveryDateUseCase {

    private final OrderRepository orderRepository;

    public UpdatePromisedDeliveryDateUseCase(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
    }

    @Transactional
    public UpdatePromisedDeliveryDateResult execute(UpdatePromisedDeliveryDateCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.orderId(), "Order id must not be null");
        if (command.promisedDeliveryDate() == null) {
            throw new OrderDomainException("Promised delivery date must not be null");
        }

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        order.changePromisedDeliveryDate(command.promisedDeliveryDate());
        Order saved = orderRepository.save(order);

        return new UpdatePromisedDeliveryDateResult(
                saved.getId(),
                saved.getConfirmationDate(),
                saved.getDeliveryCommitment().getPromisedDeliveryDate(),
                saved.getActualDeliveryDate(),
                saved.getStatus(),
                saved.getTotal().getAmount()
        );
    }
}
