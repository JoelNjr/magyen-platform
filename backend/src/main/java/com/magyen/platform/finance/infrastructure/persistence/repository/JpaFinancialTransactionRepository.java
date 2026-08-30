package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.infrastructure.persistence.entity.FinancialTransactionEntity;
import com.magyen.platform.finance.infrastructure.persistence.mapper.FinancialTransactionPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link FinancialTransactionRepository}.
 */
@Repository
public class JpaFinancialTransactionRepository implements FinancialTransactionRepository {

    private final SpringDataFinancialTransactionRepository springDataFinancialTransactionRepository;
    private final FinancialTransactionPersistenceMapper financialTransactionPersistenceMapper;

    public JpaFinancialTransactionRepository(
            SpringDataFinancialTransactionRepository springDataFinancialTransactionRepository,
            FinancialTransactionPersistenceMapper financialTransactionPersistenceMapper
    ) {
        this.springDataFinancialTransactionRepository = Objects.requireNonNull(
                springDataFinancialTransactionRepository,
                "Spring Data Financial Transaction repository must not be null"
        );
        this.financialTransactionPersistenceMapper = Objects.requireNonNull(
                financialTransactionPersistenceMapper,
                "Financial transaction persistence mapper must not be null"
        );
    }

    @Override
    public FinancialTransaction save(FinancialTransaction financialTransaction) {
        Objects.requireNonNull(financialTransaction, "Financial transaction must not be null");

        FinancialTransactionEntity entity = financialTransactionPersistenceMapper.toEntity(financialTransaction);
        FinancialTransactionEntity saved = springDataFinancialTransactionRepository.save(entity);
        return financialTransactionPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<FinancialTransaction> findById(UUID id) {
        Objects.requireNonNull(id, "Financial transaction id must not be null");

        return springDataFinancialTransactionRepository.findById(id)
                .map(financialTransactionPersistenceMapper::toDomain);
    }

    @Override
    public Optional<FinancialTransaction> findBySourceTypeAndSourceId(
            FinancialTransactionSourceType sourceType,
            UUID sourceId
    ) {
        Objects.requireNonNull(sourceType, "Source type must not be null");
        Objects.requireNonNull(sourceId, "Source id must not be null");

        return springDataFinancialTransactionRepository
                .findBySourceTypeAndSourceId(sourceType, sourceId)
                .map(financialTransactionPersistenceMapper::toDomain);
    }

    @Override
    public List<FinancialTransaction> findAllNewestFirst() {
        return springDataFinancialTransactionRepository.findAllByOrderByTransactionDateDescIdDesc()
                .stream()
                .map(financialTransactionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<FinancialTransaction> findByTransactionDateBetweenNewestFirst(LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");
        return springDataFinancialTransactionRepository
                .findByTransactionDateBetweenOrderByTransactionDateDescIdDesc(fromDate, toDate)
                .stream()
                .map(financialTransactionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public BigDecimal sumAmountByTypeBetween(
            FinancialTransactionType type,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Objects.requireNonNull(type, "Transaction type must not be null");
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");
        return normalizeMoney(
                springDataFinancialTransactionRepository.sumAmountByTypeAndTransactionDateBetween(
                        type,
                        fromDate,
                        toDate
                )
        );
    }

    @Override
    public long countByTransactionDateBetween(LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");
        return springDataFinancialTransactionRepository.countByTransactionDateBetween(fromDate, toDate);
    }

    private static BigDecimal normalizeMoney(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
