package com.magyen.platform.production.infrastructure.persistence;

import com.magyen.platform.production.application.port.ProductionLaborEmployeePort;
import com.magyen.platform.production.application.port.ProductionLaborOperatorInfo;
import com.magyen.platform.production.domain.ProductionOperator;
import com.magyen.platform.production.domain.ProductionOperatorRepository;
import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador Production-owned para resolver operarios de mano de obra.
 * <p>
 * Sustituye la resolución previa contra nómina de Finance.
 * El UUID histórico {@code operatorEmployeeId} apunta ahora a {@code production_operators.id}.
 */
public class ProductionLaborOperatorAdapter implements ProductionLaborEmployeePort {

    private final ProductionOperatorRepository productionOperatorRepository;

    public ProductionLaborOperatorAdapter(ProductionOperatorRepository productionOperatorRepository) {
        this.productionOperatorRepository = Objects.requireNonNull(
                productionOperatorRepository,
                "Production operator repository must not be null"
        );
    }

    @Override
    public ProductionLaborOperatorInfo requireEligibleProductionOperator(UUID operatorEmployeeId) {
        Objects.requireNonNull(operatorEmployeeId, "Operator employee id must not be null");

        ProductionOperator operator = productionOperatorRepository.findById(operatorEmployeeId)
                .orElseThrow(() -> new ProductionDomainException(
                        "Production operator not found: " + operatorEmployeeId
                ));

        if (!operator.isActive()) {
            throw new ProductionDomainException(
                    "Only active production operators can receive production labor work"
            );
        }

        return new ProductionLaborOperatorInfo(operator.getId(), operator.getName());
    }

    @Override
    public List<ProductionLaborOperatorInfo> listActiveProductionBasedOperators() {
        return productionOperatorRepository.findAllActive().stream()
                .map(operator -> new ProductionLaborOperatorInfo(operator.getId(), operator.getName()))
                .toList();
    }

    @Override
    public Optional<String> findOperatorDisplayName(UUID operatorEmployeeId) {
        Objects.requireNonNull(operatorEmployeeId, "Operator employee id must not be null");
        return productionOperatorRepository.findById(operatorEmployeeId)
                .map(ProductionOperator::getName);
    }
}
