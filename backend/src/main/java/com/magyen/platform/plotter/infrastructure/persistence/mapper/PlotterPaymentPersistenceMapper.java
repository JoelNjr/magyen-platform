package com.magyen.platform.plotter.infrastructure.persistence.mapper;

import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.infrastructure.persistence.entity.PlotterPaymentEntity;

import java.util.Objects;

/**
 * Convierte entre dominio y entidad JPA de pagos de Plotter.
 */
public class PlotterPaymentPersistenceMapper {

    public PlotterPaymentEntity toEntity(PlotterPayment payment) {
        Objects.requireNonNull(payment, "Plotter payment must not be null");

        PlotterPaymentEntity entity = new PlotterPaymentEntity();
        entity.setId(payment.getId());
        entity.setPlotterJobId(payment.getPlotterJobId());
        entity.setAmount(payment.getAmount());
        entity.setPaymentDate(payment.getPaymentDate());
        entity.setObservations(payment.getObservations());
        return entity;
    }

    public PlotterPayment toDomain(PlotterPaymentEntity entity) {
        Objects.requireNonNull(entity, "Plotter payment entity must not be null");

        return PlotterPayment.reconstitute(
                entity.getId(),
                entity.getPlotterJobId(),
                entity.getAmount(),
                entity.getPaymentDate(),
                entity.getObservations()
        );
    }
}
