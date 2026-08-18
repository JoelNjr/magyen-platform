package com.magyen.platform.administration.domain;

import java.util.Locale;

/**
 * Kind explícito de catálogo configurable de Administración.
 * <p>
 * No es un motor genérico de catálogos: solo cubre las cuatro familias V1
 * (prendas, telas, cuellos, mangas).
 */
public enum AdministrationCatalogKind {
    GARMENT,
    FABRIC,
    COLLAR,
    SLEEVE;

    public static AdministrationCatalogKind of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Catalog kind must not be blank");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "GARMENT", "GARMENTS", "PRENDA", "PRENDAS" -> GARMENT;
            case "FABRIC", "FABRICS", "TELA", "TELAS" -> FABRIC;
            case "COLLAR", "COLLARS", "CUELLO", "CUELLOS" -> COLLAR;
            case "SLEEVE", "SLEEVES", "MANGA", "MANGAS" -> SLEEVE;
            default -> throw new IllegalArgumentException("Unsupported catalog kind: " + value.trim());
        };
    }
}
