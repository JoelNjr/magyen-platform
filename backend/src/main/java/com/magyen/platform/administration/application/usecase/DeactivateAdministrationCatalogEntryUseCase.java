package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AdministrationCatalogEntryResult;
import com.magyen.platform.administration.application.dto.DeactivateAdministrationCatalogEntryCommand;
import com.magyen.platform.administration.domain.AdministrationCatalogEntry;
import com.magyen.platform.administration.domain.AdministrationCatalogEntryRepository;
import com.magyen.platform.administration.domain.exception.AdministrationDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Desactiva una entrada de catálogo. Los registros históricos conservan el nombre persistido.
 */
public class DeactivateAdministrationCatalogEntryUseCase {

    private final AdministrationCatalogEntryRepository administrationCatalogEntryRepository;

    public DeactivateAdministrationCatalogEntryUseCase(
            AdministrationCatalogEntryRepository administrationCatalogEntryRepository
    ) {
        this.administrationCatalogEntryRepository = Objects.requireNonNull(
                administrationCatalogEntryRepository,
                "Catalog entry repository must not be null"
        );
    }

    @Transactional
    public AdministrationCatalogEntryResult execute(DeactivateAdministrationCatalogEntryCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.catalogEntryId(), "Catalog entry id must not be null");

        AdministrationCatalogEntry catalogEntry = administrationCatalogEntryRepository
                .findById(command.catalogEntryId())
                .orElseThrow(() -> new AdministrationDomainException("Catalog entry was not found."));

        AdministrationCatalogEntry saved = administrationCatalogEntryRepository.save(catalogEntry.deactivate());
        return AdministrationCatalogEntryMapper.toResult(saved);
    }
}
