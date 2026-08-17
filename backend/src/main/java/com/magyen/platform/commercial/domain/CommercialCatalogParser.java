package com.magyen.platform.commercial.domain;

/**
 * Resuelve un valor de catálogo comercial a partir del código estable o la etiqueta de negocio.
 */
final class CommercialCatalogParser {

    private CommercialCatalogParser() {
    }

    static <E extends Enum<E> & LabeledCatalog> E parseRequired(Class<E> type, String value, String catalogName) {
        E parsed = parseOptional(type, value, catalogName);
        if (parsed == null) {
            throw new IllegalArgumentException(catalogName + " must not be blank");
        }
        return parsed;
    }

    static <E extends Enum<E> & LabeledCatalog> E parseOptional(Class<E> type, String value, String catalogName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(trimmed) || constant.label().equalsIgnoreCase(trimmed)) {
                return constant;
            }
        }

        throw new IllegalArgumentException("Unsupported " + catalogName + ": " + trimmed);
    }
}
