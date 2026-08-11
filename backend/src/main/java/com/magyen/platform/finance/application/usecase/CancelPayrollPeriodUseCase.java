package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CancelPayrollPeriodCommand;
import com.magyen.platform.finance.application.dto.CancelPayrollPeriodResult;
import com.magyen.platform.finance.domain.PayrollPeriod;
import com.magyen.platform.finance.domain.PayrollPeriodRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Cancela un período PENDING. No genera movimientos del ledger.
 */
public class CancelPayrollPeriodUseCase {

    private final PayrollPeriodRepository payrollPeriodRepository;

    public CancelPayrollPeriodUseCase(PayrollPeriodRepository payrollPeriodRepository) {
        this.payrollPeriodRepository = Objects.requireNonNull(
                payrollPeriodRepository,
                "Payroll period repository must not be null"
        );
    }

    @Transactional
    public CancelPayrollPeriodResult execute(CancelPayrollPeriodCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.periodId(), "Period id must not be null");

        PayrollPeriod period = payrollPeriodRepository
                .findById(command.periodId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll period not found: " + command.periodId()
                ));

        period.cancel();
        PayrollPeriod saved = payrollPeriodRepository.save(period);

        return new CancelPayrollPeriodResult(saved.getId(), saved.getStatus());
    }
}
