package com.magyen.platform.commercial.domain;

import java.util.Objects;

/**
 * Especificación comercial tipada del producto comprometido o cotizado.
 * <p>
 * Value Object inmutable. No modela ejecución de producción, consumo de materiales ni costos.
 * Tipo de prenda, cuello, manga y puño son catálogos cerrados. {@code color} de tela/base
 * permanece como texto del ítem, no de esta especificación.
 */
public final class ProductSpecification {

    private final String garmentType;
    private final String collarType;
    private final String sleeveType;
    private final Boolean cuffRequired;
    private final boolean sublimationRequired;
    private final boolean embroideryRequired;
    private final boolean dtfRequired;
    private final String decorationNotes;
    private final boolean includesNames;
    private final boolean includesNumbers;
    private final boolean includesLogos;
    private final String personalizationNotes;
    private final String itemObservations;

    private ProductSpecification(
            String garmentType,
            String collarType,
            String sleeveType,
            Boolean cuffRequired,
            boolean sublimationRequired,
            boolean embroideryRequired,
            boolean dtfRequired,
            String decorationNotes,
            boolean includesNames,
            boolean includesNumbers,
            boolean includesLogos,
            String personalizationNotes,
            String itemObservations
    ) {
        this.garmentType = garmentType;
        this.collarType = collarType;
        this.sleeveType = sleeveType;
        this.cuffRequired = cuffRequired;
        this.sublimationRequired = sublimationRequired;
        this.embroideryRequired = embroideryRequired;
        this.dtfRequired = dtfRequired;
        this.decorationNotes = decorationNotes;
        this.includesNames = includesNames;
        this.includesNumbers = includesNumbers;
        this.includesLogos = includesLogos;
        this.personalizationNotes = personalizationNotes;
        this.itemObservations = itemObservations;
    }

    /**
     * Especificación vacía para ítems históricos o aún no configurados.
     */
    public static ProductSpecification empty() {
        return new ProductSpecification(
                null,
                null,
                null,
                null,
                false,
                false,
                false,
                null,
                false,
                false,
                false,
                null,
                null
        );
    }

    /**
     * Crea una especificación validando los catálogos comerciales cerrados.
     */
    public static ProductSpecification of(
            String garmentType,
            String collarType,
            String sleeveType,
            Boolean cuffRequired,
            boolean sublimationRequired,
            boolean embroideryRequired,
            boolean dtfRequired,
            String decorationNotes,
            boolean includesNames,
            boolean includesNumbers,
            boolean includesLogos,
            String personalizationNotes,
            String itemObservations
    ) {
        return new ProductSpecification(
                GarmentType.canonicalize(garmentType),
                CollarType.canonicalize(collarType),
                SleeveType.canonicalize(sleeveType),
                cuffRequired,
                sublimationRequired,
                embroideryRequired,
                dtfRequired,
                decorationNotes,
                includesNames,
                includesNumbers,
                includesLogos,
                personalizationNotes,
                itemObservations
        );
    }

    /**
     * Reconstruye desde persistencia sin revalidar catálogos, para no romper filas históricas.
     */
    public static ProductSpecification reconstitute(
            String garmentType,
            String collarType,
            String sleeveType,
            Boolean cuffRequired,
            boolean sublimationRequired,
            boolean embroideryRequired,
            boolean dtfRequired,
            String decorationNotes,
            boolean includesNames,
            boolean includesNumbers,
            boolean includesLogos,
            String personalizationNotes,
            String itemObservations
    ) {
        return new ProductSpecification(
                blankToNull(garmentType),
                blankToNull(collarType),
                blankToNull(sleeveType),
                cuffRequired,
                sublimationRequired,
                embroideryRequired,
                dtfRequired,
                decorationNotes,
                includesNames,
                includesNumbers,
                includesLogos,
                personalizationNotes,
                itemObservations
        );
    }

    public String getGarmentType() {
        return garmentType;
    }

    public String getCollarType() {
        return collarType;
    }

    public String getSleeveType() {
        return sleeveType;
    }

    public Boolean getCuffRequired() {
        return cuffRequired;
    }

    public boolean isSublimationRequired() {
        return sublimationRequired;
    }

    public boolean isEmbroideryRequired() {
        return embroideryRequired;
    }

    public boolean isDtfRequired() {
        return dtfRequired;
    }

    public String getDecorationNotes() {
        return decorationNotes;
    }

    public boolean isIncludesNames() {
        return includesNames;
    }

    public boolean isIncludesNumbers() {
        return includesNumbers;
    }

    public boolean isIncludesLogos() {
        return includesLogos;
    }

    public String getPersonalizationNotes() {
        return personalizationNotes;
    }

    public String getItemObservations() {
        return itemObservations;
    }

    public boolean isEmpty() {
        return equals(empty());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ProductSpecification that = (ProductSpecification) other;
        return sublimationRequired == that.sublimationRequired
                && embroideryRequired == that.embroideryRequired
                && dtfRequired == that.dtfRequired
                && includesNames == that.includesNames
                && includesNumbers == that.includesNumbers
                && includesLogos == that.includesLogos
                && Objects.equals(garmentType, that.garmentType)
                && Objects.equals(collarType, that.collarType)
                && Objects.equals(sleeveType, that.sleeveType)
                && Objects.equals(cuffRequired, that.cuffRequired)
                && Objects.equals(decorationNotes, that.decorationNotes)
                && Objects.equals(personalizationNotes, that.personalizationNotes)
                && Objects.equals(itemObservations, that.itemObservations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                garmentType,
                collarType,
                sleeveType,
                cuffRequired,
                sublimationRequired,
                embroideryRequired,
                dtfRequired,
                decorationNotes,
                includesNames,
                includesNumbers,
                includesLogos,
                personalizationNotes,
                itemObservations
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
