package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentCommand;
import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentResult;
import com.magyen.platform.plotter.application.port.PlotterPaymentFinancePort;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Registra un pago de cliente sobre un trabajo de Plotter y sincroniza el ledger Finance.
 * <p>
 * No cambia el estado del trabajo. El saldo pendiente se calcula desde pagos de Plotter,
 * no desde movimientos del ledger.
 */
public class RegisterPlotterPaymentUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterPaymentFinancePort plotterPaymentFinancePort;

    public RegisterPlotterPaymentUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterPaymentFinancePort plotterPaymentFinancePort
    ) {
        this.plotterJobRepository = Objects.requireNonNull(
                plotterJobRepository,
                "Plotter job repository must not be null"
        );
        this.plotterPaymentRepository = Objects.requireNonNull(
                plotterPaymentRepository,
                "Plotter payment repository must not be null"
        );
        this.plotterPaymentFinancePort = Objects.requireNonNull(
                plotterPaymentFinancePort,
                "Plotter payment finance port must not be null"
        );
    }

    @Transactional
    public RegisterPlotterPaymentResult execute(RegisterPlotterPaymentCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.plotterJobId(), "Plotter job id must not be null");
        Objects.requireNonNull(command.amount(), "Amount must not be null");
        Objects.requireNonNull(command.paymentDate(), "Payment date must not be null");

        PlotterJob job = plotterJobRepository.findById(command.plotterJobId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plotter job not found: " + command.plotterJobId()
                ));

        List<PlotterPayment> existingPayments =
                plotterPaymentRepository.findByPlotterJobIdNewestFirst(job.getId());
        BigDecimal paidAmount = PlotterPaymentBalanceCalculator.sumPaid(existingPayments);
        BigDecimal outstandingAmount =
                PlotterPaymentBalanceCalculator.outstanding(job.getTotalAmount(), paidAmount);

        PlotterPayment payment = PlotterPayment.create(
                job.getId(),
                command.amount(),
                command.paymentDate(),
                command.observations()
        );

        if (payment.getAmount().compareTo(outstandingAmount) > 0) {
            throw new PlotterDomainException(
                    "Plotter payment amount exceeds outstanding balance. Outstanding balance: "
                            + outstandingAmount
                            + ", requested amount: "
                            + payment.getAmount()
            );
        }

        PlotterPayment savedPayment = plotterPaymentRepository.save(payment);
        plotterPaymentFinancePort.ensureIncomeForPlotterPayment(
                savedPayment.getId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentDate(),
                savedPayment.getObservations()
        );

        BigDecimal newPaidAmount = paidAmount.add(savedPayment.getAmount());
        BigDecimal newOutstanding =
                PlotterPaymentBalanceCalculator.outstanding(job.getTotalAmount(), newPaidAmount);

        return new RegisterPlotterPaymentResult(
                savedPayment.getId(),
                savedPayment.getPlotterJobId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentDate(),
                savedPayment.getObservations(),
                newPaidAmount,
                newOutstanding
        );
    }
}
