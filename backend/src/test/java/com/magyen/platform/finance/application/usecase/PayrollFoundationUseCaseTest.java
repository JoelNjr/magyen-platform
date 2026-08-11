package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.GeneratePayrollPeriodsCommand;
import com.magyen.platform.finance.application.dto.GeneratePayrollPeriodsResult;
import com.magyen.platform.finance.application.dto.GetPayrollPeriodResult;
import com.magyen.platform.finance.application.dto.PayPayrollPeriodCommand;
import com.magyen.platform.finance.application.dto.PayPayrollPeriodResult;
import com.magyen.platform.finance.application.dto.UpdatePayrollEmployeeCompensationCommand;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.PayrollBusinessDayAdjuster;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollPeriod;
import com.magyen.platform.finance.domain.PayrollPeriodRepository;
import com.magyen.platform.finance.domain.PayrollPeriodStatus;
import com.magyen.platform.finance.domain.exception.PayrollPeriodAlreadyPaidException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PayrollFoundationUseCaseTest {

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private UpdatePayrollEmployeeCompensationUseCase updatePayrollEmployeeCompensationUseCase;

    @Autowired
    private DeactivatePayrollEmployeeUseCase deactivatePayrollEmployeeUseCase;

    @Autowired
    private GeneratePayrollPeriodsUseCase generatePayrollPeriodsUseCase;

    @Autowired
    private PayPayrollPeriodUseCase payPayrollPeriodUseCase;

    @Autowired
    private PayrollPeriodRepository payrollPeriodRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void createsFixedPayrollEmployee() {
        CreatePayrollEmployeeResult employee = createFixedEmployee(
                "Ana-" + suffix(),
                "1500000.00",
                LocalDate.of(2026, 8, 3)
        );

        assertEquals(PayrollCompensationType.FIXED_PAYROLL, employee.compensationType());
        assertEquals(new BigDecimal("1500000.00"), employee.fixedAmount());
        assertTrue(employee.active());
    }

    @Test
    void createsProductionBasedEmployee() {
        CreatePayrollEmployeeResult employee = createPayrollEmployeeUseCase.execute(
                new CreatePayrollEmployeeCommand(
                        "Operario-" + suffix(),
                        PayrollCompensationType.PRODUCTION_BASED,
                        null,
                        null,
                        null
                )
        );

        assertEquals(PayrollCompensationType.PRODUCTION_BASED, employee.compensationType());
        assertEquals(null, employee.fixedAmount());
        assertEquals(null, employee.frequency());
    }

    @Test
    void productionBasedExcludedFromGenerate() {
        CreatePayrollEmployeeResult employee = createPayrollEmployeeUseCase.execute(
                new CreatePayrollEmployeeCommand(
                        "Operario-" + suffix(),
                        PayrollCompensationType.PRODUCTION_BASED,
                        null,
                        null,
                        null
                )
        );

        GeneratePayrollPeriodsResult result = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        assertTrue(result.skippedProductionBased() >= 1);
        assertTrue(result.createdPeriods().stream()
                .noneMatch(period -> period.employeeId().equals(employee.employeeId())));
    }

    @Test
    void generatesPayrollPeriodWithBusinessDayAdjustment() {
        CreatePayrollEmployeeResult employee = createFixedEmployee(
                "Luis-" + suffix(),
                "1500000.00",
                LocalDate.of(2026, 8, 2)
        );

        GeneratePayrollPeriodsResult result = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        GetPayrollPeriodResult saturdayEnding = result.createdPeriods().stream()
                .filter(period -> period.employeeId().equals(employee.employeeId()))
                .filter(period -> period.periodEnd().equals(LocalDate.of(2026, 8, 15)))
                .findFirst()
                .orElseThrow();

        assertEquals(LocalDate.of(2026, 8, 14), saturdayEnding.expectedPaymentDate());
        assertEquals(
                LocalDate.of(2026, 8, 14),
                PayrollBusinessDayAdjuster.adjustToBusinessDay(LocalDate.of(2026, 8, 15))
        );
    }

    @Test
    void generateIsIdempotent() {
        CreatePayrollEmployeeResult employee = createFixedEmployee(
                "Idem-" + suffix(),
                "1500000.00",
                LocalDate.of(2026, 8, 3)
        );
        Set<UUID> owned = Set.of(employee.employeeId());

        GeneratePayrollPeriodsResult first = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );
        int createdFirst = countCreatedFor(first, owned);
        assertTrue(createdFirst >= 1);

        GeneratePayrollPeriodsResult second = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        assertEquals(0, countCreatedFor(second, owned));
        assertTrue(second.alreadyExisting() >= createdFirst);
    }

    @Test
    void inactiveEmployeeSkippedFromGenerate() {
        CreatePayrollEmployeeResult employee = createFixedEmployee(
                "Inactivo-" + suffix(),
                "1500000.00",
                LocalDate.of(2026, 8, 3)
        );
        deactivatePayrollEmployeeUseCase.execute(
                new DeactivatePayrollEmployeeCommand(employee.employeeId())
        );

        GeneratePayrollPeriodsResult result = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        assertTrue(result.skippedInactive() >= 1);
        assertTrue(result.createdPeriods().stream()
                .noneMatch(period -> period.employeeId().equals(employee.employeeId())));
    }

    @Test
    void amountSnapshotPreservedWhenCompensationChanges() {
        CreatePayrollEmployeeResult jose = createFixedEmployee(
                "José-" + suffix(),
                "1500000.00",
                LocalDate.of(2026, 8, 3)
        );

        GeneratePayrollPeriodsResult august = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        List<GetPayrollPeriodResult> joseAugust = august.createdPeriods().stream()
                .filter(period -> period.employeeId().equals(jose.employeeId()))
                .toList();
        assertTrue(joseAugust.size() >= 1);
        assertTrue(joseAugust.stream()
                .allMatch(period -> period.amountSnapshot().compareTo(new BigDecimal("1500000.00")) == 0));

        GetPayrollPeriodResult firstPeriod = joseAugust.stream()
                .sorted((left, right) -> left.periodStart().compareTo(right.periodStart()))
                .findFirst()
                .orElseThrow();

        updatePayrollEmployeeCompensationUseCase.execute(
                new UpdatePayrollEmployeeCompensationCommand(
                        jose.employeeId(),
                        jose.displayName(),
                        new BigDecimal("1700000.00"),
                        LocalDate.of(2026, 8, 3),
                        null
                )
        );

        GeneratePayrollPeriodsResult augustAgain = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );
        assertEquals(0, countCreatedFor(augustAgain, Set.of(jose.employeeId())));

        PayrollPeriod persistedFirst = payrollPeriodRepository
                .findById(firstPeriod.periodId())
                .orElseThrow();
        assertEquals(new BigDecimal("1500000.00"), persistedFirst.getAmountSnapshot().getValue());

        GeneratePayrollPeriodsResult september = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30)
                )
        );

        List<GetPayrollPeriodResult> joseSeptember = september.createdPeriods().stream()
                .filter(period -> period.employeeId().equals(jose.employeeId()))
                .toList();
        assertTrue(joseSeptember.size() >= 1);
        assertTrue(joseSeptember.stream()
                .allMatch(period -> period.amountSnapshot().compareTo(new BigDecimal("1700000.00")) == 0));
    }

    @Test
    void generateDoesNotCreatePayrollLedgerTransaction() {
        CreatePayrollEmployeeResult employee = createFixedEmployee(
                "LedgerFree-" + suffix(),
                "1500000.00",
                LocalDate.of(2026, 8, 3)
        );

        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        GeneratePayrollPeriodsResult result = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        List<UUID> periodIds = result.createdPeriods().stream()
                .filter(period -> period.employeeId().equals(employee.employeeId()))
                .map(GetPayrollPeriodResult::periodId)
                .toList();
        assertTrue(periodIds.size() >= 1);
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());

        for (UUID periodId : periodIds) {
            assertTrue(
                    financialTransactionRepository
                            .findBySourceTypeAndSourceId(FinancialTransactionSourceType.PAYROLL, periodId)
                            .isEmpty()
            );
        }
    }

    @Test
    void payCreatesExactlyOneExpensePayrollTransactionFromSnapshot() {
        CreatePayrollEmployeeResult employee = createFixedEmployee(
                "Pago-" + suffix(),
                "1500000.00",
                LocalDate.of(2026, 8, 3)
        );

        GeneratePayrollPeriodsResult generated = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );
        GetPayrollPeriodResult period = firstOwnedPeriod(generated, employee.employeeId());

        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        PayPayrollPeriodResult paid = payPayrollPeriodUseCase.execute(
                new PayPayrollPeriodCommand(period.periodId())
        );

        assertEquals(PayrollPeriodStatus.PAID, paid.status());
        assertEquals(new BigDecimal("1500000.00"), paid.amountSnapshot());
        assertEquals(new BigDecimal("1500000.00"), paid.transactionAmount());
        assertEquals("PAYROLL", paid.transactionCategory());
        assertEquals(transactionsBefore + 1, financialTransactionRepository.findAllNewestFirst().size());

        FinancialTransaction transaction = financialTransactionRepository
                .findById(paid.financialTransactionId())
                .orElseThrow();
        assertEquals(FinancialTransactionType.EXPENSE, transaction.getType());
        assertEquals(FinancialTransactionSourceType.PAYROLL, transaction.getSourceType());
        assertEquals(period.periodId(), transaction.getSourceId());
        assertEquals(new BigDecimal("1500000.00"), transaction.getAmount().getValue());
        assertEquals("PAYROLL", transaction.getCategory());
        assertTrue(transaction.getDescription().contains(employee.displayName()));
    }

    @Test
    void payTwiceThrowsAlreadyPaidAndCreatesNoAdditionalTransaction() {
        CreatePayrollEmployeeResult employee = createFixedEmployee(
                "DoblePago-" + suffix(),
                "1500000.00",
                LocalDate.of(2026, 8, 3)
        );

        GeneratePayrollPeriodsResult generated = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );
        GetPayrollPeriodResult period = firstOwnedPeriod(generated, employee.employeeId());

        payPayrollPeriodUseCase.execute(new PayPayrollPeriodCommand(period.periodId()));
        long transactionsAfterFirstPay = financialTransactionRepository.findAllNewestFirst().size();

        assertThrows(
                PayrollPeriodAlreadyPaidException.class,
                () -> payPayrollPeriodUseCase.execute(new PayPayrollPeriodCommand(period.periodId()))
        );
        assertEquals(transactionsAfterFirstPay, financialTransactionRepository.findAllNewestFirst().size());
    }

    @Test
    void twoPeriodsProduceTwoPayrollExpenses() {
        CreatePayrollEmployeeResult employee = createFixedEmployee(
                "DosPeriodos-" + suffix(),
                "1500000.00",
                LocalDate.of(2026, 8, 3)
        );

        GeneratePayrollPeriodsResult generated = generatePayrollPeriodsUseCase.execute(
                new GeneratePayrollPeriodsCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        List<GetPayrollPeriodResult> periods = generated.createdPeriods().stream()
                .filter(period -> period.employeeId().equals(employee.employeeId()))
                .toList();
        assertTrue(periods.size() >= 2);

        GetPayrollPeriodResult first = periods.get(0);
        GetPayrollPeriodResult second = periods.get(1);

        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        payPayrollPeriodUseCase.execute(new PayPayrollPeriodCommand(first.periodId()));
        payPayrollPeriodUseCase.execute(new PayPayrollPeriodCommand(second.periodId()));

        assertEquals(transactionsBefore + 2, financialTransactionRepository.findAllNewestFirst().size());

        Set<UUID> sourceIds = financialTransactionRepository.findAllNewestFirst().stream()
                .filter(tx -> tx.getSourceType() == FinancialTransactionSourceType.PAYROLL)
                .filter(tx -> tx.getSourceId() != null)
                .filter(tx -> tx.getSourceId().equals(first.periodId())
                        || tx.getSourceId().equals(second.periodId()))
                .map(FinancialTransaction::getSourceId)
                .collect(Collectors.toSet());
        assertEquals(Set.of(first.periodId(), second.periodId()), sourceIds);
    }

    private CreatePayrollEmployeeResult createFixedEmployee(
            String displayName,
            String amount,
            LocalDate effectiveFrom
    ) {
        return createPayrollEmployeeUseCase.execute(
                new CreatePayrollEmployeeCommand(
                        displayName,
                        PayrollCompensationType.FIXED_PAYROLL,
                        new BigDecimal(amount),
                        effectiveFrom,
                        null
                )
        );
    }

    private static int countCreatedFor(GeneratePayrollPeriodsResult result, Set<UUID> employeeIds) {
        return (int) result.createdPeriods().stream()
                .filter(period -> employeeIds.contains(period.employeeId()))
                .count();
    }

    private static GetPayrollPeriodResult firstOwnedPeriod(
            GeneratePayrollPeriodsResult result,
            UUID employeeId
    ) {
        return result.createdPeriods().stream()
                .filter(period -> period.employeeId().equals(employeeId))
                .sorted((left, right) -> left.periodStart().compareTo(right.periodStart()))
                .findFirst()
                .orElseThrow();
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
