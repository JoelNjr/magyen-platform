package com.magyen.platform.production.infrastructure.persistence.mapper;

import com.magyen.platform.production.domain.ProductionOperation;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOperationEntity;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOrderEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link ProductionOrder} y su modelo JPA.
 * <p>
 * No contiene reglas de negocio ni accede a la base de datos.
 */
public class ProductionPersistenceMapper {

    public ProductionOrderEntity toEntity(ProductionOrder productionOrder) {
        Objects.requireNonNull(productionOrder, "Production order must not be null");

        ProductionOrderEntity productionOrderEntity = new ProductionOrderEntity();
        productionOrderEntity.setId(productionOrder.getId());
        productionOrderEntity.setOrderId(productionOrder.getOrderId());
        productionOrderEntity.setCreationDate(productionOrder.getCreationDate());
        productionOrderEntity.setStatus(productionOrder.getStatus());
        productionOrderEntity.setPriority(productionOrder.getPriority());
        productionOrderEntity.setPlannedStartDate(productionOrder.getPlannedStartDate());
        productionOrderEntity.setPlannedEndDate(productionOrder.getPlannedEndDate());
        productionOrderEntity.setObservations(productionOrder.getObservations());

        List<ProductionOperationEntity> operationEntities = new ArrayList<>();
        for (ProductionOperation operation : productionOrder.getOperations()) {
            ProductionOperationEntity operationEntity = toOperationEntity(operation);
            operationEntity.setProductionOrder(productionOrderEntity);
            operationEntities.add(operationEntity);
        }
        productionOrderEntity.setOperations(operationEntities);

        return productionOrderEntity;
    }

    public ProductionOrder toDomain(ProductionOrderEntity productionOrderEntity) {
        Objects.requireNonNull(productionOrderEntity, "Production order entity must not be null");

        List<ProductionOperation> operations = new ArrayList<>();
        for (ProductionOperationEntity operationEntity : productionOrderEntity.getOperations()) {
            operations.add(toOperationDomain(operationEntity));
        }

        return ProductionOrder.reconstitute(
                productionOrderEntity.getId(),
                productionOrderEntity.getOrderId(),
                productionOrderEntity.getCreationDate(),
                productionOrderEntity.getStatus(),
                productionOrderEntity.getPriority(),
                productionOrderEntity.getPlannedStartDate(),
                productionOrderEntity.getPlannedEndDate(),
                productionOrderEntity.getObservations(),
                operations
        );
    }

    private ProductionOperationEntity toOperationEntity(ProductionOperation operation) {
        Objects.requireNonNull(operation, "Production operation must not be null");

        ProductionOperationEntity operationEntity = new ProductionOperationEntity();
        operationEntity.setId(operation.getId());
        operationEntity.setType(operation.getType());
        operationEntity.setStatus(operation.getStatus());
        operationEntity.setAssignedOperator(operation.getAssignedOperator());
        operationEntity.setPlannedStartDate(operation.getPlannedStartDate());
        operationEntity.setPlannedEndDate(operation.getPlannedEndDate());
        operationEntity.setActualStartDate(operation.getActualStartDate());
        operationEntity.setActualEndDate(operation.getActualEndDate());
        operationEntity.setObservations(operation.getObservations());
        return operationEntity;
    }

    private ProductionOperation toOperationDomain(ProductionOperationEntity operationEntity) {
        Objects.requireNonNull(operationEntity, "Production operation entity must not be null");

        return ProductionOperation.reconstitute(
                operationEntity.getId(),
                operationEntity.getType(),
                operationEntity.getStatus(),
                operationEntity.getAssignedOperator(),
                operationEntity.getPlannedStartDate(),
                operationEntity.getPlannedEndDate(),
                operationEntity.getActualStartDate(),
                operationEntity.getActualEndDate(),
                operationEntity.getObservations()
        );
    }
}
