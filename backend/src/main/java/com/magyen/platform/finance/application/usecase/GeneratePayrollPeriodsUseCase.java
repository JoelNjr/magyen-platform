package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GeneratePayrollPeriodsCommand;
import com.magyen.platform.finance.application.dto.GeneratePayrollPeriodsResult;
import com.magyen.platform.finance.application.dto.GetPayrollPeriodResult;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.PayrollPeriod;
import com.magyen.platform.finance.domain.PayrollPeriodRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Genera de forma controlada e idempotente períodos PENDING para empleados fijos activos.
 * <p>
 * No crea {@code FinancialTransaction}. El monto se congela como snapshot al generarse.
 */
public class GeneratePayrollPeriodsUseCase {

    private static final int MAX_RANGE_DAYS_INCLUSIVE = 366;

    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;

    public GeneratePayrollPeriodsUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            PayrollPeriodRepository payrollPeriodRepository
    ) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
        this.payrollPeriodRepository = Objects.requireNonNull(
                payrollPeriodRepository,
                "Payroll period repository must not be null"
        );
    }

    @Transactional
    public GeneratePayrollPeriodsResult execute(GeneratePayrollPeriodsCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        LocalDate fromDate = command.fromDate();
        LocalDate toDate = command.toDate();

        List<PayrollEmployee> employees = payrollEmployeeRepository.findAll();
        Set<String> existingKeys = loadExistingPeriodKeys();

        int created = 0;
        int alreadyExisting = 0;
        int skippedInactive = 0;
        int skippedProductionBased = 0;
        int skippedOutsideValidity = 0;
        List<GetPayrollPeriodResult> createdPeriods = new ArrayList<>();

        for (PayrollEmployee employee : employees) {
            if (!employee.isActive()) {
                skippedInactive++;
                continue;
            }

            if (employee.getCompensationType() != PayrollCompensationType.FIXED_PAYROLL) {
                skippedProductionBased++;
                continue;
            }

            List<PayrollEmployee.ResolvedPayrollPeriodWindow> windows =
                    employee.resolveBiweeklyPeriodWindows(fromDate, toDate);
            if (windows.isEmpty()) {
                skippedOutsideValidity++;
                continue;
            }

            for (PayrollEmployee.ResolvedPayrollPeriodWindow window : windows) {
                String key = periodKey(employee.getId(), window.periodStart());
                if (existingKeys.contains(key)) {
                    alreadyExisting++;
                    continue;
                }

                PayrollPeriod period = PayrollPeriod.createPending(
                        employee.getId(),
                        window.periodStart(),
                        window.periodEnd(),
                        window.expectedPaymentDate(),
                        employee.getFixedAmount()
                );

                try {
                    PayrollPeriod saved = payrollPeriodRepository.save(period);
                    existingKeys.add(key);
                    created++;
                    createdPeriods.add(
                            PayrollPeriodReadMapper.toGetResult(saved, employee.getDisplayName())
                    );
                } catch (DataIntegrityViolationException exception) {
                    alreadyExisting++;
                    existingKeys.add(key);
                }
            }
        }

        return new GeneratePayrollPeriodsResult(
                fromDate,
                toDate,
                employees.size(),
                created,
                alreadyExisting,
                skippedInactive,
                skippedProductionBased,
                skippedOutsideValidity,
                List.copyOf(createdPeriods)
        );
    }

    private Set<String> loadExistingPeriodKeys() {
        Set<String> keys = new HashSet<>();
        for (PayrollPeriod period : payrollPeriodRepository.findAllNewestFirst()) {
            keys.add(periodKey(period.getEmployeeId(), period.getPeriodStart()));
        }
        return keys;
    }

    private static String periodKey(java.util.UUID employeeId, LocalDate periodStart) {
        return employeeId + "|" + periodStart;
    }

    private void validateCommand(GeneratePayrollPeriodsCommand command) {
        if (command.fromDate() == null) {
            throw new FinanceDomainException("From date must not be null");
        }
        if (command.toDate() == null) {
            throw new FinanceDomainException("To date must not be null");
        }
        if (command.toDate().isBefore(command.fromDate())) {
            throw new FinanceDomainException("From date must not be after to date");
        }

        long inclusiveDays = ChronoUnit.DAYS.between(command.fromDate(), command.toDate()) + 1;
        if (inclusiveDays > MAX_RANGE_DAYS_INCLUSIVE) {
            throw new FinanceDomainException(
                    "Generation range must not exceed " + MAX_RANGE_DAYS_INCLUSIVE + " days"
            );
        }
    }
}
