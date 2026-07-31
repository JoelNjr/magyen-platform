package com.magyen.platform.commercial.infrastructure.persistence.repository;

import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.infrastructure.persistence.entity.QuotationEntity;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.QuotationPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link QuotationRepository}.
 * <p>
 * Traduce entre el agregado de dominio y el modelo JPA; nunca expone entidades de persistencia.
 */
@Repository
public class JpaQuotationRepository implements QuotationRepository {

    private final SpringDataQuotationJpaRepository springDataQuotationJpaRepository;
    private final QuotationPersistenceMapper quotationPersistenceMapper;

    public JpaQuotationRepository(
            SpringDataQuotationJpaRepository springDataQuotationJpaRepository,
            QuotationPersistenceMapper quotationPersistenceMapper
    ) {
        this.springDataQuotationJpaRepository = Objects.requireNonNull(
                springDataQuotationJpaRepository,
                "Spring Data Quotation JPA repository must not be null"
        );
        this.quotationPersistenceMapper = Objects.requireNonNull(
                quotationPersistenceMapper,
                "Quotation persistence mapper must not be null"
        );
    }

    @Override
    public Quotation save(Quotation quotation) {
        Objects.requireNonNull(quotation, "Quotation must not be null");

        QuotationEntity quotationEntity = quotationPersistenceMapper.toEntity(quotation);
        QuotationEntity savedQuotationEntity = springDataQuotationJpaRepository.save(quotationEntity);
        return quotationPersistenceMapper.toDomain(savedQuotationEntity);
    }

    @Override
    public Optional<Quotation> findById(UUID id) {
        Objects.requireNonNull(id, "Quotation id must not be null");

        return springDataQuotationJpaRepository.findById(id)
                .map(quotationPersistenceMapper::toDomain);
    }
}
