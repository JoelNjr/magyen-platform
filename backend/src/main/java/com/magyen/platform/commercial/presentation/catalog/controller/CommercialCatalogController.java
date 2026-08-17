package com.magyen.platform.commercial.presentation.catalog.controller;

import com.magyen.platform.commercial.application.dto.GetCommercialCatalogsResult;
import com.magyen.platform.commercial.application.usecase.GetCommercialCatalogsUseCase;
import com.magyen.platform.commercial.presentation.catalog.mapper.CommercialCatalogPresentationMapper;
import com.magyen.platform.commercial.presentation.catalog.response.GetCommercialCatalogsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los catálogos comerciales cerrados.
 * <p>
 * Solo lectura. No consulta inventario ni muta datos.
 */
@RestController
@RequestMapping("/api/v1/commercial-catalogs")
public class CommercialCatalogController {

    private final GetCommercialCatalogsUseCase getCommercialCatalogsUseCase;
    private final CommercialCatalogPresentationMapper presentationMapper;

    public CommercialCatalogController(
            GetCommercialCatalogsUseCase getCommercialCatalogsUseCase,
            CommercialCatalogPresentationMapper presentationMapper
    ) {
        this.getCommercialCatalogsUseCase = getCommercialCatalogsUseCase;
        this.presentationMapper = presentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetCommercialCatalogsResponse> getCatalogs() {
        GetCommercialCatalogsResult result = getCommercialCatalogsUseCase.execute();
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }
}
