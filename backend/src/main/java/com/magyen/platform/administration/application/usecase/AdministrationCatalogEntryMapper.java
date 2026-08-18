package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AdministrationCatalogEntryResult;
import com.magyen.platform.administration.domain.AdministrationCatalogEntry;

final class AdministrationCatalogEntryMapper {

    private AdministrationCatalogEntryMapper() {
    }

    static AdministrationCatalogEntryResult toResult(AdministrationCatalogEntry catalogEntry) {
        return new AdministrationCatalogEntryResult(
                catalogEntry.getId(),
                catalogEntry.getKind(),
                catalogEntry.getName(),
                catalogEntry.isActive()
        );
    }
}
