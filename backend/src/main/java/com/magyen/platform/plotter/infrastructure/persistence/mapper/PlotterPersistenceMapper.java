package com.magyen.platform.plotter.infrastructure.persistence.mapper;

import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.infrastructure.persistence.entity.PlotterJobEntity;

import java.util.Objects;

/**
 * Traduce entre el agregado de dominio Plotter y el modelo JPA.
 */
public class PlotterPersistenceMapper {

    public PlotterJobEntity toEntity(PlotterJob plotterJob) {
        Objects.requireNonNull(plotterJob, "Plotter job must not be null");

        PlotterJobEntity entity = new PlotterJobEntity();
        entity.setId(plotterJob.getId());
        entity.setJobType(plotterJob.getJobType());
        entity.setCustomerId(plotterJob.getCustomerId());
        entity.setOrderId(plotterJob.getOrderId());
        entity.setCreationDate(plotterJob.getCreationDate());
        entity.setPaperInventoryItemId(plotterJob.getPaperInventoryItemId());
        entity.setPrintedMeters(plotterJob.getPrintedMeters());
        entity.setPricePerMeter(plotterJob.getPricePerMeter());
        entity.setTotalAmount(plotterJob.getTotalAmount());
        entity.setStatus(plotterJob.getStatus());
        entity.setObservations(plotterJob.getObservations());
        return entity;
    }

    public PlotterJob toDomain(PlotterJobEntity entity) {
        Objects.requireNonNull(entity, "Plotter job entity must not be null");

        return PlotterJob.reconstitute(
                entity.getId(),
                entity.getJobType(),
                entity.getCustomerId(),
                entity.getOrderId(),
                entity.getCreationDate(),
                entity.getPaperInventoryItemId(),
                entity.getPrintedMeters(),
                entity.getPricePerMeter(),
                entity.getTotalAmount(),
                entity.getStatus(),
                entity.getObservations()
        );
    }
}
