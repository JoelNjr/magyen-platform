package com.magyen.platform.commercial.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Catálogo cerrado de tipo de manga comercial.
 */
public enum SleeveType implements LabeledCatalog {
    MANGA_CORTA_SISA("Manga corta sisa"),
    MANGA_CORTA_RANGLA("Manga corta rangla"),
    MANGA_LARGA_SISA("Manga larga sisa"),
    MANGA_LARGA_RANGLA("Manga larga rangla");

    private final String label;

    SleeveType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

    public static SleeveType of(String value) {
        return CommercialCatalogParser.parseOptional(SleeveType.class, value, "sleeve type");
    }

    public static String canonicalize(String value) {
        SleeveType parsed = of(value);
        return parsed == null ? null : parsed.label();
    }

    public static List<SleeveType> catalog() {
        return Arrays.asList(values());
    }
}
