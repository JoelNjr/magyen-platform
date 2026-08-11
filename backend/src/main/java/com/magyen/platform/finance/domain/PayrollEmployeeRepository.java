package com.magyen.platform.finance.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para {@link PayrollEmployee}.
 */
public interface PayrollEmployeeRepository {

    PayrollEmployee save(PayrollEmployee employee);

    Optional<PayrollEmployee> findById(UUID id);

    List<PayrollEmployee> findAll();

    List<PayrollEmployee> findByActive(boolean active);
}
