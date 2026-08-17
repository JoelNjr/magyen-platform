package com.magyen.platform.commercial.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Catálogo cerrado de tipo de prenda comercial.
 * <p>
 * Se persiste la etiqueta canónica de negocio, no texto libre.
 */
public enum GarmentType implements LabeledCatalog {
    CAMISETA("Camiseta"),
    CAMISETA_TIPO_POLO("Camiseta tipo polo"),
    CONJUNTO_DEPORTIVO("Conjunto deportivo"),
    CONJUNTO_DE_PRESENTACION("Conjunto de presentación"),
    PANTALONETA("Pantaloneta"),
    OTRO("Otro");

    private final String label;

    GarmentType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

    public static GarmentType of(String value) {
        return CommercialCatalogParser.parseOptional(GarmentType.class, value, "garment type");
    }

    public static String canonicalize(String value) {
        GarmentType parsed = of(value);
        return parsed == null ? null : parsed.label();
    }

    public static List<GarmentType> catalog() {
        return Arrays.asList(values());
    }
}
