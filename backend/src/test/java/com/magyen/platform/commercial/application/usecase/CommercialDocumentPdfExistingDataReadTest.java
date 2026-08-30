package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CommercialDocumentPdfResult;
import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetQuotationCommand;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumberFormat;
import com.magyen.platform.commercial.domain.QuotationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Read-only smoke against already persisted Magyen records.
 * Does not insert, update, or delete business rows.
 */
@SpringBootTest
@Transactional
class CommercialDocumentPdfExistingDataReadTest {

    @Autowired
    private GenerateQuotationPdfUseCase generateQuotationPdfUseCase;

    @Autowired
    private GenerateOrderRemissionPdfUseCase generateOrderRemissionPdfUseCase;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void existingQuotationGeneratesReadablePdf() throws Exception {
        List<Quotation> quotations = quotationRepository.findAll();
        Assumptions.assumeFalse(quotations.isEmpty(), "No persisted quotations available");

        Quotation quotation = quotations.getFirst();
        CommercialDocumentPdfResult result = generateQuotationPdfUseCase.execute(
                new GetQuotationCommand(quotation.getId())
        );

        String text = extractText(result.content());
        assertTrue(result.content().length > 100);
        assertTrue("application/pdf".equals(result.contentType()));
        assertTrue(text.contains("COTIZACIÓN"));
        String businessNumber = QuotationNumberFormat.display(quotation.getQuotationNumber());
        if (businessNumber != null) {
            assertTrue(text.contains(businessNumber));
            assertTrue(result.filename().contains(
                    "Cotizacion_" + quotation.getQuotationNumber().getValue() + ".pdf"
            ));
        }
        assertFalse(text.contains(quotation.getId().toString()));
    }

    @Test
    void existingOrderGeneratesReadableRemissionPdf() throws Exception {
        List<Order> orders = orderRepository.findAll();
        Assumptions.assumeFalse(orders.isEmpty(), "No persisted orders available");

        Order order = orders.getFirst();
        CommercialDocumentPdfResult result = generateOrderRemissionPdfUseCase.execute(
                new GetOrderCommand(order.getId())
        );

        String text = extractText(result.content());
        assertTrue(result.content().length > 100);
        assertTrue(text.contains("REMISIÓN"));
        assertTrue(text.contains("Documento de entrega"));
        assertFalse(text.contains("FACTURA"));
        assertTrue(text.contains("Recibido por"));
        assertTrue(text.contains("Firma"));
        if (order.getOrderNumber() != null) {
            String orderNumber = order.getOrderNumber().getValue();
            assertTrue(text.contains(orderNumber));
            assertTrue(result.filename().contains(orderNumber.replace(" ", "-")));
        }
        assertFalse(text.contains(order.getId().toString()));
    }

    private String extractText(byte[] pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            StringBuilder text = new StringBuilder();
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                text.append(extractor.getTextFromPage(page)).append('\n');
            }
            return text.toString();
        } finally {
            reader.close();
        }
    }
}
