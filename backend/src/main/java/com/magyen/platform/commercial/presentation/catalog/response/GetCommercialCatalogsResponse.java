package com.magyen.platform.commercial.presentation.catalog.response;

import java.util.List;

/**
 * Catálogos comerciales cerrados para la UI.
 */
public record GetCommercialCatalogsResponse(
        List<CatalogOptionResponse> garmentTypes,
        List<CatalogOptionResponse> collarTypes,
        List<CatalogOptionResponse> sleeveTypes,
        List<CuffOptionResponse> cuffOptions,
        List<CatalogOptionResponse> fabrics
) {

    public record CatalogOptionResponse(String value, String label) {
    }

    public record CuffOptionResponse(boolean value, String label) {
    }
}
