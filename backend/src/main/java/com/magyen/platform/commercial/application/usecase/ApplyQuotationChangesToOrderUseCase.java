package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.OrderPaymentFloorGuard;
import com.magyen.platform.commercial.application.dto.ApplyQuotationChangesToOrderCommand;
import com.magyen.platform.commercial.application.dto.ApplyQuotationChangesToOrderResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Aplica el contenido comercial persistido de una cotización a su Orden.
 * <p>
 * Recarga cotización y orden actuales. No confía en un parche del cliente.
 * No muta pagos, ledger ni producción.
 */
public class ApplyQuotationChangesToOrderUseCase {

    private final QuotationRepository quotationRepository;
    private final OrderRepository orderRepository;
    private final OrderPaymentFloorGuard orderPaymentFloorGuard;

    public ApplyQuotationChangesToOrderUseCase(
            QuotationRepository quotationRepository,
            OrderRepository orderRepository,
            OrderPaymentFloorGuard orderPaymentFloorGuard
    ) {
        this.quotationRepository = Objects.requireNonNull(
                quotationRepository,
                "Quotation repository must not be null"
        );
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.orderPaymentFloorGuard = Objects.requireNonNull(
                orderPaymentFloorGuard,
                "Order payment floor guard must not be null"
        );
    }

    @Transactional
    public ApplyQuotationChangesToOrderResult execute(ApplyQuotationChangesToOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        Order order = orderRepository.findByQuotationId(quotation.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found for quotation: " + quotation.getId()
                ));

        order.applyQuotationCommercialSource(quotation.getItems(), quotation.getDiscount());
        orderPaymentFloorGuard.ensureTotalCoversAmountPaid(order);

        Order saved = orderRepository.save(order);
        return new ApplyQuotationChangesToOrderResult(
                saved.getId(),
                saved.getSubtotal().getAmount(),
                saved.getDiscount().getAmount(),
                saved.getTotal().getAmount()
        );
    }
}
