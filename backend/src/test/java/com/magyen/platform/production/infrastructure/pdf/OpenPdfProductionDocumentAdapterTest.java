package com.magyen.platform.production.infrastructure.pdf;

import com.magyen.platform.production.application.dto.ProductionDocumentOperationLine;
import com.magyen.platform.production.application.dto.ProductionDocumentProductLine;
import com.magyen.platform.production.application.dto.ProductionOrderPdfDocument;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenPdfProductionDocumentAdapterTest {

    private static final byte[] PNG_1X1 = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
            0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08,
            0x06, 0x00, 0x00, 0x00, 0x1F, (byte) 0x15, (byte) 0xC4, (byte) 0x89, 0x00, 0x00,
            0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00,
            0x00, 0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00,
            0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, (byte) 0x42, 0x60, (byte) 0x82
    };

    @Test
    void productionOrderPdfShowsOperationalFieldsWithoutTechnicalIdsOrImageSection() throws Exception {
        byte[] pdf = new OpenPdfProductionDocumentAdapter().renderProductionOrder(sampleDocument(null));

        try (PdfReader reader = new PdfReader(pdf)) {
            String text = extractAllText(reader);
            assertTrue(text.contains("ORDEN DE PRODUCCIÓN"));
            assertTrue(text.contains("MAGYEN"));
            assertTrue(text.contains("14"));
            assertTrue(text.contains("Colegio San José"));
            assertTrue(text.contains("PRENDAS A FABRICAR"));
            assertTrue(text.contains("Camiseta institucional"));
            assertTrue(text.contains("Cantidad total"));
            assertTrue(text.contains("20"));
            assertTrue(text.contains("S: 10"));
            assertTrue(text.contains("ESPECIFICACIONES DE FABRICACIÓN"));
            assertTrue(text.contains("Tipo de prenda"));
            assertTrue(text.contains("Camiseta"));
            assertTrue(text.contains("Cuello"));
            assertTrue(text.contains("Redondo"));
            assertTrue(text.contains("Manga"));
            assertTrue(text.contains("Manga corta sisa"));
            assertTrue(text.contains("Puño"));
            assertTrue(text.contains("Sublimación"));
            assertTrue(text.contains("Cuello reforzado"));
            assertTrue(text.contains("Corte"));
            assertFalse(text.contains("Imagen de referencia"));
            assertFalse(text.toLowerCase().contains("uuid"));
            assertTrue(pageHasEmbeddedImage(reader, 1));
        }
    }

    @Test
    void productionOrderPdfIncludesReferenceImageSectionWhenBytesAreValid() throws Exception {
        byte[] pdf = new OpenPdfProductionDocumentAdapter().renderProductionOrder(sampleDocument(PNG_1X1));

        try (PdfReader reader = new PdfReader(pdf)) {
            String text = extractAllText(reader);
            assertTrue(text.contains("Imagen de referencia"));
        }
    }

    @Test
    void productionOrderPdfKeepsMultipleGarmentsVisuallySeparated() throws Exception {
        byte[] pdf = new OpenPdfProductionDocumentAdapter().renderProductionOrder(documentWithLines(List.of(
                garment("Camiseta institucional", 20, "S: 10  ·  M: 10", "Sublimación"),
                garment("Pantalón deportivo", 15, "M: 8  ·  L: 7", "Bordado")
        )));

        try (PdfReader reader = new PdfReader(pdf)) {
            String text = extractAllText(reader);
            assertTrue(text.contains("Prenda 1 de 2"));
            assertTrue(text.contains("Prenda 2 de 2"));
            assertTrue(text.contains("Camiseta institucional"));
            assertTrue(text.contains("Pantalón deportivo"));
            assertTrue(text.contains("Sublimación"));
            assertTrue(text.contains("Bordado"));
            assertTrue(text.contains("S: 10"));
            assertTrue(text.contains("L: 7"));
        }
    }

    @Test
    void productionOrderPdfFlowsManyGarmentsAcrossPagesWithoutLosingContent() throws Exception {
        List<ProductionDocumentProductLine> lines = new ArrayList<>();
        for (int index = 1; index <= 8; index++) {
            lines.add(garment(
                    "Prenda operativa " + index,
                    index * 5,
                    "S: " + index + "  ·  M: " + (index + 2),
                    "Sublimación  ·  Bordado  ·  DTF  ·  Personalización: nombres del grupo " + index
            ));
        }

        byte[] pdf = new OpenPdfProductionDocumentAdapter().renderProductionOrder(documentWithLines(lines));

        try (PdfReader reader = new PdfReader(pdf)) {
            assertTrue(reader.getNumberOfPages() > 1);
            String text = extractAllText(reader);
            for (int index = 1; index <= 8; index++) {
                assertTrue(text.contains("Prenda operativa " + index));
                assertTrue(text.contains("nombres del grupo " + index));
            }
            assertTrue(text.contains("Página"));
            assertTrue(text.contains("ORDEN DE PRODUCCIÓN"));
            assertTrue(text.contains("MAGYEN"));
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                assertTrue(pageHasEmbeddedImage(reader, page));
            }
        }
    }

    private static boolean pageHasEmbeddedImage(PdfReader reader, int pageNumber) {
        PdfDictionary page = reader.getPageN(pageNumber);
        PdfDictionary resources = page.getAsDict(PdfName.RESOURCES);
        if (resources == null) {
            return false;
        }
        PdfDictionary xObjects = resources.getAsDict(PdfName.XOBJECT);
        return xObjects != null && !xObjects.getKeys().isEmpty();
    }

    private static String extractAllText(PdfReader reader) throws Exception {
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        StringBuilder text = new StringBuilder();
        int pageCount = reader.getNumberOfPages();
        for (int page = 1; page <= pageCount; page++) {
            text.append(extractor.getTextFromPage(page));
            text.append('\n');
        }
        return text.toString();
    }

    private static ProductionOrderPdfDocument sampleDocument(byte[] referenceImage) {
        return documentWithLines(
                List.of(new ProductionDocumentProductLine(
                        "Camiseta institucional",
                        20,
                        "S: 10  ·  M: 10",
                        "Camiseta",
                        "Redondo",
                        "Manga corta sisa",
                        "No",
                        "Sublimación",
                        "Cuello reforzado"
                )),
                referenceImage
        );
    }

    private static ProductionOrderPdfDocument documentWithLines(List<ProductionDocumentProductLine> lines) {
        return documentWithLines(lines, null);
    }

    private static ProductionOrderPdfDocument documentWithLines(
            List<ProductionDocumentProductLine> lines,
            byte[] referenceImage
    ) {
        return new ProductionOrderPdfDocument(
                "14",
                "Uniformes institucionales 2026",
                "Colegio San José",
                LocalDate.of(2026, 8, 20),
                "Creada",
                "Normal",
                LocalDate.of(2026, 8, 21),
                LocalDate.of(2026, 8, 30),
                null,
                null,
                "Entregar con etiqueta interior.",
                lines,
                List.of(new ProductionDocumentOperationLine(
                        "Corte",
                        "Pendiente",
                        "Ana",
                        LocalDate.of(2026, 8, 21),
                        LocalDate.of(2026, 8, 22),
                        null
                )),
                referenceImage
        );
    }

    private static ProductionDocumentProductLine garment(
            String productName,
            int quantity,
            String sizes,
            String extraSpecifications
    ) {
        return new ProductionDocumentProductLine(
                productName,
                quantity,
                sizes,
                "Camiseta",
                "Redondo",
                "Manga corta sisa",
                "No",
                extraSpecifications,
                "Nota de planta"
        );
    }
}
