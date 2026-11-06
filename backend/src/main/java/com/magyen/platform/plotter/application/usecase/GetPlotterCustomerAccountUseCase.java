package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterCustomerAccountResult;
import com.magyen.platform.plotter.application.dto.GetPlotterCustomerAccountsResult;
import com.magyen.platform.plotter.application.dto.PlotterAccountMovement;
import com.magyen.platform.plotter.application.dto.PlotterCustomerAccountSummary;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.usecase.PlotterCustomerAccountCalculator.Position;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterMachineRepository;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lee la cuenta corriente. No crea débitos ni ingresos.
 * Cada trabajo externo aporta un débito y cada pago recibido aporta un abono.
 */
public class GetPlotterCustomerAccountUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;
    private final PlotterMachineRepository plotterMachineRepository;

    public GetPlotterCustomerAccountUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterCommercialOrderPort plotterCommercialOrderPort,
            PlotterMachineRepository plotterMachineRepository
    ) {
        this.plotterJobRepository = Objects.requireNonNull(plotterJobRepository, "Plotter job repository must not be null");
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

    public GetPlotterCustomerAccountsResult list(UUID customerId) {
        Map<UUID, Position> positions = PlotterCustomerAccountCalculator.calculate(
                plotterJobRepository.findAll(),
                plotterPaymentRepository.findAll()
        );
        List<PlotterCustomerAccountSummary> customers = positions.values().stream()
                .filter(position -> customerId == null || customerId.equals(position.customerId()))
                .filter(position -> !position.jobs().isEmpty() || !position.payments().isEmpty())
                .map(position -> new PlotterCustomerAccountSummary(
                        position.customerId(),
                        customerName(position.customerId()),
                        position.debitTotal(),
                        position.paymentTotal(),
                        position.outstandingAmount(),
                        position.jobs().size()
                ))
                .sorted(Comparator
                        .comparing(PlotterCustomerAccountSummary::outstandingAmount).reversed()
                        .thenComparing(summary -> summary.customerName() == null ? "" : summary.customerName()))
                .toList();
        return new GetPlotterCustomerAccountsResult(customers);
    }

    public GetPlotterCustomerAccountResult detail(UUID customerId) {
        Objects.requireNonNull(customerId, "Customer id must not be null");
        plotterCommercialOrderPort.requireExternalPlotterCustomer(customerId);
        Position position = PlotterCustomerAccountCalculator.positionOf(
                customerId,
                plotterJobRepository.findAll(),
                plotterPaymentRepository.findAll()
        );
        if (position.jobs().isEmpty() && position.payments().isEmpty()) {
            throw new PlotterDomainException("External plotter customer has no account activity: " + customerId);
        }
        Map<UUID, String> machineNames = plotterMachineRepository.findAll().stream()
                .collect(Collectors.toMap(machine -> machine.getId(), machine -> machine.getName()));
        List<PlotterAccountMovement> movements = new ArrayList<>();
        for (PlotterJob job : position.jobs()) {
            String machineName = job.getPlotterMachineId() == null
                    ? null
                    : machineNames.get(job.getPlotterMachineId());
            movements.add(new PlotterAccountMovement(
                    "JOB_DEBIT",
                    job.getCreationDate(),
                    job.getId(),
                    null,
                    job.getObservations(),
                    job.getPrintedMeters(),
                    job.getPlotterMachineId(),
                    machineName,
                    job.getTotalAmount()
            ));
        }
        for (PlotterPayment payment : position.payments()) {
            movements.add(new PlotterAccountMovement(
                    "CUSTOMER_PAYMENT",
                    payment.getPaymentDate(),
                    payment.getPlotterJobId(),
                    payment.getId(),
                    payment.getObservations(),
                    null,
                    null,
                    null,
                    payment.getAmount()
            ));
        }
        movements.sort(Comparator
                .comparing(PlotterAccountMovement::movementDate)
                .thenComparing(movement -> "JOB_DEBIT".equals(movement.movementType()) ? 0 : 1)
                .thenComparing(movement -> movement.plotterJobId() == null
                        ? movement.paymentId().toString()
                        : movement.plotterJobId().toString()));
        return new GetPlotterCustomerAccountResult(
                customerId,
                customerName(customerId),
                position.debitTotal(),
                position.paymentTotal(),
                position.outstandingAmount(),
                List.copyOf(movements)
        );
    }

    private String customerName(UUID customerId) {
        return plotterCommercialOrderPort.findCustomerName(customerId).orElse(null);
    }
}
