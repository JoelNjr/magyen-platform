package com.magyen.platform.plotter.infrastructure.persistence.repository;

import com.magyen.platform.plotter.domain.PlotterMachine;
import com.magyen.platform.plotter.domain.PlotterMachineRepository;
import com.magyen.platform.plotter.infrastructure.persistence.entity.PlotterMachineEntity;
import com.magyen.platform.plotter.infrastructure.persistence.mapper.PlotterMachinePersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del puerto {@link PlotterMachineRepository}.
 */
@Repository
public class JpaPlotterMachineRepository implements PlotterMachineRepository {

    private final SpringDataPlotterMachineRepository springDataPlotterMachineRepository;
    private final PlotterMachinePersistenceMapper plotterMachinePersistenceMapper;

    public JpaPlotterMachineRepository(
            SpringDataPlotterMachineRepository springDataPlotterMachineRepository,
            PlotterMachinePersistenceMapper plotterMachinePersistenceMapper
    ) {
        this.springDataPlotterMachineRepository = Objects.requireNonNull(
                springDataPlotterMachineRepository,
                "Spring Data Plotter machine repository must not be null"
        );
        this.plotterMachinePersistenceMapper = Objects.requireNonNull(
                plotterMachinePersistenceMapper,
                "Plotter machine persistence mapper must not be null"
        );
    }

    @Override
    public List<PlotterMachine> findAll() {
        return springDataPlotterMachineRepository.findAll().stream()
                .map(plotterMachinePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<PlotterMachine> findById(UUID id) {
        Objects.requireNonNull(id, "Plotter machine id must not be null");
        return springDataPlotterMachineRepository.findById(id)
                .map(plotterMachinePersistenceMapper::toDomain);
    }

    @Override
    public Optional<PlotterMachine> findDefaultActive() {
        return springDataPlotterMachineRepository.findFirstByDefaultForRegistrationTrueAndActiveTrue()
                .map(plotterMachinePersistenceMapper::toDomain);
    }

    @Override
    public PlotterMachine save(PlotterMachine plotterMachine) {
        Objects.requireNonNull(plotterMachine, "Plotter machine must not be null");
        PlotterMachineEntity saved = springDataPlotterMachineRepository.save(
                plotterMachinePersistenceMapper.toEntity(plotterMachine)
        );
        return plotterMachinePersistenceMapper.toDomain(saved);
    }
}
