package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.time.LocalDate;

/**
 * La fecha del trabajo interno no puede salir del rango de la orden comercial.
 */
final class PlotterJobDateRules {

    private PlotterJobDateRules() {
    }

    static void validateAgainstOrder(LocalDate creationDate, PlotterCommercialOrderView commercialOrder) {
        if (commercialOrder.confirmationDate() != null
                && creationDate.isBefore(commercialOrder.confirmationDate())) {
            throw new PlotterDomainException(
                    "Plotter job date must not be before order confirmation date"
            );
        }
        if (commercialOrder.deliveryDate() != null
                && creationDate.isAfter(commercialOrder.deliveryDate())) {
            throw new PlotterDomainException(
                    "Plotter job date must not be after delivery date"
            );
        }
    }
}
