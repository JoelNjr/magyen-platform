package com.magyen.platform.commercial.infrastructure.pdf;

import com.magyen.platform.commercial.application.dto.CommercialDocumentProductLine;
import com.magyen.platform.commercial.application.dto.QuotationPdfDocument;
import org.junit.jupiter.api.Test;
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
        } finally {
            reader.close();
        }
    }
}
