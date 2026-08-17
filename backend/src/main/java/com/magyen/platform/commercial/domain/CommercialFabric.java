package com.magyen.platform.commercial.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Catálogo comercial controlado de tela / material.
 * <p>
 * Independiente del stock de Inventario: un material puede cotizarse con existencia cero.
 * No inventa un catálogo completo; reutiliza los valores ya presentes en el código y las pruebas.
 */
public enum CommercialFabric implements LabeledCatalog {
    SUD_AFRICA("Sudáfrica"),
    PIQUE("Piqué"),
    HYDROTECH("Hydrotech");

    private final String label;

    CommercialFabric(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

    public static CommercialFabric of(String value) {
        return CommercialCatalogParser.parseRequired(CommercialFabric.class, value, "fabric");
    }

    public static String canonicalize(String value) {
        return of(value).label();
    }

    public static List<CommercialFabric> catalog() {
        return Arrays.asList(values());
    }
}
