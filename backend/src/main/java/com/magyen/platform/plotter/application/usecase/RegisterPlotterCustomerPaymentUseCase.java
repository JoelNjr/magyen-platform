package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.RegisterPlotterCustomerPaymentCommand;
import com.magyen.platform.plotter.application.dto.RegisterPlotterCustomerPaymentResult;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterPaymentFinancePort;
import com.magyen.platform.plotter.application.usecase.PlotterCustomerAccountCalculator.Position;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import com.magyen.platform.plotter.domain.exception.PlotterRegistrationConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Registra dinero recibido en la cuenta del cliente externo.
 * <p>
 * El pago no liquida un trabajo. El ingreso de Finanzas nace aquí, no al registrar
 * el trabajo. La misma clave de registro no crea un segundo pago ni un segundo ingreso.
 * No se acepta un pago mayor que el saldo: la cuenta no queda a favor del cliente.
 */
public class RegisterPlotterCustomerPaymentUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterPaymentFinancePort plotterPaymentFinancePort;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;

    public RegisterPlotterCustomerPaymentUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterPaymentFinancePort plotterPaymentFinancePort,
            PlotterCommercialOrderPort plotterCommercialOrderPort
    ) {
        this.plotterJobRepository = Objects.requireNonNull(plotterJobRepository, "Plotter job repository must not be null");
        this.plotterPaymentRepository = Objects.requireNonNull(
                plotterPaymentRepository,
                "Plotter payment repository must not be null"
        );
        this.plotterPaymentFinancePort = Objects.requireNonNull(
                plotterPaymentFinancePort,
                "Plotter payment finance port must not be null"
        );
        this.plotterCommercialOrderPort = Objects.requireNonNull(
                plotterCommercialOrderPort,
                "Plotter commercial order port must not be null"
        );
    }

    @Transactional
    public RegisterPlotterCustomerPaymentResult execute(RegisterPlotterCustomerPaymentCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.customerId(), "Customer id must not be null");
        Objects.requireNonNull(command.amount(), "Amount must not be null");
        Objects.requireNonNull(command.paymentDate(), "Payment date must not be null");
        if (command.registrationKey() == null) {
            throw new PlotterDomainException("Plotter payment registration key is required");
        }
        plotterCommercialOrderPort.requireExternalPlotterCustomer(command.customerId());

        var existing = plotterPaymentRepository.findByRegistrationKey(command.registrationKey());
        if (existing.isPresent()) {
            return replay(existing.get(), command);
        }

        Position position = PlotterCustomerAccountCalculator.positionOf(
                command.customerId(),
                plotterJobRepository.findAll(),
                plotterPaymentRepository.findAll()
        );
        PlotterPayment payment = PlotterPayment.createForCustomer(
                command.customerId(),
                command.amount(),
                command.paymentDate(),
                command.observations(),
                command.registrationKey()
        );
        if (payment.getAmount().compareTo(position.outstandingAmount()) > 0) {
            throw new PlotterDomainException(
                    "Plotter payment amount exceeds the customer account outstanding balance. Outstanding balance: "
                            + position.outstandingAmount()
                            + ", requested amount: "
                            + payment.getAmount()
            );
        }

        PlotterPayment saved = saveIdempotent(payment);
        if (!saved.getId().equals(payment.getId())) {
            return replay(saved, command);
        }
        plotterPaymentFinancePort.ensureIncomeForPlotterPayment(
                saved.getId(),
                saved.getAmount(),
                saved.getPaymentDate(),
                saved.getObservations()
        );
        BigDecimal paymentTotal = position.paymentTotal().add(saved.getAmount());
        BigDecimal outstanding = position.debitTotal().subtract(paymentTotal);
        return new RegisterPlotterCustomerPaymentResult(
                saved.getId(),
                saved.getCustomerId(),
                saved.getAmount(),
                saved.getPaymentDate(),
                saved.getObservations(),
                position.debitTotal(),
                PlotterCustomerAccountCalculator.money(paymentTotal),
                PlotterCustomerAccountCalculator.money(outstanding),
                false
        );
    }

    private RegisterPlotterCustomerPaymentResult replay(
            PlotterPayment existing,
            RegisterPlotterCustomerPaymentCommand command
    ) {
        if (!command.customerId().equals(existing.getCustomerId())) {
            throw new PlotterRegistrationConflictException(
                    "Registration key already belongs to another customer account"
            );
        }
        if (existing.getAmount().compareTo(command.amount()) != 0) {
            throw new PlotterRegistrationConflictException(
                    "Registration key already belongs to a payment with a different amount"
            );
        }
        plotterPaymentFinancePort.ensureIncomeForPlotterPayment(
                existing.getId(),
                existing.getAmount(),
                existing.getPaymentDate(),
                existing.getObservations()
        );
        Position position = PlotterCustomerAccountCalculator.positionOf(
                command.customerId(),
                plotterJobRepository.findAll(),
                plotterPaymentRepository.findAll()
        );
        return new RegisterPlotterCustomerPaymentResult(
                existing.getId(),
                existing.getCustomerId(),
                existing.getAmount(),
                existing.getPaymentDate(),
                existing.getObservations(),
                position.debitTotal(),
                position.paymentTotal(),
                position.outstandingAmount(),
                true
        );
    }

    private PlotterPayment saveIdempotent(PlotterPayment payment) {
        try {
            return plotterPaymentRepository.save(payment);
        } catch (DataIntegrityViolationException exception) {
            if (payment.getRegistrationKey() == null) {
                throw exception;
            }
            return plotterPaymentRepository.findByRegistrationKey(payment.getRegistrationKey())
                    .orElseThrow(() -> exception);
        }
    }
}
