package com.magyen.platform.commercial.infrastructure.persistence.repository;

import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.infrastructure.persistence.entity.OrderEntity;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.OrderPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link OrderRepository}.
 * <p>
 * Traduce entre el agregado de dominio y el modelo JPA; nunca expone entidades de persistencia.
 */
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository springDataOrderRepository;
    private final OrderPersistenceMapper orderPersistenceMapper;

    public JpaOrderRepository(
            SpringDataOrderRepository springDataOrderRepository,
            OrderPersistenceMapper orderPersistenceMapper
    ) {
        this.springDataOrderRepository = Objects.requireNonNull(
                springDataOrderRepository,
                "Spring Data Order repository must not be null"
        );
        this.orderPersistenceMapper = Objects.requireNonNull(
                orderPersistenceMapper,
                "Order persistence mapper must not be null"
        );
    }

    @Override
    public Order save(Order order) {
        Objects.requireNonNull(order, "Order must not be null");

        OrderEntity orderEntity = orderPersistenceMapper.toEntity(order);
        OrderEntity savedOrderEntity = springDataOrderRepository.save(orderEntity);
        return orderPersistenceMapper.toDomain(savedOrderEntity);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        Objects.requireNonNull(id, "Order id must not be null");

        return springDataOrderRepository.findById(id)
                .map(orderPersistenceMapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return springDataOrderRepository.findAll().stream()
                .map(orderPersistenceMapper::toDomain)
                .toList();
    }
}
