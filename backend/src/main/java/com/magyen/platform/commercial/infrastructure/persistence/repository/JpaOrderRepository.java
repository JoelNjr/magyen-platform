package com.magyen.platform.commercial.infrastructure.persistence.repository;

import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.infrastructure.persistence.entity.OrderEntity;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.OrderPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    private final EntityManager entityManager;

    public JpaOrderRepository(
            SpringDataOrderRepository springDataOrderRepository,
            OrderPersistenceMapper orderPersistenceMapper,
            EntityManager entityManager
    ) {
        this.springDataOrderRepository = Objects.requireNonNull(
                springDataOrderRepository,
                "Spring Data Order repository must not be null"
        );
        this.orderPersistenceMapper = Objects.requireNonNull(
                orderPersistenceMapper,
                "Order persistence mapper must not be null"
        );
        this.entityManager = Objects.requireNonNull(entityManager, "Entity manager must not be null");
    }

    @Override
    @Transactional
    public Order save(Order order) {
        Objects.requireNonNull(order, "Order must not be null");

        // SizeBreakdown.replace assigns new IDs. Clearing persisted sizes first avoids
        // UNIQUE(order_item_id, size) collisions when labels overlap during merge.
        if (springDataOrderRepository.existsById(order.getId())) {
            entityManager.createQuery(
                            "delete from OrderItemSizeEntity sizeEntity "
                                    + "where sizeEntity.orderItem.order.id = :orderId"
                    )
                    .setParameter("orderId", order.getId())
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();
        }

        OrderEntity orderEntity = orderPersistenceMapper.toEntity(order);
        OrderEntity savedOrderEntity = springDataOrderRepository.save(orderEntity);
        return orderPersistenceMapper.toDomain(savedOrderEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        Objects.requireNonNull(id, "Order id must not be null");

        return springDataOrderRepository.findById(id)
                .map(orderPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByQuotationId(UUID quotationId) {
        Objects.requireNonNull(quotationId, "Quotation id must not be null");

        return springDataOrderRepository.findFirstByQuotationId(quotationId)
                .map(orderPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return springDataOrderRepository.findAll().stream()
                .map(orderPersistenceMapper::toDomain)
                .toList();
    }
}
