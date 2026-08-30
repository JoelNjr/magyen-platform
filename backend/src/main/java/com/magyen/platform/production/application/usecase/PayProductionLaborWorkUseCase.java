package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.PayProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.PayProductionLaborWorkResult;
import com.magyen.platform.production.application.port.ProductionLaborEmployeePort;
import com.magyen.platform.production.application.port.ProductionLaborFinancePort;
import com.magyen.platform.production.domain.ProductionLaborWork;
import com.magyen.platform.production.domain.ProductionLaborWorkStatus;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.production.domain.exception.ProductionLaborWorkAlreadyPaidException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Paga un registro PENDING y acumula el EXPENSE PAYROLL semanal en Finance.
 * <p>
 * Atomicidad: ledger + markPaid + save del agregado en la misma transacción.
 */
public class PayProductionLaborWorkUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionLaborFinancePort productionLaborFinancePort;
    private final ProductionLaborEmployeePort productionLaborEmployeePort;

    public PayProductionLaborWorkUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionLaborFinancePort productionLaborFinancePort,
            ProductionLaborEmployeePort productionLaborEmployeePort
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.productionLaborFinancePort = Objects.requireNonNull(
                productionLaborFinancePort,
                "Production labor finance port must not be null"
        );
        this.productionLaborEmployeePort = Objects.requireNonNull(
                productionLaborEmployeePort,
                "Production labor employee port must not be null"
        );
    }

    @Transactional
    public PayProductionLaborWorkResult execute(PayProductionLaborWorkCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
        Objects.requireNonNull(command.laborWorkId(), "Labor work id must not be null");

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        ProductionLaborWork laborWork = productionOrder.requireLaborWork(command.laborWorkId());

        if (laborWork.getStatus() == ProductionLaborWorkStatus.PAID) {
            throw new ProductionLaborWorkAlreadyPaidException();
        }
        if (laborWork.getStatus() != ProductionLaborWorkStatus.PENDING) {
            throw new ProductionDomainException(
                    "Only PENDING production labor work can be paid. Current status: " + laborWork.getStatus()
            );
        }

        String operatorDisplayName = productionLaborEmployeePort
                .findOperatorDisplayName(laborWork.getOperatorEmployeeId())
                .orElse(null);

        LocalDate paymentDate = command.paymentDate() == null ? LocalDate.now() : command.paymentDate();
        LocalDateTime paidAt = LocalDateTime.now();

        UUID financialTransactionId = productionLaborFinancePort.registerLaborExpense(
                laborWork.getId(),
                laborWork.getCalculatedAmount(),
                paymentDate,
                operatorDisplayName,
                command.observation()
        );

        laborWork.markPaid(financialTransactionId, paidAt);
        productionOrderRepository.save(productionOrder);

        return new PayProductionLaborWorkResult(
                laborWork.getId(),
                laborWork.getProductionOrderId(),
                laborWork.getStatus(),
                laborWork.getCalculatedAmount(),
                laborWork.getPaidAt(),
                laborWork.getFinancialTransactionId()
        );
    }
}
