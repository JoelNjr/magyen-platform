package com.magyen.platform.commercial.infrastructure.administration;

import com.magyen.platform.administration.application.dto.AdministrationCatalogEntryResult;
import com.magyen.platform.administration.application.dto.ListAdministrationCatalogEntriesQuery;
import com.magyen.platform.administration.application.usecase.ListAdministrationCatalogEntriesUseCase;
import com.magyen.platform.administration.domain.AdministrationCatalogKind;
import com.magyen.platform.commercial.application.port.CommercialCatalogOption;
import com.magyen.platform.commercial.application.port.CommercialCatalogPort;

import java.util.List;
import java.util.Objects;

/**
 * Adaptador Commercial → Administration para catálogos configurables.
 */
public class CommercialCatalogAdapter implements CommercialCatalogPort {

    private final ListAdministrationCatalogEntriesUseCase listAdministrationCatalogEntriesUseCase;

    public CommercialCatalogAdapter(
            ListAdministrationCatalogEntriesUseCase listAdministrationCatalogEntriesUseCase
    ) {
        this.listAdministrationCatalogEntriesUseCase = Objects.requireNonNull(
                listAdministrationCatalogEntriesUseCase,
                "List administration catalog entries use case must not be null"
        );
    }

    @Override
    public List<CommercialCatalogOption> listActiveGarments() {
        return listActive(AdministrationCatalogKind.GARMENT);
    }

    @Override
    public List<CommercialCatalogOption> listActiveFabrics() {
        return listActive(AdministrationCatalogKind.FABRIC);
    }

    @Override
    public List<CommercialCatalogOption> listActiveCollars() {
        return listActive(AdministrationCatalogKind.COLLAR);
    }

    @Override
    public List<CommercialCatalogOption> listActiveSleeves() {
        return listActive(AdministrationCatalogKind.SLEEVE);
    }

    @Override
    public String requireActiveGarment(String value) {
        return requireActive(AdministrationCatalogKind.GARMENT, value, false, "garment type");
    }

    @Override
    public String requireActiveFabric(String value) {
        return requireActive(AdministrationCatalogKind.FABRIC, value, true, "fabric");
    }

    @Override
    public String requireActiveSecondaryFabric(String value) {
        return requireActive(AdministrationCatalogKind.FABRIC, value, false, "secondary fabric");
    }

    @Override
    public String requireActiveCollar(String value) {
        return requireActive(AdministrationCatalogKind.COLLAR, value, false, "collar type");
    }

    @Override
    public String requireActiveSleeve(String value) {
        return requireActive(AdministrationCatalogKind.SLEEVE, value, false, "sleeve type");
    }

    private List<CommercialCatalogOption> listActive(AdministrationCatalogKind kind) {
        return listAdministrationCatalogEntriesUseCase
                .execute(new ListAdministrationCatalogEntriesQuery(kind, true))
                .stream()
                .map(entry -> new CommercialCatalogOption(entry.name(), entry.name()))
                .toList();
    }

    private String requireActive(
            AdministrationCatalogKind kind,
            String value,
            boolean required,
            String catalogName
    ) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new IllegalArgumentException(catalogName + " must not be blank");
            }
            return null;
        }

        String trimmed = value.trim();
        List<AdministrationCatalogEntryResult> entries = listAdministrationCatalogEntriesUseCase.execute(
                new ListAdministrationCatalogEntriesQuery(kind, false)
        );

        AdministrationCatalogEntryResult match = entries.stream()
                .filter(entry -> matches(entry, trimmed))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported " + catalogName + ": " + trimmed));

        if (!match.active()) {
            throw new IllegalArgumentException(
                    "Inactive " + catalogName + " cannot be selected: " + match.name()
            );
        }

        return match.name();
    }

    private static boolean matches(AdministrationCatalogEntryResult entry, String trimmed) {
        return entry.name().equalsIgnoreCase(trimmed)
                || entry.catalogEntryId().toString().equalsIgnoreCase(trimmed);
    }
}
