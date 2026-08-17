package com.magyen.platform.commercial.application.dto;

import java.util.List;

/**
 * Catálogos comerciales cerrados para selectores de cotización y orden.
 */
public record GetCommercialCatalogsResult(
        List<CatalogOptionResult> garmentTypes,
        List<CatalogOptionResult> collarTypes,
        List<CatalogOptionResult> sleeveTypes,
        List<CuffOptionResult> cuffOptions,
        List<CatalogOptionResult> fabrics
) {

    public record CatalogOptionResult(String value, String label) {
    }

    public record CuffOptionResult(boolean value, String label) {
    }
}
