package com.magyen.platform.plotter.infrastructure.finance;

import com.magyen.platform.finance.application.dto.EnsurePlotterInternalServiceLedgerCommand;
import com.magyen.platform.finance.application.usecase.EnsurePlotterInternalServiceLedgerUseCase;
import com.magyen.platform.plotter.application.port.PlotterInternalServiceFinancePort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Plotter → Finance para el servicio interno Magyen.
 */
public class PlotterInternalServiceFinanceAdapter implements PlotterInternalServiceFinancePort {

    private final EnsurePlotterInternalServiceLedgerUseCase ensurePlotterInternalServiceLedgerUseCase;

    public PlotterInternalServiceFinanceAdapter(
            EnsurePlotterInternalServiceLedgerUseCase ensurePlotterInternalServiceLedgerUseCase
    ) {
        this.ensurePlotterInternalServiceLedgerUseCase = Objects.requireNonNull(
                ensurePlotterInternalServiceLedgerUseCase,
                "Ensure plotter internal service ledger use case must not be null"
        );
    }

    @Override
    public void ensureInternalServiceLedger(
            UUID plotterJobId,
            BigDecimal amount,
            LocalDate transactionDate,
            String observation
    ) {
        ensurePlotterInternalServiceLedgerUseCase.execute(new EnsurePlotterInternalServiceLedgerCommand(
                plotterJobId,
                amount,
                transactionDate,
                observation
        ));
    }
}
