package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AdministrationCatalogEntryResult;
import com.magyen.platform.administration.application.dto.CreateAdministrationCatalogEntryCommand;
import com.magyen.platform.administration.domain.AdministrationCatalogEntry;
import com.magyen.platform.administration.domain.AdministrationCatalogEntryRepository;
import com.magyen.platform.administration.domain.exception.CatalogNameAlreadyExistsException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Crea una entrada de catálogo. No toca Inventario ni Finanzas.
 */
public class CreateAdministrationCatalogEntryUseCase {

    private final AdministrationCatalogEntryRepository administrationCatalogEntryRepository;

    public CreateAdministrationCatalogEntryUseCase(
            AdministrationCatalogEntryRepository administrationCatalogEntryRepository
    ) {
        this.administrationCatalogEntryRepository = Objects.requireNonNull(
                administrationCatalogEntryRepository,
                "Catalog entry repository must not be null"
        );
    }

    @Transactional
    public AdministrationCatalogEntryResult execute(CreateAdministrationCatalogEntryCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.kind(), "Catalog kind must not be null");

        AdministrationCatalogEntry catalogEntry = AdministrationCatalogEntry.create(command.kind(), command.name());

        administrationCatalogEntryRepository
                .findByKindAndNameIgnoreCase(catalogEntry.getKind(), catalogEntry.getName())
                .ifPresent(existing -> {
                    throw new CatalogNameAlreadyExistsException();
                });

        AdministrationCatalogEntry saved = administrationCatalogEntryRepository.save(catalogEntry);
        return AdministrationCatalogEntryMapper.toResult(saved);
    }
}
