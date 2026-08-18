package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetCommercialCatalogsResult;
import com.magyen.platform.commercial.application.dto.GetCommercialCatalogsResult.CatalogOptionResult;
import com.magyen.platform.commercial.application.dto.GetCommercialCatalogsResult.CuffOptionResult;
import com.magyen.platform.commercial.application.port.CommercialCatalogOption;
import com.magyen.platform.commercial.application.port.CommercialCatalogPort;

import java.util.List;
import java.util.Objects;

/**
 * Expone catálogos comerciales activos para selectores. El puño permanece boolean.
 * No consulta Inventario.
 */
public class GetCommercialCatalogsUseCase {

    private final CommercialCatalogPort commercialCatalogPort;

    public GetCommercialCatalogsUseCase(CommercialCatalogPort commercialCatalogPort) {
        this.commercialCatalogPort = Objects.requireNonNull(
                commercialCatalogPort,
                "Commercial catalog port must not be null"
        );
    }

    public GetCommercialCatalogsResult execute() {
        return new GetCommercialCatalogsResult(
                toOptions(commercialCatalogPort.listActiveGarments()),
                toOptions(commercialCatalogPort.listActiveCollars()),
                toOptions(commercialCatalogPort.listActiveSleeves()),
                List.of(
                        new CuffOptionResult(true, "Sí"),
                        new CuffOptionResult(false, "No")
                ),
                toOptions(commercialCatalogPort.listActiveFabrics())
        );
    }

    private static List<CatalogOptionResult> toOptions(List<CommercialCatalogOption> values) {
        return values.stream()
                .map(value -> new CatalogOptionResult(value.value(), value.label()))
                .toList();
    }
}
