package com.magyen.platform.commercial.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommercialDocumentFilenameTest {

    @Test
    void quotationFilenameUsesBusinessNumberNotUuid() {
        assertEquals("Cotizacion-C000001.pdf", CommercialDocumentFilename.quotation("C000001"));
    }

    @Test
    void remissionFilenamePreservesExistingOrderNumber() {
        assertEquals("Remision-1.pdf", CommercialDocumentFilename.remission("1"));
        assertEquals("Remision-PDF-1.pdf", CommercialDocumentFilename.remission("PDF-1"));
    }
}
