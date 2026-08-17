package com.magyen.platform.finance.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para {@link PayrollDeduction}.
 */
public interface PayrollDeductionRepository {

    PayrollDeduction save(PayrollDeduction deduction);

    Optional<PayrollDeduction> findById(UUID id);

    List<PayrollDeduction> findByEmployeeId(UUID employeeId);

    List<PayrollDeduction> findByEmployeeIdAndStatus(UUID employeeId, PayrollDeductionStatus status);
}
