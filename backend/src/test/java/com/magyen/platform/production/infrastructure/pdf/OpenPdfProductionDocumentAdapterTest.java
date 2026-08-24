package com.magyen.platform.production.infrastructure.pdf;

import com.magyen.platform.production.application.dto.ProductionDocumentOperationLine;
import com.magyen.platform.production.application.dto.ProductionDocumentProductLine;
import com.magyen.platform.production.application.dto.ProductionOrderPdfDocument;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.time.LocalDate;
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
            0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    @Test
    void productionOrderPdfShowsOperationalFieldsWithoutTechnicalIdsOrImageSection() throws Exception {
        byte[] pdf = new OpenPdfProductionDocumentAdapter().renderProductionOrder(sampleDocument(null));

        PdfReader reader = new PdfReader(pdf);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            String firstPage = extractor.getTextFromPage(1);
            assertTrue(firstPage.contains("ORDEN DE PRODUCCIÓN"));
            assertTrue(firstPage.contains("MAGYEN"));
            assertTrue(firstPage.contains("14"));
            assertTrue(firstPage.contains("Colegio San José"));
            assertTrue(firstPage.contains("Camiseta institucional"));
            assertTrue(firstPage.contains("S: 10"));
            assertTrue(firstPage.contains("Sublimación"));
            assertTrue(firstPage.contains("Corte"));
            assertFalse(firstPage.contains("Imagen de referencia"));
            assertFalse(firstPage.toLowerCase().contains("uuid"));
        } finally {
            reader.close();
        }
    }

    @Test
    void productionOrderPdfIncludesReferenceImageSectionWhenBytesAreValid() throws Exception {
        byte[] pdf = new OpenPdfProductionDocumentAdapter().renderProductionOrder(sampleDocument(PNG_1X1));

        PdfReader reader = new PdfReader(pdf);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            String firstPage = extractor.getTextFromPage(1);
            assertTrue(firstPage.contains("Imagen de referencia"));
        } finally {
            reader.close();
        }
    }

    private static ProductionOrderPdfDocument sampleDocument(byte[] referenceImage) {
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
}
