package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.QuotationDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Grupo comercial de un cliente. No existe un grupo BOTH.
 * <p>
 * {@code UNCLASSIFIED} conserva clientes históricos sin actividad que permita
 * decidir el grupo. No es un tercer grupo de negocio.
 */
public enum CustomerCategory {
    MAGYEN,
    PLOTTER,
    UNCLASSIFIED;

    public static CustomerCategory of(String value) {
        Objects.requireNonNull(value, "Customer category must not be null");
        if (value.isBlank()) {
            throw new QuotationDomainException("Customer category must not be blank");
        }
        try {
            return CustomerCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new QuotationDomainException("Unsupported customer category: " + value);
        }
    }

    public boolean isMagyen() {
        return this == MAGYEN;
    }

    public boolean isPlotter() {
        return this == PLOTTER;
    }

    public boolean allowsNewQuotation() {
        return this == MAGYEN;
    }

    public boolean allowsExternalPlotterJob() {
        return this == PLOTTER;
    }
}
