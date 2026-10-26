package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsResult;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.PlotterMachineRepository;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso que lista los trabajos de plotter.
 */
public class GetPlotterJobsUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;
    private final PlotterMachineRepository plotterMachineRepository;

    public GetPlotterJobsUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterCommercialOrderPort plotterCommercialOrderPort,
            PlotterMachineRepository plotterMachineRepository
    ) {
        this.plotterJobRepository = Objects.requireNonNull(
                plotterJobRepository,
                "Plotter job repository must not be null"
        );
        this.plotterPaymentRepository = Objects.requireNonNull(
                plotterPaymentRepository,
                "Plotter payment repository must not be null"
        );
        this.plotterCommercialOrderPort = Objects.requireNonNull(
                plotterCommercialOrderPort,
                "Plotter commercial order port must not be null"
        );
        this.plotterMachineRepository = Objects.requireNonNull(
                plotterMachineRepository,
                "Plotter machine repository must not be null"
        );
    }

    public GetPlotterJobsResult execute() {
        return execute(new GetPlotterJobsQuery(null, null));
    }

    public GetPlotterJobsResult execute(GetPlotterJobsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        if (query.plotterMachineId() != null && query.unassignedMachineOnly()) {
            throw new PlotterDomainException("Choose either a Plotter machine or unassigned jobs");
        }
        validateRange(query.fromDate(), query.toDate());

        Map<UUID, String> machineNames = plotterMachineRepository.findAll().stream()
                .collect(Collectors.toMap(machine -> machine.getId(), machine -> machine.getName()));
        List<GetPlotterJobResult> jobs = plotterJobRepository.findAll().stream()
                .filter(job -> matchesCustomer(job, query.customerId()))
                .filter(job -> matchesJobType(job, query.jobType()))
                .filter(job -> matchesMachine(job, query))
                .filter(job -> inRange(job.getCreationDate(), query.fromDate(), query.toDate()))
                .map(job -> toResult(job, machineNames))
                .toList();
        return new GetPlotterJobsResult(List.copyOf(jobs));
    }

    private static void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return;
        }
        if (fromDate == null || toDate == null) {
            throw new PlotterDomainException("Both fromDate and toDate must be provided together");
        }
        if (fromDate.isAfter(toDate)) {
            throw new PlotterDomainException("From date must not be after to date");
        }
    }

    private static boolean matchesCustomer(PlotterJob job, UUID customerId) {
        if (customerId == null) {
            return true;
        }
        return customerId.equals(job.getCustomerId());
    }

    private static boolean matchesMachine(PlotterJob job, GetPlotterJobsQuery query) {
        if (query.unassignedMachineOnly()) {
            return job.getPlotterMachineId() == null;
        }
        if (query.plotterMachineId() == null) {
            return true;
        }
        return query.plotterMachineId().equals(job.getPlotterMachineId());
    }

    private static boolean matchesJobType(PlotterJob job, PlotterJobType jobType) {
        if (jobType == null) {
            return true;
        }
        return job.getJobType() == jobType;
    }

    private static boolean inRange(LocalDate businessDate, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            return true;
        }
        return businessDate != null && !businessDate.isBefore(fromDate) && !businessDate.isAfter(toDate);
    }

    private GetPlotterJobResult toResult(PlotterJob job, Map<UUID, String> machineNames) {
        List<PlotterPayment> payments =
                plotterPaymentRepository.findByPlotterJobIdNewestFirst(job.getId());
        BigDecimal paidAmount = PlotterPaymentBalanceCalculator.sumPaid(payments);
        BigDecimal outstandingAmount =
                PlotterPaymentBalanceCalculator.collectableOutstanding(job, paidAmount);
        String machineName = job.getPlotterMachineId() == null
                ? null
                : machineNames.get(job.getPlotterMachineId());
        return PlotterJobReadMapper.toGetResult(
                job,
                paidAmount,
                outstandingAmount,
                plotterCommercialOrderPort,
                machineName
        );
    }
}
