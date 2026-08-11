package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.CancelProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.CancelProductionLaborWorkResult;
import com.magyen.platform.production.domain.ProductionLaborWork;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Cancela un registro PENDING de mano de obra. No crea movimiento financiero.
 */
public class CancelProductionLaborWorkUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public CancelProductionLaborWorkUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    @Transactional
    public CancelProductionLaborWorkResult execute(CancelProductionLaborWorkCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
        Objects.requireNonNull(command.laborWorkId(), "Labor work id must not be null");

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        ProductionLaborWork laborWork = productionOrder.requireLaborWork(command.laborWorkId());
        laborWork.cancel();
        productionOrderRepository.save(productionOrder);

        return new CancelProductionLaborWorkResult(
                laborWork.getId(),
                laborWork.getProductionOrderId(),
                laborWork.getStatus()
        );
    }
}
