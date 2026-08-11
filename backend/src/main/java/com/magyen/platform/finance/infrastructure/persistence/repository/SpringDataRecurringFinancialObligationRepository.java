package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.infrastructure.persistence.entity.RecurringFinancialObligationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link RecurringFinancialObligationEntity}.
 */
public interface SpringDataRecurringFinancialObligationRepository
        extends JpaRepository<RecurringFinancialObligationEntity, UUID> {

    List<RecurringFinancialObligationEntity> findAllByOrderByNameAscIdAsc();

    List<RecurringFinancialObligationEntity> findByActiveTrueOrderByNameAscIdAsc();
}
