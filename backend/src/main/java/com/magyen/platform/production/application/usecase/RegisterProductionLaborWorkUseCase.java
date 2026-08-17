package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkResult;
import com.magyen.platform.production.application.port.ProductionLaborEmployeePort;
import com.magyen.platform.production.application.port.ProductionLaborOperatorInfo;
import com.magyen.platform.production.domain.ProductionLaborWork;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Registra trabajo de mano de obra PENDING en una Orden de Producción IN_PROGRESS.
 * <p>
 * No crea movimiento financiero. Valida operario de producción activo vía puerto.
 */
public class RegisterProductionLaborWorkUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionLaborEmployeePort productionLaborEmployeePort;

    public RegisterProductionLaborWorkUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionLaborEmployeePort productionLaborEmployeePort
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.productionLaborEmployeePort = Objects.requireNonNull(
                productionLaborEmployeePort,
                "Production labor employee port must not be null"
        );
    }

    @Transactional
    public RegisterProductionLaborWorkResult execute(RegisterProductionLaborWorkCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionLaborOperatorInfo operator = productionLaborEmployeePort.requireEligibleProductionOperator(
                command.operatorEmployeeId()
        );

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        ProductionLaborWork laborWork = productionOrder.registerLaborWork(
                command.operatorEmployeeId(),
                command.workDate(),
                command.operation(),
                command.quantity(),
                command.unitOfMeasure(),
                command.unitRate(),
                command.observation()
        );

        productionOrderRepository.save(productionOrder);

        return new RegisterProductionLaborWorkResult(
                laborWork.getId(),
                laborWork.getProductionOrderId(),
                laborWork.getOperatorEmployeeId(),
                operator.displayName(),
                laborWork.getWorkDate(),
                laborWork.getOperation(),
                laborWork.getQuantity(),
                laborWork.getUnitOfMeasure(),
                laborWork.getUnitRate(),
                laborWork.getCalculatedAmount(),
                laborWork.getObservation(),
                laborWork.getStatus()
        );
    }

    private void validateCommand(RegisterProductionLaborWorkCommand command) {
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
        Objects.requireNonNull(command.operatorEmployeeId(), "Operator employee id must not be null");
        Objects.requireNonNull(command.workDate(), "Work date must not be null");
        Objects.requireNonNull(command.quantity(), "Quantity must not be null");
        Objects.requireNonNull(command.unitRate(), "Unit rate must not be null");
    }
}
