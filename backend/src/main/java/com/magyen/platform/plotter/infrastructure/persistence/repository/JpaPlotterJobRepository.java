package com.magyen.platform.plotter.infrastructure.persistence.repository;

import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.infrastructure.persistence.entity.PlotterJobEntity;
import com.magyen.platform.plotter.infrastructure.persistence.mapper.PlotterPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link PlotterJobRepository}.
 */
@Repository
public class JpaPlotterJobRepository implements PlotterJobRepository {

    private final SpringDataPlotterJobRepository springDataPlotterJobRepository;
    private final PlotterPersistenceMapper plotterPersistenceMapper;

    public JpaPlotterJobRepository(
            SpringDataPlotterJobRepository springDataPlotterJobRepository,
            PlotterPersistenceMapper plotterPersistenceMapper
    ) {
        this.springDataPlotterJobRepository = Objects.requireNonNull(
                springDataPlotterJobRepository,
                "Spring Data Plotter Job repository must not be null"
        );
        this.plotterPersistenceMapper = Objects.requireNonNull(
                plotterPersistenceMapper,
                "Plotter persistence mapper must not be null"
        );
    }

    @Override
    public PlotterJob save(PlotterJob plotterJob) {
        Objects.requireNonNull(plotterJob, "Plotter job must not be null");

        PlotterJobEntity entity = plotterPersistenceMapper.toEntity(plotterJob);
        PlotterJobEntity savedEntity = springDataPlotterJobRepository.save(entity);
        return plotterPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PlotterJob> findById(UUID id) {
        Objects.requireNonNull(id, "Plotter job id must not be null");

        return springDataPlotterJobRepository.findById(id)
                .map(plotterPersistenceMapper::toDomain);
    }

    @Override
    public List<PlotterJob> findByOrderId(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");
        return springDataPlotterJobRepository.findByOrderId(orderId).stream()
                .map(plotterPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<PlotterJob> findAll() {
        return springDataPlotterJobRepository.findAll().stream()
                .map(plotterPersistenceMapper::toDomain)
                .toList();
    }
}
