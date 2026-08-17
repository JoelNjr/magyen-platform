package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetCommercialCatalogsResult;
import com.magyen.platform.commercial.application.dto.GetCommercialCatalogsResult.CatalogOptionResult;
import com.magyen.platform.commercial.application.dto.GetCommercialCatalogsResult.CuffOptionResult;
import com.magyen.platform.commercial.domain.CollarType;
import com.magyen.platform.commercial.domain.CommercialFabric;
import com.magyen.platform.commercial.domain.GarmentType;
import com.magyen.platform.commercial.domain.LabeledCatalog;
import com.magyen.platform.commercial.domain.SleeveType;

import java.util.List;

/**
 * Expone los catálogos comerciales cerrados. No persiste ni consulta inventario.
 */
public class GetCommercialCatalogsUseCase {

    public GetCommercialCatalogsResult execute() {
        return new GetCommercialCatalogsResult(
                toOptions(GarmentType.catalog()),
                toOptions(CollarType.catalog()),
                toOptions(SleeveType.catalog()),
                List.of(
                        new CuffOptionResult(true, "Sí"),
                        new CuffOptionResult(false, "No")
                ),
                toOptions(CommercialFabric.catalog())
        );
    }

    private static List<CatalogOptionResult> toOptions(List<? extends LabeledCatalog> values) {
        return values.stream()
                .map(value -> new CatalogOptionResult(value.label(), value.label()))
                .toList();
    }
}
