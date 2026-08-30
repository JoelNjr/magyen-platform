package com.magyen.platform.commercial.infrastructure.pdf;

import com.magyen.platform.commercial.application.dto.CommercialDocumentProductLine;
import com.magyen.platform.commercial.application.dto.QuotationPdfDocument;
import com.magyen.platform.commercial.application.dto.RemissionPdfDocument;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenPdfCommercialDocumentAdapterTest {

    @Test
    void multiPageQuotationRepeatsHeadersAndKeepsTotalsReadable() throws Exception {
        List<CommercialDocumentProductLine> lines = new ArrayList<>();
        for (int index = 1; index <= 28; index++) {
            lines.add(new CommercialDocumentProductLine(
                    "Prenda PDF " + index,
                    "Camiseta",
                    "Descripción " + index,
                    2,
                    null,
                    "Sudáfrica",
                    null,
                    "Blanco",
                    "Redondo",
                    "Manga corta sisa",
                    "No",
                    null,
                    new BigDecimal("25000.00"),
                    new BigDecimal("50000.00")
            ));
        }

        byte[] pdf = new OpenPdfCommercialDocumentAdapter().renderQuotation(new QuotationPdfDocument(
                "C009999",
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 9, 1),
                "Cliente PDF multipágina",
                "Vendedor PDF",
                null,
                lines,
                new BigDecimal("1400000.00"),
                BigDecimal.ZERO,
                new BigDecimal("1400000.00")
        ));

        PdfReader reader = new PdfReader(pdf);
        try {
            assertTrue(reader.getNumberOfPages() > 1);
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            String firstPage = extractor.getTextFromPage(1);
            String lastPage = extractor.getTextFromPage(reader.getNumberOfPages());
            assertTrue(firstPage.contains("COTIZACIÓN"));
            assertTrue(firstPage.contains("C009999"));
            assertTrue(firstPage.contains("Prenda PDF 1"));
            assertTrue(lastPage.contains("Prenda PDF 28") || firstPage.contains("Prenda PDF 28"));
            assertTrue(lastPage.contains("1.400.000") || lastPage.contains("1400000"));
            assertTrue(firstPage.contains("MAGYEN"));
            assertTrue(firstPage.contains("COTIZACIÓN"));
            assertTrue(pageHasEmbeddedImage(reader, 1));
            assertTrue(pageHasEmbeddedImage(reader, reader.getNumberOfPages()));
        } finally {
            reader.close();
        }
    }

    @Test
    void remissionPdfIncludesHeaderLogoWithoutChangingDocumentCopy() throws Exception {
        byte[] pdf = new OpenPdfCommercialDocumentAdapter().renderRemission(new RemissionPdfDocument(
                "14",
                "Uniformes institucionales",
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 30),
                "Colegio San José",
                "Ana",
                null,
                List.of(new CommercialDocumentProductLine(
                        "Camiseta institucional",
                        "Camiseta",
                        "Uniforme",
                        10,
                        "S: 10",
                        null,
                        null,
                        "Blanco",
                        "Redondo",
                        "Manga corta sisa",
                        "No",
                        null,
                        new BigDecimal("25000.00"),
                        new BigDecimal("250000.00")
                )),
                new BigDecimal("250000.00"),
                BigDecimal.ZERO,
                new BigDecimal("250000.00")
        ));

        try (PdfReader reader = new PdfReader(pdf)) {
            String text = new PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(text.contains("REMISIÓN"));
            assertTrue(text.contains("MAGYEN"));
            assertTrue(text.contains("14"));
            assertTrue(pageHasEmbeddedImage(reader, 1));
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
}
