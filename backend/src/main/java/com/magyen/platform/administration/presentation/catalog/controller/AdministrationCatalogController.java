package com.magyen.platform.administration.presentation.catalog.controller;

import com.magyen.platform.administration.application.dto.AdministrationCatalogEntryResult;
import com.magyen.platform.administration.application.dto.CreateAdministrationCatalogEntryCommand;
import com.magyen.platform.administration.application.dto.GetAdministrationCatalogsResult;
import com.magyen.platform.administration.application.dto.ListAdministrationCatalogEntriesQuery;
import com.magyen.platform.administration.application.usecase.ActivateAdministrationCatalogEntryUseCase;
import com.magyen.platform.administration.application.usecase.CreateAdministrationCatalogEntryUseCase;
import com.magyen.platform.administration.application.usecase.DeactivateAdministrationCatalogEntryUseCase;
import com.magyen.platform.administration.application.usecase.GetAdministrationCatalogsUseCase;
import com.magyen.platform.administration.application.usecase.ListAdministrationCatalogEntriesUseCase;
import com.magyen.platform.administration.presentation.catalog.mapper.AdministrationCatalogPresentationMapper;
import com.magyen.platform.administration.presentation.catalog.request.CreateAdministrationCatalogEntryRequest;
import com.magyen.platform.administration.presentation.catalog.response.AdministrationCatalogEntryResponse;
import com.magyen.platform.administration.presentation.catalog.response.GetAdministrationCatalogEntriesResponse;
import com.magyen.platform.administration.presentation.catalog.response.GetAdministrationCatalogsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * API de gestión de catálogos de Administración. Solo ADMIN.
 * <p>
 * OPERATOR consume valores activos vía {@code GET /api/v1/commercial-catalogs}.
 */
@RestController
@RequestMapping("/api/v1/admin/catalogs")
public class AdministrationCatalogController {

    private final GetAdministrationCatalogsUseCase getAdministrationCatalogsUseCase;
    private final ListAdministrationCatalogEntriesUseCase listAdministrationCatalogEntriesUseCase;
    private final CreateAdministrationCatalogEntryUseCase createAdministrationCatalogEntryUseCase;
    private final ActivateAdministrationCatalogEntryUseCase activateAdministrationCatalogEntryUseCase;
    private final DeactivateAdministrationCatalogEntryUseCase deactivateAdministrationCatalogEntryUseCase;
    private final AdministrationCatalogPresentationMapper administrationCatalogPresentationMapper;

    public AdministrationCatalogController(
            GetAdministrationCatalogsUseCase getAdministrationCatalogsUseCase,
            ListAdministrationCatalogEntriesUseCase listAdministrationCatalogEntriesUseCase,
            CreateAdministrationCatalogEntryUseCase createAdministrationCatalogEntryUseCase,
            ActivateAdministrationCatalogEntryUseCase activateAdministrationCatalogEntryUseCase,
            DeactivateAdministrationCatalogEntryUseCase deactivateAdministrationCatalogEntryUseCase,
            AdministrationCatalogPresentationMapper administrationCatalogPresentationMapper
    ) {
        this.getAdministrationCatalogsUseCase = getAdministrationCatalogsUseCase;
        this.listAdministrationCatalogEntriesUseCase = listAdministrationCatalogEntriesUseCase;
        this.createAdministrationCatalogEntryUseCase = createAdministrationCatalogEntryUseCase;
        this.activateAdministrationCatalogEntryUseCase = activateAdministrationCatalogEntryUseCase;
        this.deactivateAdministrationCatalogEntryUseCase = deactivateAdministrationCatalogEntryUseCase;
        this.administrationCatalogPresentationMapper = administrationCatalogPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetAdministrationCatalogsResponse> getCatalogs() {
        GetAdministrationCatalogsResult result = getAdministrationCatalogsUseCase.execute();
        return ResponseEntity.ok(administrationCatalogPresentationMapper.toResponse(result));
    }

    @GetMapping("/{catalogKind}")
    public ResponseEntity<GetAdministrationCatalogEntriesResponse> listCatalog(
            @PathVariable String catalogKind
    ) {
        ListAdministrationCatalogEntriesQuery query =
                administrationCatalogPresentationMapper.toListQuery(catalogKind);
        List<AdministrationCatalogEntryResult> results = listAdministrationCatalogEntriesUseCase.execute(query);
        return ResponseEntity.ok(administrationCatalogPresentationMapper.toEntriesResponse(results));
    }

    @PostMapping("/{catalogKind}")
    public ResponseEntity<AdministrationCatalogEntryResponse> createCatalogEntry(
            @PathVariable String catalogKind,
            @RequestBody CreateAdministrationCatalogEntryRequest request
    ) {
        CreateAdministrationCatalogEntryCommand command =
                administrationCatalogPresentationMapper.toCreateCommand(catalogKind, request);
        AdministrationCatalogEntryResult result = createAdministrationCatalogEntryUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(administrationCatalogPresentationMapper.toResponse(result));
    }

    @PatchMapping("/{catalogKind}/{catalogEntryId}/activate")
    public ResponseEntity<AdministrationCatalogEntryResponse> activateCatalogEntry(
            @PathVariable String catalogKind,
            @PathVariable UUID catalogEntryId
    ) {
        administrationCatalogPresentationMapper.toKind(catalogKind);
        AdministrationCatalogEntryResult result = activateAdministrationCatalogEntryUseCase.execute(
                administrationCatalogPresentationMapper.toActivateCommand(catalogEntryId)
        );
        return ResponseEntity.ok(administrationCatalogPresentationMapper.toResponse(result));
    }

    @PatchMapping("/{catalogKind}/{catalogEntryId}/deactivate")
    public ResponseEntity<AdministrationCatalogEntryResponse> deactivateCatalogEntry(
            @PathVariable String catalogKind,
            @PathVariable UUID catalogEntryId
    ) {
        administrationCatalogPresentationMapper.toKind(catalogKind);
        AdministrationCatalogEntryResult result = deactivateAdministrationCatalogEntryUseCase.execute(
                administrationCatalogPresentationMapper.toDeactivateCommand(catalogEntryId)
        );
        return ResponseEntity.ok(administrationCatalogPresentationMapper.toResponse(result));
    }
}
