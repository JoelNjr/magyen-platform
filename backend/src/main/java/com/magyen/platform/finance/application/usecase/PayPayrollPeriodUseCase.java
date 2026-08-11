package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.PayPayrollPeriodCommand;
import com.magyen.platform.finance.application.dto.PayPayrollPeriodResult;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.PayrollPeriod;
import com.magyen.platform.finance.domain.PayrollPeriodRepository;
import com.magyen.platform.finance.domain.PayrollPeriodStatus;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.domain.exception.PayrollPeriodAlreadyPaidException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Paga un período PENDING creando exactamente un {@link FinancialTransaction} EXPENSE.
 * <p>
 * Atomicidad: el movimiento del ledger y el estado PAID se persisten en la misma transacción.
 * El monto proviene del snapshot del período, no de la compensación actual del empleado.
 */
public class PayPayrollPeriodUseCase {

    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final FinancialTransactionRepository financialTransactionRepository;

    public PayPayrollPeriodUseCase(
            PayrollPeriodRepository payrollPeriodRepository,
            PayrollEmployeeRepository payrollEmployeeRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.payrollPeriodRepository = Objects.requireNonNull(
                payrollPeriodRepository,
                "Payroll period repository must not be null"
        );
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    @Transactional
    public PayPayrollPeriodResult execute(PayPayrollPeriodCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.periodId(), "Period id must not be null");

        PayrollPeriod period = payrollPeriodRepository
                .findById(command.periodId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll period not found: " + command.periodId()
                ));

        if (period.getStatus() == PayrollPeriodStatus.PAID) {
            throw new PayrollPeriodAlreadyPaidException();
        }
        if (period.getStatus() != PayrollPeriodStatus.PENDING) {
            throw new FinanceDomainException(
                    "Only PENDING payroll periods can be paid. Current status: " + period.getStatus()
            );
        }

        PayrollEmployee employee = payrollEmployeeRepository
                .findById(period.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll employee not found: " + period.getEmployeeId()
                ));

        LocalDateTime paidAt = command.paidAt() == null ? LocalDateTime.now() : command.paidAt();
        String description = "Pago de nómina - " + employee.getDisplayName();

        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                period.getAmountSnapshot(),
                paidAt.toLocalDate(),
                FinancialCategory.PAYROLL.name(),
                description,
                command.observation(),
                FinancialTransactionSourceType.PAYROLL,
                period.getId()
        );

        FinancialTransaction savedTransaction = financialTransactionRepository.save(transaction);
        period.markPaid(savedTransaction.getId(), paidAt, paidAt.toLocalDate());
        PayrollPeriod savedPeriod = payrollPeriodRepository.save(period);

        return new PayPayrollPeriodResult(
                savedPeriod.getId(),
                savedPeriod.getEmployeeId(),
                savedPeriod.getPeriodStart(),
                savedPeriod.getPeriodEnd(),
                savedPeriod.getAmountSnapshot().getValue(),
                savedPeriod.getStatus(),
                savedPeriod.getActualPaymentDate(),
                savedPeriod.getPaidAt(),
                savedPeriod.getFinancialTransactionId(),
                savedTransaction.getAmount().getValue(),
                savedTransaction.getCategory()
        );
    }
}
