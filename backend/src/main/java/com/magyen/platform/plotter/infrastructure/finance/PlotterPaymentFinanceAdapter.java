package com.magyen.platform.plotter.infrastructure.finance;

import com.magyen.platform.finance.application.dto.RegisterPlotterPaymentIncomeCommand;
import com.magyen.platform.finance.application.usecase.RegisterPlotterPaymentIncomeUseCase;
import com.magyen.platform.plotter.application.port.PlotterPaymentFinancePort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador que traduce el puerto financiero de Plotter al caso de uso de Finance.
 */
public class PlotterPaymentFinanceAdapter implements PlotterPaymentFinancePort {

    private final RegisterPlotterPaymentIncomeUseCase registerPlotterPaymentIncomeUseCase;

    public PlotterPaymentFinanceAdapter(
            RegisterPlotterPaymentIncomeUseCase registerPlotterPaymentIncomeUseCase
    ) {
        this.registerPlotterPaymentIncomeUseCase = Objects.requireNonNull(
                registerPlotterPaymentIncomeUseCase,
                "Register plotter payment income use case must not be null"
        );
    }

    @Override
    public void ensureIncomeForPlotterPayment(
            UUID plotterPaymentId,
            BigDecimal amount,
            LocalDate paymentDate,
            String observation
    ) {
        registerPlotterPaymentIncomeUseCase.execute(
                new RegisterPlotterPaymentIncomeCommand(
                        plotterPaymentId,
                        amount,
                        paymentDate,
                        observation
                )
        );
    }
}
