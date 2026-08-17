package com.magyen.platform.commercial.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommercialCatalogTest {

    @Test
    void garmentTypeAcceptsOnlyTheSixDefinedValues() {
        assertEquals("Camiseta", GarmentType.canonicalize("Camiseta"));
        assertEquals("Camiseta tipo polo", GarmentType.canonicalize("CAMISETA_TIPO_POLO"));
        assertEquals("Conjunto deportivo", GarmentType.canonicalize("Conjunto deportivo"));
        assertEquals("Conjunto de presentación", GarmentType.canonicalize("Conjunto de presentación"));
        assertEquals("Pantaloneta", GarmentType.canonicalize("Pantaloneta"));
        assertEquals("Otro", GarmentType.canonicalize("Otro"));
        assertNull(GarmentType.canonicalize(null));
        assertThrows(IllegalArgumentException.class, () -> GarmentType.canonicalize("Camiseta deportiva"));
    }

    @Test
    void collarTypeAcceptsOnlyTheFourDefinedValues() {
        assertEquals("Redondo", CollarType.canonicalize("Redondo"));
        assertEquals("En V recto", CollarType.canonicalize("En V recto"));
        assertEquals("En V cruzado", CollarType.canonicalize("En V cruzado"));
        assertEquals("Tejido", CollarType.canonicalize("Tejido"));
        assertThrows(IllegalArgumentException.class, () -> CollarType.canonicalize("Mao"));
    }

    @Test
    void sleeveTypeAcceptsOnlyTheFourDefinedValues() {
        assertEquals("Manga corta sisa", SleeveType.canonicalize("Manga corta sisa"));
        assertEquals("Manga corta rangla", SleeveType.canonicalize("Manga corta rangla"));
        assertEquals("Manga larga sisa", SleeveType.canonicalize("Manga larga sisa"));
        assertEquals("Manga larga rangla", SleeveType.canonicalize("Manga larga rangla"));
        assertThrows(IllegalArgumentException.class, () -> SleeveType.canonicalize("Corta"));
    }

    @Test
    void fabricAcceptsOnlyKnownCatalogValuesAndRejectsFreeText() {
        assertEquals("Sudáfrica", CommercialFabric.canonicalize("Sudáfrica"));
        assertEquals("Piqué", CommercialFabric.canonicalize("Piqué"));
        assertEquals("Hydrotech", CommercialFabric.canonicalize("Hydrotech"));
        assertThrows(IllegalArgumentException.class, () -> CommercialFabric.canonicalize("Algodón"));
        assertThrows(IllegalArgumentException.class, () -> CommercialFabric.canonicalize("Tela inventada"));
    }

    @Test
    void productSpecificationCanonicalizesCatalogsAndPersistsCuffAsBoolean() {
        ProductSpecification specification = ProductSpecification.of(
                "Camiseta",
                "Redondo",
                "Manga corta sisa",
                true,
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

        assertEquals("Camiseta", specification.getGarmentType());
        assertEquals("Redondo", specification.getCollarType());
        assertEquals("Manga corta sisa", specification.getSleeveType());
        assertEquals(Boolean.TRUE, specification.getCuffRequired());
    }

    @Test
    void productSpecificationReconstituteKeepsHistoricalUnknownValues() {
        ProductSpecification historical = ProductSpecification.reconstitute(
                "Camiseta deportiva",
                "Cuello redondo",
                "Corta",
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

        assertEquals("Camiseta deportiva", historical.getGarmentType());
        assertEquals("Cuello redondo", historical.getCollarType());
        assertEquals("Corta", historical.getSleeveType());
        assertNull(historical.getCuffRequired());
    }

    @Test
    void quotationNumberDisplayUsesBusinessIdentifier() {
        assertEquals("C000001", QuotationNumberFormat.display(1L));
        assertEquals("C000001", QuotationNumberFormat.display(QuotationNumber.of(1)));
        assertNull(QuotationNumberFormat.display((Long) null));
    }
}
