package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.infrastructure.persistence.entity.SellerCommissionSettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para liquidaciones de comisión de vendedor.
 */
public interface SpringDataSellerCommissionSettlementRepository
        extends JpaRepository<SellerCommissionSettlementEntity, UUID> {

    Optional<SellerCommissionSettlementEntity> findByEmployeeIdAndPeriodStart(
            UUID employeeId,
            LocalDate periodStart
    );

    List<SellerCommissionSettlementEntity> findByPeriodStart(LocalDate periodStart);
}
