package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.PayrollDeductionStatus;
import com.magyen.platform.finance.infrastructure.persistence.entity.PayrollDeductionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link PayrollDeductionEntity}.
 */
public interface SpringDataPayrollDeductionRepository extends JpaRepository<PayrollDeductionEntity, UUID> {

    List<PayrollDeductionEntity> findByEmployeeIdOrderByDeductionDateDescCreatedAtDescIdDesc(UUID employeeId);

    List<PayrollDeductionEntity> findByEmployeeIdAndStatusOrderByDeductionDateDescCreatedAtDescIdDesc(
            UUID employeeId,
            PayrollDeductionStatus status
    );
}
