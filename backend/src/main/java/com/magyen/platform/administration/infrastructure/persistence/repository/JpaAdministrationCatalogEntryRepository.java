package com.magyen.platform.administration.infrastructure.persistence.repository;

import com.magyen.platform.administration.domain.AdministrationCatalogEntry;
import com.magyen.platform.administration.domain.AdministrationCatalogEntryRepository;
import com.magyen.platform.administration.domain.AdministrationCatalogKind;
import com.magyen.platform.administration.infrastructure.persistence.entity.AdministrationCatalogEntryEntity;
import com.magyen.platform.administration.infrastructure.persistence.mapper.AdministrationCatalogEntryPersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa {@link AdministrationCatalogEntryRepository}.
 */
@Repository
public class JpaAdministrationCatalogEntryRepository implements AdministrationCatalogEntryRepository {

    private final SpringDataAdministrationCatalogEntryJpaRepository springDataAdministrationCatalogEntryJpaRepository;
    private final AdministrationCatalogEntryPersistenceMapper administrationCatalogEntryPersistenceMapper;

    public JpaAdministrationCatalogEntryRepository(
            SpringDataAdministrationCatalogEntryJpaRepository springDataAdministrationCatalogEntryJpaRepository,
            AdministrationCatalogEntryPersistenceMapper administrationCatalogEntryPersistenceMapper
    ) {
        this.springDataAdministrationCatalogEntryJpaRepository = Objects.requireNonNull(
                springDataAdministrationCatalogEntryJpaRepository,
                "Spring Data catalog entry JPA repository must not be null"
        );
        this.administrationCatalogEntryPersistenceMapper = Objects.requireNonNull(
                administrationCatalogEntryPersistenceMapper,
                "Catalog entry persistence mapper must not be null"
        );
    }

    @Override
    @Transactional
    public AdministrationCatalogEntry save(AdministrationCatalogEntry catalogEntry) {
        Objects.requireNonNull(catalogEntry, "Catalog entry must not be null");

        AdministrationCatalogEntryEntity entity = administrationCatalogEntryPersistenceMapper.toEntity(catalogEntry);
        AdministrationCatalogEntryEntity saved = springDataAdministrationCatalogEntryJpaRepository.save(entity);
        return administrationCatalogEntryPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdministrationCatalogEntry> findById(UUID id) {
        Objects.requireNonNull(id, "Catalog entry id must not be null");

        return springDataAdministrationCatalogEntryJpaRepository.findById(id)
                .map(administrationCatalogEntryPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdministrationCatalogEntry> findByKindAndNameIgnoreCase(
            AdministrationCatalogKind kind,
            String name
    ) {
        Objects.requireNonNull(kind, "Catalog kind must not be null");
        Objects.requireNonNull(name, "Catalog name must not be null");

        return springDataAdministrationCatalogEntryJpaRepository
                .findByCatalogKindAndNameIgnoreCase(kind, name.trim())
                .map(administrationCatalogEntryPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministrationCatalogEntry> findByKindOrderByName(AdministrationCatalogKind kind) {
        Objects.requireNonNull(kind, "Catalog kind must not be null");

        return springDataAdministrationCatalogEntryJpaRepository
                .findByCatalogKindOrderByNameAsc(kind)
                .stream()
                .map(administrationCatalogEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministrationCatalogEntry> findAllOrderByKindAndName() {
        return springDataAdministrationCatalogEntryJpaRepository.findAllByOrderByCatalogKindAscNameAsc().stream()
                .map(administrationCatalogEntryPersistenceMapper::toDomain)
                .toList();
    }
}
