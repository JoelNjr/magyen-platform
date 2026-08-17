package com.magyen.platform.commercial.presentation.catalog.mapper;

import com.magyen.platform.commercial.application.dto.GetCommercialCatalogsResult;
import com.magyen.platform.commercial.presentation.catalog.response.GetCommercialCatalogsResponse;
import com.magyen.platform.commercial.presentation.catalog.response.GetCommercialCatalogsResponse.CatalogOptionResponse;
import com.magyen.platform.commercial.presentation.catalog.response.GetCommercialCatalogsResponse.CuffOptionResponse;

import java.util.Objects;

/**
 * Convierte el catálogo comercial de Application a HTTP.
 */
public class CommercialCatalogPresentationMapper {

    public GetCommercialCatalogsResponse toResponse(GetCommercialCatalogsResult result) {
        Objects.requireNonNull(result, "Catalog result must not be null");
        return new GetCommercialCatalogsResponse(
                result.garmentTypes().stream()
                        .map(option -> new CatalogOptionResponse(option.value(), option.label()))
                        .toList(),
                result.collarTypes().stream()
                        .map(option -> new CatalogOptionResponse(option.value(), option.label()))
                        .toList(),
                result.sleeveTypes().stream()
                        .map(option -> new CatalogOptionResponse(option.value(), option.label()))
                        .toList(),
                result.cuffOptions().stream()
                        .map(option -> new CuffOptionResponse(option.value(), option.label()))
                        .toList(),
                result.fabrics().stream()
                        .map(option -> new CatalogOptionResponse(option.value(), option.label()))
                        .toList()
        );
    }
}
