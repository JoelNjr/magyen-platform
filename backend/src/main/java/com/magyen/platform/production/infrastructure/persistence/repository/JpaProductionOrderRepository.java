package com.magyen.platform.production.infrastructure.persistence.repository;

import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOrderEntity;
import com.magyen.platform.production.infrastructure.persistence.mapper.ProductionPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link ProductionOrderRepository}.
 * <p>
 * Traduce entre el agregado de dominio y el modelo JPA; nunca expone entidades de persistencia.
 */
@Repository
public class JpaProductionOrderRepository implements ProductionOrderRepository {

    private final SpringDataProductionOrderRepository springDataProductionOrderRepository;
    private final ProductionPersistenceMapper productionPersistenceMapper;

    public JpaProductionOrderRepository(
            SpringDataProductionOrderRepository springDataProductionOrderRepository,
            ProductionPersistenceMapper productionPersistenceMapper
    ) {
        this.springDataProductionOrderRepository = Objects.requireNonNull(
                springDataProductionOrderRepository,
                "Spring Data Production Order repository must not be null"
        );
        this.productionPersistenceMapper = Objects.requireNonNull(
                productionPersistenceMapper,
                "Production persistence mapper must not be null"
        );
    }

    @Override
    public ProductionOrder save(ProductionOrder productionOrder) {
        Objects.requireNonNull(productionOrder, "Production order must not be null");

        ProductionOrderEntity productionOrderEntity = productionPersistenceMapper.toEntity(productionOrder);
        ProductionOrderEntity savedProductionOrderEntity =
                springDataProductionOrderRepository.save(productionOrderEntity);
        return productionPersistenceMapper.toDomain(savedProductionOrderEntity);
    }

    @Override
    public Optional<ProductionOrder> findById(UUID id) {
        Objects.requireNonNull(id, "Production order id must not be null");

        return springDataProductionOrderRepository.findById(id)
                .map(productionPersistenceMapper::toDomain);
    }

    @Override
    public Optional<ProductionOrder> findByOrderId(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");

        return springDataProductionOrderRepository.findByOrderId(orderId)
                .map(productionPersistenceMapper::toDomain);
    }

    @Override
    public List<ProductionOrder> findAll() {
        return springDataProductionOrderRepository.findAll().stream()
                .map(productionPersistenceMapper::toDomain)
                .toList();
    }
}
