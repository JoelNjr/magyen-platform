package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.SellerCommissionSettlement;
import com.magyen.platform.finance.domain.SellerCommissionSettlementRepository;
import com.magyen.platform.finance.infrastructure.persistence.entity.SellerCommissionSettlementEntity;
import com.magyen.platform.finance.infrastructure.persistence.mapper.SellerCommissionSettlementPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura del port {@link SellerCommissionSettlementRepository}.
 */
@Repository
public class JpaSellerCommissionSettlementRepository implements SellerCommissionSettlementRepository {

    private final SpringDataSellerCommissionSettlementRepository springDataRepository;
    private final SellerCommissionSettlementPersistenceMapper persistenceMapper;

    public JpaSellerCommissionSettlementRepository(
            SpringDataSellerCommissionSettlementRepository springDataRepository,
            SellerCommissionSettlementPersistenceMapper persistenceMapper
    ) {
        this.springDataRepository = Objects.requireNonNull(
                springDataRepository,
                "Spring Data seller commission settlement repository must not be null"
        );
        this.persistenceMapper = Objects.requireNonNull(
                persistenceMapper,
                "Seller commission settlement persistence mapper must not be null"
        );
    }

    @Override
    public SellerCommissionSettlement save(SellerCommissionSettlement settlement) {
        Objects.requireNonNull(settlement, "Seller commission settlement must not be null");
        SellerCommissionSettlementEntity saved = springDataRepository.saveAndFlush(
                persistenceMapper.toEntity(settlement)
        );
        return persistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<SellerCommissionSettlement> findById(UUID id) {
        Objects.requireNonNull(id, "Settlement id must not be null");
        return springDataRepository.findById(id).map(persistenceMapper::toDomain);
    }

    @Override
    public Optional<SellerCommissionSettlement> findByEmployeeIdAndPeriodStart(
            UUID employeeId,
            LocalDate periodStart
    ) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        Objects.requireNonNull(periodStart, "Period start must not be null");
        return springDataRepository
                .findByEmployeeIdAndPeriodStart(employeeId, periodStart)
                .map(persistenceMapper::toDomain);
    }

    @Override
    public List<SellerCommissionSettlement> findByPeriodStart(LocalDate periodStart) {
        Objects.requireNonNull(periodStart, "Period start must not be null");
        return springDataRepository.findByPeriodStart(periodStart).stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }
}
