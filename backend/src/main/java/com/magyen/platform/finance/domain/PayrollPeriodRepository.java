package com.magyen.platform.finance.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para {@link PayrollPeriod}.
 */
public interface PayrollPeriodRepository {

    PayrollPeriod save(PayrollPeriod payrollPeriod);

    Optional<PayrollPeriod> findById(UUID id);

    Optional<PayrollPeriod> findByEmployeeIdAndPeriodStart(UUID employeeId, LocalDate periodStart);

    List<PayrollPeriod> findAllNewestFirst();

    List<PayrollPeriod> findByExpectedPaymentDateBetween(LocalDate fromDate, LocalDate toDate);

    List<PayrollPeriod> findByStatus(PayrollPeriodStatus status);
}
