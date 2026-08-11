package com.magyen.platform.plotter.infrastructure.persistence.repository;

import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.infrastructure.persistence.entity.PlotterPaymentEntity;
import com.magyen.platform.plotter.infrastructure.persistence.mapper.PlotterPaymentPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura del port {@link PlotterPaymentRepository}.
 */
@Repository
public class JpaPlotterPaymentRepository implements PlotterPaymentRepository {

    private final SpringDataPlotterPaymentRepository springDataRepository;
    private final PlotterPaymentPersistenceMapper persistenceMapper;

    public JpaPlotterPaymentRepository(
            SpringDataPlotterPaymentRepository springDataRepository,
            PlotterPaymentPersistenceMapper persistenceMapper
    ) {
        this.springDataRepository = Objects.requireNonNull(
                springDataRepository,
                "Spring Data plotter payment repository must not be null"
        );
        this.persistenceMapper = Objects.requireNonNull(
                persistenceMapper,
                "Plotter payment persistence mapper must not be null"
        );
    }

    @Override
    public PlotterPayment save(PlotterPayment payment) {
        Objects.requireNonNull(payment, "Plotter payment must not be null");
        PlotterPaymentEntity saved = springDataRepository.save(persistenceMapper.toEntity(payment));
        return persistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<PlotterPayment> findById(UUID id) {
        Objects.requireNonNull(id, "Plotter payment id must not be null");
        return springDataRepository.findById(id).map(persistenceMapper::toDomain);
    }

    @Override
    public List<PlotterPayment> findByPlotterJobIdNewestFirst(UUID plotterJobId) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        return springDataRepository
                .findByPlotterJobIdOrderByPaymentDateDescIdDesc(plotterJobId)
                .stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }
}
