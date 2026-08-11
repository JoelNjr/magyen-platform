package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.PayrollPeriodStatus;
import com.magyen.platform.finance.infrastructure.persistence.entity.PayrollPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para períodos de nómina.
 */
public interface SpringDataPayrollPeriodRepository extends JpaRepository<PayrollPeriodEntity, UUID> {

    Optional<PayrollPeriodEntity> findByEmployeeIdAndPeriodStart(UUID employeeId, LocalDate periodStart);

    List<PayrollPeriodEntity> findAllByOrderByExpectedPaymentDateDescIdDesc();

    List<PayrollPeriodEntity> findByExpectedPaymentDateBetweenOrderByExpectedPaymentDateAscIdAsc(
            LocalDate fromDate,
            LocalDate toDate
    );

    List<PayrollPeriodEntity> findByStatusOrderByExpectedPaymentDateAscIdAsc(PayrollPeriodStatus status);
}
