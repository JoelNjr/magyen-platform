package com.magyen.platform.commercial.application;

import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.application.port.CommercialCatalogPort;
import com.magyen.platform.commercial.domain.ProductSpecification;

import java.util.Objects;

/**
 * Valida escrituras comerciales contra catálogos activos de Administración.
 */
public class CommercialCatalogValidator {

    private final CommercialCatalogPort commercialCatalogPort;

    public CommercialCatalogValidator(CommercialCatalogPort commercialCatalogPort) {
        this.commercialCatalogPort = Objects.requireNonNull(
                commercialCatalogPort,
                "Commercial catalog port must not be null"
        );
    }

    public String requirePrimaryFabric(String fabric) {
        return commercialCatalogPort.requireActiveFabric(fabric);
    }

    public String requireSecondaryFabric(String secondaryFabric) {
        return commercialCatalogPort.requireActiveSecondaryFabric(secondaryFabric);
    }

    public ProductSpecification requireProductSpecification(ProductSpecificationCommand command) {
        if (command == null) {
            return ProductSpecification.empty();
        }

        return ProductSpecification.of(
                commercialCatalogPort.requireActiveGarment(command.garmentType()),
                commercialCatalogPort.requireActiveCollar(command.collarType()),
                commercialCatalogPort.requireActiveSleeve(command.sleeveType()),
                command.cuffRequired(),
                command.sublimationRequired(),
                command.embroideryRequired(),
                command.dtfRequired(),
                command.decorationNotes(),
                command.includesNames(),
                command.includesNumbers(),
                command.includesLogos(),
                command.personalizationNotes(),
                command.itemObservations()
        );
    }
}
