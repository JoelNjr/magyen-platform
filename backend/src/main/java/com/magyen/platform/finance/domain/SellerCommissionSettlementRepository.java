package com.magyen.platform.finance.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia de la liquidación pagada de comisión de vendedor.
 */
public interface SellerCommissionSettlementRepository {

    SellerCommissionSettlement save(SellerCommissionSettlement settlement);

    Optional<SellerCommissionSettlement> findById(UUID id);

    Optional<SellerCommissionSettlement> findByEmployeeIdAndPeriodStart(UUID employeeId, LocalDate periodStart);

    List<SellerCommissionSettlement> findByPeriodStart(LocalDate periodStart);
}
