package com.magyen.platform.production.infrastructure.persistence.repository;

import com.magyen.platform.production.domain.ProductionOperator;
import com.magyen.platform.production.domain.ProductionOperatorRepository;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOperatorEntity;
import com.magyen.platform.production.infrastructure.persistence.mapper.ProductionOperatorPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link ProductionOperatorRepository}.
 * <p>
 * Traduce entre el agregado de dominio y el modelo JPA; nunca expone entidades de persistencia.
 */
@Repository
public class JpaProductionOperatorRepository implements ProductionOperatorRepository {

    private final SpringDataProductionOperatorJpaRepository springDataProductionOperatorJpaRepository;
    private final ProductionOperatorPersistenceMapper productionOperatorPersistenceMapper;

    public JpaProductionOperatorRepository(
            SpringDataProductionOperatorJpaRepository springDataProductionOperatorJpaRepository,
            ProductionOperatorPersistenceMapper productionOperatorPersistenceMapper
    ) {
        this.springDataProductionOperatorJpaRepository = Objects.requireNonNull(
                springDataProductionOperatorJpaRepository,
                "Spring Data Production Operator JPA repository must not be null"
        );
        this.productionOperatorPersistenceMapper = Objects.requireNonNull(
                productionOperatorPersistenceMapper,
                "Production operator persistence mapper must not be null"
        );
    }

    @Override
    public ProductionOperator save(ProductionOperator productionOperator) {
        Objects.requireNonNull(productionOperator, "Production operator must not be null");

        ProductionOperatorEntity entity = productionOperatorPersistenceMapper.toEntity(productionOperator);
        ProductionOperatorEntity savedEntity = springDataProductionOperatorJpaRepository.save(entity);
        return productionOperatorPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductionOperator> findById(UUID id) {
        Objects.requireNonNull(id, "Production operator id must not be null");

        return springDataProductionOperatorJpaRepository.findById(id)
                .map(productionOperatorPersistenceMapper::toDomain);
    }

    @Override
    public List<ProductionOperator> findAll() {
        return springDataProductionOperatorJpaRepository.findAll().stream()
                .map(productionOperatorPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<ProductionOperator> findAllActive() {
        return springDataProductionOperatorJpaRepository.findByActiveTrue().stream()
                .map(productionOperatorPersistenceMapper::toDomain)
                .toList();
    }
}
