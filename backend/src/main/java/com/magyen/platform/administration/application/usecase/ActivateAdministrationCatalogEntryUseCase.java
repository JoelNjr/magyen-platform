package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.ActivateAdministrationCatalogEntryCommand;
import com.magyen.platform.administration.application.dto.AdministrationCatalogEntryResult;
import com.magyen.platform.administration.domain.AdministrationCatalogEntry;
import com.magyen.platform.administration.domain.AdministrationCatalogEntryRepository;
import com.magyen.platform.administration.domain.exception.AdministrationDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Reactiva una entrada de catálogo. No recrea Inventario ni Finanzas.
 */
public class ActivateAdministrationCatalogEntryUseCase {

    private final AdministrationCatalogEntryRepository administrationCatalogEntryRepository;

    public ActivateAdministrationCatalogEntryUseCase(
            AdministrationCatalogEntryRepository administrationCatalogEntryRepository
    ) {
        this.administrationCatalogEntryRepository = Objects.requireNonNull(
                administrationCatalogEntryRepository,
                "Catalog entry repository must not be null"
        );
    }

    @Transactional
    public AdministrationCatalogEntryResult execute(ActivateAdministrationCatalogEntryCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.catalogEntryId(), "Catalog entry id must not be null");

        AdministrationCatalogEntry catalogEntry = administrationCatalogEntryRepository
                .findById(command.catalogEntryId())
                .orElseThrow(() -> new AdministrationDomainException("Catalog entry was not found."));

        AdministrationCatalogEntry saved = administrationCatalogEntryRepository.save(catalogEntry.activate());
        return AdministrationCatalogEntryMapper.toResult(saved);
    }
}
