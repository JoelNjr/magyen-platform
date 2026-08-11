package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.infrastructure.persistence.entity.FinancialTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link FinancialTransactionEntity}.
 */
public interface SpringDataFinancialTransactionRepository
        extends JpaRepository<FinancialTransactionEntity, UUID> {

    List<FinancialTransactionEntity> findAllByOrderByTransactionDateDescIdDesc();

    Optional<FinancialTransactionEntity> findBySourceTypeAndSourceId(
            FinancialTransactionSourceType sourceType,
            UUID sourceId
    );

    @Query("""
            select coalesce(sum(ledger.amount), 0)
            from FinancialTransactionEntity ledger
            where ledger.transactionType = :type
              and ledger.transactionDate between :fromDate and :toDate
            """)
    BigDecimal sumAmountByTypeAndTransactionDateBetween(
            @Param("type") FinancialTransactionType type,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    long countByTransactionDateBetween(LocalDate fromDate, LocalDate toDate);
}
