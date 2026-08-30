package com.magyen.platform.commercial.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommercialDocumentFilenameTest {

    @Test
    void quotationFilenameUsesBusinessNumberNotUuid() {
        assertEquals("Cotizacion_14.pdf", CommercialDocumentFilename.quotation("14"));
    }

    @Test
    void remissionFilenamePreservesExistingOrderNumber() {
        assertEquals("Remision_13.pdf", CommercialDocumentFilename.remission("13"));
        assertEquals("Remision_PDF-1.pdf", CommercialDocumentFilename.remission("PDF-1"));
    }
}
