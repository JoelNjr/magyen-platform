package com.magyen.platform.production.infrastructure.persistence.mapper;

import com.magyen.platform.production.domain.ProductionOperator;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOperatorEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link ProductionOperator} y su modelo JPA.
 * <p>
 * No contiene reglas de negocio ni accede a la base de datos.
 */
public class ProductionOperatorPersistenceMapper {

    public ProductionOperatorEntity toEntity(ProductionOperator productionOperator) {
        Objects.requireNonNull(productionOperator, "Production operator must not be null");

        ProductionOperatorEntity entity = new ProductionOperatorEntity();
        entity.setId(productionOperator.getId());
        entity.setName(productionOperator.getName());
        entity.setActive(productionOperator.isActive());
        return entity;
    }

    public ProductionOperator toDomain(ProductionOperatorEntity entity) {
        Objects.requireNonNull(entity, "Production operator entity must not be null");

        return ProductionOperator.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.isActive()
        );
    }
}
