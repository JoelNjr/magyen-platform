package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.finance.infrastructure.persistence.entity.PaymentEntity;
import com.magyen.platform.finance.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link PaymentRepository}.
 * <p>
 * Traduce entre el agregado de dominio y el modelo JPA; nunca expone entidades de persistencia.
 */
@Repository
public class JpaPaymentRepository implements PaymentRepository {

    private final SpringDataPaymentRepository springDataPaymentRepository;
    private final PaymentPersistenceMapper paymentPersistenceMapper;

    public JpaPaymentRepository(
            SpringDataPaymentRepository springDataPaymentRepository,
            PaymentPersistenceMapper paymentPersistenceMapper
    ) {
        this.springDataPaymentRepository = Objects.requireNonNull(
                springDataPaymentRepository,
                "Spring Data Payment repository must not be null"
        );
        this.paymentPersistenceMapper = Objects.requireNonNull(
                paymentPersistenceMapper,
                "Payment persistence mapper must not be null"
        );
    }

    @Override
    public Payment save(Payment payment) {
        Objects.requireNonNull(payment, "Payment must not be null");

        PaymentEntity paymentEntity = paymentPersistenceMapper.toEntity(payment);
        PaymentEntity savedPaymentEntity = springDataPaymentRepository.save(paymentEntity);
        return paymentPersistenceMapper.toDomain(savedPaymentEntity);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        Objects.requireNonNull(id, "Payment id must not be null");

        return springDataPaymentRepository.findById(id)
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public List<Payment> findByOrderId(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");

        return springDataPaymentRepository.findByOrderId(orderId).stream()
                .map(paymentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Payment> findAll() {
        return springDataPaymentRepository.findAll().stream()
                .map(paymentPersistenceMapper::toDomain)
                .toList();
    }
}
