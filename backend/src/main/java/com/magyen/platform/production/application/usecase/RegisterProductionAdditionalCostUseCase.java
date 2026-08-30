package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.RegisterProductionAdditionalCostCommand;
import com.magyen.platform.production.application.dto.RegisterProductionAdditionalCostResult;
import com.magyen.platform.production.application.port.ProductionAdditionalCostFinancePort;
import com.magyen.platform.production.domain.ProductionAdditionalCost;
import com.magyen.platform.production.domain.ProductionDirectCostCategory;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.shared.domain.Money;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Registra un costo directo adicional (OTROS) y su EXPENSE en Finance.
 * <p>
 * Atomicidad: ledger + vínculo + save del agregado en la misma transacción.
 * sourceId = additionalCostId evita doble contabilización.
 */
public class RegisterProductionAdditionalCostUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionAdditionalCostFinancePort productionAdditionalCostFinancePort;

    public RegisterProductionAdditionalCostUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionAdditionalCostFinancePort productionAdditionalCostFinancePort
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.productionAdditionalCostFinancePort = Objects.requireNonNull(
                productionAdditionalCostFinancePort,
                "Production additional cost finance port must not be null"
        );
    }

    @Transactional
    public RegisterProductionAdditionalCostResult execute(RegisterProductionAdditionalCostCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        ProductionAdditionalCost additionalCost = productionOrder.registerAdditionalCost(
                ProductionDirectCostCategory.of(command.category()),
                command.description(),
                Money.of(command.amount()),
                command.incurredDate()
        );

        UUID financialTransactionId = productionAdditionalCostFinancePort.registerAdditionalCostExpense(
                additionalCost.getId(),
                additionalCost.getAmount().getAmount(),
                additionalCost.getIncurredDate(),
                additionalCost.getDescription()
        );
        additionalCost.assignFinancialTransaction(financialTransactionId);
        productionOrderRepository.save(productionOrder);

        return new RegisterProductionAdditionalCostResult(
                additionalCost.getId(),
                additionalCost.getProductionOrderId(),
                additionalCost.getCategory(),
                additionalCost.getDescription(),
                additionalCost.getAmount().getAmount(),
                additionalCost.getIncurredDate(),
                additionalCost.getFinancialTransactionId()
        );
    }

    private void validateCommand(RegisterProductionAdditionalCostCommand command) {
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
        if (command.category() == null || command.category().isBlank()) {
            throw new ProductionDomainException("Direct cost category must not be blank");
        }
        if (command.description() == null || command.description().isBlank()) {
            throw new ProductionDomainException("Additional cost description must not be blank");
        }
        Objects.requireNonNull(command.amount(), "Amount must not be null");
        LocalDate incurredDate = command.incurredDate();
        Objects.requireNonNull(incurredDate, "Incurred date must not be null");
    }
}
