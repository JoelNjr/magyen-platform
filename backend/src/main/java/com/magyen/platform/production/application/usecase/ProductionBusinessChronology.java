package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.port.CommercialOrderChronology;
import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.time.LocalDate;

/**
 * Valida la cronología de negocio entre Commercial y Production.
 * <p>
 * Permite fechas históricas. No exige que las fechas coincidan con hoy.
 */
final class ProductionBusinessChronology {

    private ProductionBusinessChronology() {
    }

    static void validatePlan(CommercialOrderChronology chronology, LocalDate plannedStart, LocalDate plannedEnd) {
        if (plannedStart.isBefore(chronology.confirmationDate())) {
            throw new ProductionDomainException(
                    "Planned start date must not be before order confirmation date"
            );
        }
        if (plannedEnd.isAfter(chronology.deliveryDate())) {
            throw new ProductionDomainException(
                    "Planned end date must not be after delivery date"
            );
        }
    }

    static void validateStart(CommercialOrderChronology chronology, LocalDate actualStartDate) {
        if (actualStartDate.isBefore(chronology.confirmationDate())) {
            throw new ProductionDomainException(
                    "Production start date must not be before order confirmation date"
            );
        }
        if (actualStartDate.isAfter(chronology.deliveryDate())) {
            throw new ProductionDomainException(
                    "Production start date must not be after delivery date"
            );
        }
    }

    static void validateCompletion(
            CommercialOrderChronology chronology,
            LocalDate actualStartDate,
            LocalDate actualCompletionDate
    ) {
        if (actualStartDate != null && actualCompletionDate.isBefore(actualStartDate)) {
            throw new ProductionDomainException(
                    "Production completion date must not be before production start date"
            );
        }
        if (actualCompletionDate.isBefore(chronology.confirmationDate())) {
            throw new ProductionDomainException(
                    "Production completion date must not be before order confirmation date"
            );
        }
        if (actualCompletionDate.isAfter(chronology.deliveryDate())) {
            throw new ProductionDomainException(
                    "Production completion date must not be after delivery date"
            );
        }
    }
}
