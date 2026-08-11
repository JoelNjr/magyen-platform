package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.infrastructure.persistence.entity.RecurringFinancialObligationEntity;
import com.magyen.platform.finance.infrastructure.persistence.mapper.RecurringFinancialObligationPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link RecurringFinancialObligationRepository}.
 */
@Repository
public class JpaRecurringFinancialObligationRepository implements RecurringFinancialObligationRepository {

    private final SpringDataRecurringFinancialObligationRepository springDataRecurringFinancialObligationRepository;
    private final RecurringFinancialObligationPersistenceMapper recurringFinancialObligationPersistenceMapper;

    public JpaRecurringFinancialObligationRepository(
            SpringDataRecurringFinancialObligationRepository springDataRecurringFinancialObligationRepository,
            RecurringFinancialObligationPersistenceMapper recurringFinancialObligationPersistenceMapper
    ) {
        this.springDataRecurringFinancialObligationRepository = Objects.requireNonNull(
                springDataRecurringFinancialObligationRepository,
                "Spring Data recurring financial obligation repository must not be null"
        );
        this.recurringFinancialObligationPersistenceMapper = Objects.requireNonNull(
                recurringFinancialObligationPersistenceMapper,
                "Recurring financial obligation persistence mapper must not be null"
        );
    }

    @Override
    public RecurringFinancialObligation save(RecurringFinancialObligation obligation) {
        return persist(obligation);
    }

    @Override
    public RecurringFinancialObligation update(RecurringFinancialObligation obligation) {
        return persist(obligation);
    }

    @Override
    public Optional<RecurringFinancialObligation> findById(UUID id) {
        Objects.requireNonNull(id, "Obligation id must not be null");

        return springDataRecurringFinancialObligationRepository.findById(id)
                .map(recurringFinancialObligationPersistenceMapper::toDomain);
    }

    @Override
    public List<RecurringFinancialObligation> findAll() {
        return springDataRecurringFinancialObligationRepository.findAllByOrderByNameAscIdAsc()
                .stream()
                .map(recurringFinancialObligationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringFinancialObligation> findActive() {
        return springDataRecurringFinancialObligationRepository.findByActiveTrueOrderByNameAscIdAsc()
                .stream()
                .map(recurringFinancialObligationPersistenceMapper::toDomain)
                .toList();
    }

    private RecurringFinancialObligation persist(RecurringFinancialObligation obligation) {
        Objects.requireNonNull(obligation, "Obligation must not be null");

        RecurringFinancialObligationEntity entity =
                recurringFinancialObligationPersistenceMapper.toEntity(obligation);
        RecurringFinancialObligationEntity saved =
                springDataRecurringFinancialObligationRepository.save(entity);
        return recurringFinancialObligationPersistenceMapper.toDomain(saved);
    }
}
