package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.infrastructure.persistence.entity.PayrollEmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link PayrollEmployeeEntity}.
 */
public interface SpringDataPayrollEmployeeRepository extends JpaRepository<PayrollEmployeeEntity, UUID> {

    List<PayrollEmployeeEntity> findAllByOrderByDisplayNameAscIdAsc();

    List<PayrollEmployeeEntity> findByActiveOrderByDisplayNameAscIdAsc(boolean active);
}
