package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.CreateProductionOperatorCommand;
import com.magyen.platform.production.application.dto.CreateProductionOperatorResult;
import com.magyen.platform.production.domain.ProductionOperator;
import com.magyen.platform.production.domain.ProductionOperatorRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la creación de un operario de producción.
 */
public class CreateProductionOperatorUseCase {

    private final ProductionOperatorRepository productionOperatorRepository;

    public CreateProductionOperatorUseCase(ProductionOperatorRepository productionOperatorRepository) {
        this.productionOperatorRepository = Objects.requireNonNull(
                productionOperatorRepository,
                "Production operator repository must not be null"
        );
    }

    public CreateProductionOperatorResult execute(CreateProductionOperatorCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        String name = command.name().trim();
        ensureNameIsUnique(name);

        ProductionOperator operator = ProductionOperator.create(name);
        ProductionOperator savedOperator = productionOperatorRepository.save(operator);

        return new CreateProductionOperatorResult(
                savedOperator.getId(),
                savedOperator.getName(),
                savedOperator.isActive()
        );
    }

    private void validateCommand(CreateProductionOperatorCommand command) {
        if (command.name() == null) {
            throw new IllegalArgumentException("Production operator name must not be null");
        }

        if (command.name().isBlank()) {
            throw new IllegalArgumentException("Production operator name must not be blank");
        }
    }

    private void ensureNameIsUnique(String name) {
        boolean nameAlreadyExists = productionOperatorRepository.findAll().stream()
                .anyMatch(operator -> operator.getName().equalsIgnoreCase(name));

        if (nameAlreadyExists) {
            throw new IllegalArgumentException("Production operator name already exists: " + name);
        }
    }
}
