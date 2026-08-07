package com.magyen.platform.finance.infrastructure.persistence.mapper;

import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentAmount;
import com.magyen.platform.finance.infrastructure.persistence.entity.PaymentEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link Payment} y su modelo JPA.
 * <p>
 * No contiene reglas de negocio ni accede a la base de datos.
 */
public class PaymentPersistenceMapper {

    public PaymentEntity toEntity(Payment payment) {
        Objects.requireNonNull(payment, "Payment must not be null");

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(payment.getId());
        paymentEntity.setOrderId(payment.getOrderId());
        paymentEntity.setAmount(payment.getAmount().getValue());
        paymentEntity.setPaymentDate(payment.getPaymentDate());
        paymentEntity.setObservations(payment.getObservations());
        return paymentEntity;
    }

    public Payment toDomain(PaymentEntity paymentEntity) {
        Objects.requireNonNull(paymentEntity, "Payment entity must not be null");

        return Payment.reconstitute(
                paymentEntity.getId(),
                paymentEntity.getOrderId(),
                PaymentAmount.of(paymentEntity.getAmount()),
                paymentEntity.getPaymentDate(),
                paymentEntity.getObservations()
        );
    }
}
