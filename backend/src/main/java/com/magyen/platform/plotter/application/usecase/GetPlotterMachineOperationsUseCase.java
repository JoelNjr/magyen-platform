package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterMachineOperationsQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterMachineOperationsResult;
import com.magyen.platform.plotter.application.dto.PlotterMachineOperationResult;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterMachine;
import com.magyen.platform.plotter.domain.PlotterMachineRepository;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Totales operativos por máquina.
 * <p>
 * La cantidad de trabajos, los metros impresos y los metros de merma usan
 * {@link PlotterJob#getCreationDate()}. El ingreso externo suma los pagos reales
 * cuya fecha cae en el período, de trabajos externos de esa máquina.
 * No suma compras de papel ni de tinta y no modifica la rentabilidad consolidada.
 */
public class GetPlotterMachineOperationsUseCase {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO_METERS = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterMachineRepository plotterMachineRepository;

    public GetPlotterMachineOperationsUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterMachineRepository plotterMachineRepository
    ) {
        this.plotterJobRepository = Objects.requireNonNull(plotterJobRepository, "Plotter job repository must not be null");
        this.plotterPaymentRepository = Objects.requireNonNull(
                plotterPaymentRepository,
                "Plotter payment repository must not be null"
        );
        this.plotterMachineRepository = Objects.requireNonNull(
                plotterMachineRepository,
                "Plotter machine repository must not be null"
        );
    }

    public GetPlotterMachineOperationsResult execute(GetPlotterMachineOperationsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        if (query.fromDate() == null || query.toDate() == null) {
            throw new PlotterDomainException("Both fromDate and toDate must be provided together");
        }
        if (query.fromDate().isAfter(query.toDate())) {
            throw new PlotterDomainException("From date must not be after to date");
        }
        if (query.plotterMachineId() != null && query.unassignedMachineOnly()) {
            throw new PlotterDomainException("Choose either a Plotter machine or unassigned jobs");
        }

        Map<UUID, Accumulator> accumulators = new LinkedHashMap<>();
        List<PlotterMachine> machines = plotterMachineRepository.findAll().stream()
                .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                .toList();
        if (query.unassignedMachineOnly()) {
            accumulators.put(null, new Accumulator(null, null));
        } else if (query.plotterMachineId() != null) {
            PlotterMachine selected = machines.stream()
                    .filter(machine -> machine.getId().equals(query.plotterMachineId()))
                    .findFirst()
                    .orElseThrow(() -> new PlotterDomainException(
                            "Plotter machine not found: " + query.plotterMachineId()
                    ));
            accumulators.put(selected.getId(), new Accumulator(selected.getId(), selected.getName()));
        } else {
            for (PlotterMachine machine : machines) {
                accumulators.put(machine.getId(), new Accumulator(machine.getId(), machine.getName()));
            }
            accumulators.put(null, new Accumulator(null, null));
        }

        for (PlotterJob job : plotterJobRepository.findAll()) {
            if (!belongsToSelection(job.getPlotterMachineId(), query)) {
                continue;
            }
            Accumulator accumulator = accumulators.computeIfAbsent(
                    job.getPlotterMachineId(),
                    id -> new Accumulator(id, null)
            );
            if (inRange(job.getCreationDate(), query.fromDate(), query.toDate())) {
                accumulator.jobCount++;
                if (job.getJobType().isWaste()) {
                    accumulator.wasteMeters = accumulator.wasteMeters.add(job.getPrintedMeters());
                } else {
                    accumulator.printedMeters = accumulator.printedMeters.add(job.getPrintedMeters());
                }
            }
            if (!job.getJobType().isExternal()) {
                continue;
            }
            for (PlotterPayment payment : plotterPaymentRepository.findByPlotterJobIdNewestFirst(job.getId())) {
                if (inRange(payment.getPaymentDate(), query.fromDate(), query.toDate())) {
                    accumulator.externalRevenue = accumulator.externalRevenue.add(payment.getAmount());
                }
            }
        }

        List<PlotterMachineOperationResult> rows = new ArrayList<>();
        for (Accumulator accumulator : accumulators.values()) {
            rows.add(accumulator.toResult());
        }
        return new GetPlotterMachineOperationsResult(query.fromDate(), query.toDate(), List.copyOf(rows));
    }

    private static boolean belongsToSelection(UUID machineId, GetPlotterMachineOperationsQuery query) {
        if (query.unassignedMachineOnly()) {
            return machineId == null;
        }
        if (query.plotterMachineId() == null) {
            return true;
        }
        return query.plotterMachineId().equals(machineId);
    }

    private static boolean inRange(LocalDate value, LocalDate fromDate, LocalDate toDate) {
        return value != null && !value.isBefore(fromDate) && !value.isAfter(toDate);
    }

    private static final class Accumulator {
        private final UUID plotterMachineId;
        private final String machineName;
        private int jobCount;
        private BigDecimal printedMeters = ZERO_METERS;
        private BigDecimal wasteMeters = ZERO_METERS;
        private BigDecimal externalRevenue = ZERO_MONEY;

        private Accumulator(UUID plotterMachineId, String machineName) {
            this.plotterMachineId = plotterMachineId;
            this.machineName = machineName;
        }

        private PlotterMachineOperationResult toResult() {
            return new PlotterMachineOperationResult(
                    plotterMachineId,
                    machineName,
                    plotterMachineId != null,
                    jobCount,
                    printedMeters.setScale(4, RoundingMode.HALF_UP),
                    wasteMeters.setScale(4, RoundingMode.HALF_UP),
                    externalRevenue.setScale(2, RoundingMode.HALF_UP)
            );
        }
    }
}
