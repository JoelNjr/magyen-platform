package com.magyen.platform.commercial.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Catálogo cerrado de tipo de cuello comercial.
 */
public enum CollarType implements LabeledCatalog {
    REDONDO("Redondo"),
    EN_V_RECTO("En V recto"),
    EN_V_CRUZADO("En V cruzado"),
    TEJIDO("Tejido");

    private final String label;

    CollarType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

    public static CollarType of(String value) {
        return CommercialCatalogParser.parseOptional(CollarType.class, value, "collar type");
    }

    public static String canonicalize(String value) {
        CollarType parsed = of(value);
        return parsed == null ? null : parsed.label();
    }

    public static List<CollarType> catalog() {
        return Arrays.asList(values());
    }
}
