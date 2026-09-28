package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderProfitabilityEligibility;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que registra un movimiento de ingreso o gasto en el ledger financiero.
 * <p>
 * {@code orderId} es opcional. Si viene, el mismo movimiento queda atribuido a la
 * Orden y la rentabilidad lo lee como costo otro. No crea un segundo gasto.
 */
public class RegisterFinancialTransactionUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final OrderRepository orderRepository;

    public RegisterFinancialTransactionUseCase(
            FinancialTransactionRepository financialTransactionRepository,
            OrderRepository orderRepository
    ) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
    }

    public RegisterFinancialTransactionResult execute(RegisterFinancialTransactionCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        FinancialTransactionSourceType sourceType = command.sourceType() == null
                ? FinancialTransactionSourceType.MANUAL
                : command.sourceType();
        UUID orderId = command.orderId();
        if (orderId != null) {
            validateOrderAssociation(command.type(), sourceType, orderId);
        }

        FinancialTransaction transaction = FinancialTransaction.create(
                command.type(),
                FinancialAmount.of(command.amount()),
                command.transactionDate(),
                command.category(),
                command.description(),
                command.observation(),
                sourceType,
                command.sourceId(),
                orderId
        );

        FinancialTransaction saved = financialTransactionRepository.save(transaction);

        return new RegisterFinancialTransactionResult(
                saved.getId(),
                saved.getType(),
                saved.getAmount().getValue(),
                saved.getTransactionDate(),
                saved.getCategory(),
                saved.getDescription(),
                saved.getObservation(),
                saved.getSourceType(),
                saved.getSourceId(),
                saved.getOrderId()
        );
    }

    private void validateCommand(RegisterFinancialTransactionCommand command) {
        if (command.type() == null) {
            throw new FinanceDomainException("Transaction type must not be null");
        }
        if (command.amount() == null) {
            throw new FinanceDomainException("Amount must not be null");
        }
        if (command.transactionDate() == null) {
            throw new FinanceDomainException("Transaction date must not be null");
        }
        if (command.category() == null || command.category().isBlank()) {
            throw new FinanceDomainException("Category must not be blank");
        }
    }

    /**
     * Solo un gasto manual puede atribuirse a un pedido. Los ingresos no entran
     * en costos otros, y los orígenes ya contabilizados (producción, Plotter,
     * nómina, inventario) no se reatribuyen por esta vía.
     */
    private void validateOrderAssociation(
            FinancialTransactionType type,
            FinancialTransactionSourceType sourceType,
            UUID orderId
    ) {
        if (type != FinancialTransactionType.EXPENSE) {
            throw new FinanceDomainException(
                    "Only an expense can be associated with a commercial order"
            );
        }
        if (sourceType != FinancialTransactionSourceType.MANUAL) {
            throw new FinanceDomainException(
                    "An order association is only valid for a manual expense"
            );
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new FinanceDomainException("Order not found: " + orderId));
        if (!OrderProfitabilityEligibility.includes(order.getStatus())) {
            throw new FinanceDomainException(
                    "The commercial order cannot receive an attributed expense. Current status: "
                            + order.getStatus()
            );
        }
    }
}
