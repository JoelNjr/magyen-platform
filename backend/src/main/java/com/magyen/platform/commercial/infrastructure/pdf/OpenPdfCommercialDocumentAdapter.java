package com.magyen.platform.commercial.infrastructure.pdf;

import com.magyen.platform.commercial.application.dto.CommercialDocumentProductLine;
import com.magyen.platform.commercial.application.dto.QuotationPdfDocument;
import com.magyen.platform.commercial.application.dto.RemissionPdfDocument;
import com.magyen.platform.commercial.application.port.CommercialDocumentPdfPort;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Adaptador OpenPDF para cotización y remisión.
 * <p>
 * No consulta repositorios. No recalcula totales de negocio.
 */
public class OpenPdfCommercialDocumentAdapter implements CommercialDocumentPdfPort {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color INK = new Color(33, 37, 41);
    private static final Color MUTED = new Color(90, 90, 90);
    private static final Color HEADER_FILL = new Color(245, 245, 245);
    private static final Color LINE = new Color(210, 210, 210);

    @Override
    public byte[] renderQuotation(QuotationPdfDocument document) {
        Objects.requireNonNull(document, "Quotation PDF document must not be null");
        return render(
                "COTIZACIÓN",
                document.quotationNumberDisplay(),
                writer -> buildQuotation(writer, document)
        );
    }

    @Override
    public byte[] renderRemission(RemissionPdfDocument document) {
        Objects.requireNonNull(document, "Remission PDF document must not be null");
        return render(
                "REMISIÓN",
                document.orderNumber(),
                writer -> buildRemission(writer, document)
        );
    }

    private byte[] render(String title, String documentNumber, DocumentBody body) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document pdf = new Document(PageSize.A4, 48, 48, 86, 56);
        try {
            PdfWriter writer = PdfWriter.getInstance(pdf, output);
            writer.setPageEvent(new MagyenDocumentPageEvent(title, documentNumber == null ? "" : documentNumber));
            pdf.open();
            body.write(pdf);
        } catch (DocumentException exception) {
            throw new IllegalStateException("Unable to generate commercial PDF", exception);
        } finally {
            if (pdf.isOpen()) {
                pdf.close();
            }
        }
        return output.toByteArray();
    }

    private void buildQuotation(Document pdf, QuotationPdfDocument document) throws DocumentException {
        addTitle(pdf, "COTIZACIÓN");
        addMetaTable(pdf, List.of(
                meta("Número", present(document.quotationNumberDisplay())),
                meta("Fecha", formatDate(document.quotationDate())),
                meta("Entrega estimada", formatDate(document.deliveryDate()))
        ));
        addPartySection(pdf, document.customerName(), document.sellerName());
        addObservations(pdf, document.observations());
        addProductTable(pdf, document.lines());
        addQuotationTotals(pdf, document);
    }

    private void buildRemission(Document pdf, RemissionPdfDocument document) throws DocumentException {
        addTitle(pdf, "REMISIÓN");
        Paragraph notice = new Paragraph(
                "Documento de entrega. No es una factura.",
                mutedFont(9)
        );
        notice.setSpacingAfter(10);
        pdf.add(notice);

        addMetaTable(pdf, List.of(
                meta("Pedido", present(document.orderNumber())),
                meta("Descripción", present(document.description())),
                meta("Fecha de confirmación", formatDate(document.confirmationDate())),
                meta("Fecha de entrega", formatDate(document.promisedDeliveryDate()))
        ));
        addPartySection(pdf, document.customerName(), document.sellerName());
        addObservations(pdf, document.observations());
        addProductTable(pdf, document.lines());
        addTotals(pdf, document.totalAmount(), document.collectedAmount(), document.outstandingAmount());
        addReceiptFields(pdf);
    }

    private void addTitle(Document pdf, String title) throws DocumentException {
        Paragraph brand = new Paragraph("MAGYEN  ·  Confecciones Magyen", mutedFont(10));
        brand.setSpacingAfter(2);
        pdf.add(brand);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, INK);
        Paragraph heading = new Paragraph(title, titleFont);
        heading.setSpacingAfter(12);
        pdf.add(heading);
    }

    private void addMetaTable(Document pdf, List<String[]> rows) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{32, 68});
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);
        table.setKeepTogether(true);
        for (String[] row : rows) {
            table.addCell(labelCell(row[0]));
            table.addCell(valueCell(row[1]));
        }
        pdf.add(table);
    }

    private void addPartySection(Document pdf, String customerName, String sellerName) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);
        table.setKeepTogether(true);
        table.addCell(sectionCell("Cliente", present(customerName)));
        table.addCell(sectionCell("Vendedor", present(sellerName)));
        pdf.add(table);
    }

    private void addObservations(Document pdf, String observations) throws DocumentException {
        if (observations == null || observations.isBlank()) {
            return;
        }
        Paragraph label = new Paragraph("Observaciones", boldFont(9));
        label.setSpacingAfter(2);
        pdf.add(label);
        Paragraph value = new Paragraph(observations, bodyFont(9));
        value.setSpacingAfter(12);
        pdf.add(value);
    }

    private void addProductTable(
            Document pdf,
            List<CommercialDocumentProductLine> lines
    ) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{48, 10, 21, 21});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        table.setSplitLate(false);
        table.setSpacingAfter(12);

        table.addCell(headerCell("Producto"));
        table.addCell(headerCell("Cant."));
        table.addCell(headerCell("Vr. unitario"));
        table.addCell(headerCell("Total"));

        if (lines == null || lines.isEmpty()) {
            PdfPCell empty = valueCell("Sin productos registrados.");
            empty.setColspan(4);
            table.addCell(empty);
        } else {
            for (CommercialDocumentProductLine line : lines) {
                table.addCell(productCell(line));
                table.addCell(alignCell(String.valueOf(line.quantity()), Element.ALIGN_CENTER));
                table.addCell(alignCell(formatMoney(line.unitPrice()), Element.ALIGN_RIGHT));
                table.addCell(alignCell(formatMoney(line.lineTotal()), Element.ALIGN_RIGHT));
            }
        }
        pdf.add(table);
    }

    private void addQuotationTotals(Document pdf, QuotationPdfDocument document) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(45);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setKeepTogether(true);
        table.setSpacingAfter(16);
        table.addCell(labelCell("Subtotal"));
        table.addCell(alignCell(formatMoney(document.subtotalAmount()), Element.ALIGN_RIGHT));
        if (document.discountAmount() != null
                && document.discountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            table.addCell(labelCell("Descuento"));
            table.addCell(alignCell(formatMoney(document.discountAmount()), Element.ALIGN_RIGHT));
        }
        table.addCell(labelCell("Total"));
        table.addCell(alignCell(formatMoney(document.totalAmount()), Element.ALIGN_RIGHT));
        pdf.add(table);
    }

    private void addTotals(
            Document pdf,
            BigDecimal totalAmount,
            BigDecimal collectedAmount,
            BigDecimal outstandingAmount
    ) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(45);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setKeepTogether(true);
        table.setSpacingAfter(16);
        table.addCell(labelCell("Total"));
        table.addCell(alignCell(formatMoney(totalAmount), Element.ALIGN_RIGHT));
        if (collectedAmount != null) {
            table.addCell(labelCell("Total pagado"));
            table.addCell(alignCell(formatMoney(collectedAmount), Element.ALIGN_RIGHT));
        }
        if (outstandingAmount != null) {
            table.addCell(labelCell("Saldo pendiente"));
            table.addCell(alignCell(formatMoney(outstandingAmount), Element.ALIGN_RIGHT));
        }
        pdf.add(table);
    }

    private void addReceiptFields(Document pdf) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setKeepTogether(true);
        table.setSpacingBefore(8);
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(LINE);
        cell.setPadding(12);
        cell.addElement(new Paragraph("Confirmación de recibido", boldFont(10)));
        cell.addElement(spacer("El cliente declara haber recibido los productos listados en esta remisión."));
        cell.addElement(spacer(" "));
        cell.addElement(spacer("Recibido por: ______________________________________________"));
        cell.addElement(spacer(" "));
        cell.addElement(spacer("Fecha de entrega: __________________________________________"));
        cell.addElement(spacer(" "));
        cell.addElement(spacer("Firma: ____________________________________________________"));
        table.addCell(cell);
        pdf.add(table);
    }

    private PdfPCell productCell(CommercialDocumentProductLine line) {
        Paragraph content = new Paragraph();
        content.add(new Phrase(present(line.productName()), boldFont(9)));
        appendDetail(content, "Tipo", line.garmentType());
        appendDetail(content, "Descripción", line.description());
        appendDetail(content, "Tallas", line.sizes());
        appendDetail(content, "Tela principal", line.mainFabric());
        appendDetail(content, "Tela secundaria", line.secondaryFabric());
        appendDetail(content, "Color", line.color());
        appendDetail(content, "Cuello", line.collarType());
        appendDetail(content, "Manga", line.sleeveType());
        appendDetail(content, "Puño", line.cuffLabel());
        if (line.extraSpecifications() != null) {
            content.add(new Phrase("\n" + line.extraSpecifications(), mutedFont(8)));
        }
        PdfPCell cell = new PdfPCell(content);
        styleValue(cell);
        return cell;
    }

    private void appendDetail(Paragraph content, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        content.add(new Phrase("\n" + label + ": " + value, mutedFont(8)));
    }

    private PdfPCell sectionCell(String title, String value) {
        Paragraph content = new Paragraph();
        content.add(new Phrase(title + "\n", boldFont(8)));
        content.add(new Phrase(value, bodyFont(10)));
        PdfPCell cell = new PdfPCell(content);
        styleValue(cell);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, boldFont(8)));
        cell.setBackgroundColor(HEADER_FILL);
        cell.setBorderColor(LINE);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private PdfPCell labelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, mutedFont(8)));
        styleValue(cell);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, bodyFont(9)));
        styleValue(cell);
        return cell;
    }

    private PdfPCell alignCell(String text, int alignment) {
        PdfPCell cell = valueCell(text);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private void styleValue(PdfPCell cell) {
        cell.setBorderColor(LINE);
        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
    }

    private Paragraph spacer(String text) {
        Paragraph paragraph = new Paragraph(text, bodyFont(9));
        paragraph.setSpacingAfter(4);
        return paragraph;
    }

    private static String[] meta(String label, String value) {
        return new String[]{label, value};
    }

    private static String present(String value) {
        if (value == null || value.isBlank()) {
            return "No registrado";
        }
        return value;
    }

    private static String formatDate(LocalDate date) {
        if (date == null) {
            return "No registrado";
        }
        return DATE_FORMAT.format(date);
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "No registrado";
        }
        NumberFormat moneyFormat = NumberFormat.getNumberInstance(Locale.of("es", "CO"));
        moneyFormat.setMinimumFractionDigits(2);
        moneyFormat.setMaximumFractionDigits(2);
        return "$ " + moneyFormat.format(amount);
    }

    private static Font bodyFont(int size) {
        return FontFactory.getFont(FontFactory.HELVETICA, size, INK);
    }

    private static Font boldFont(int size) {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, size, INK);
    }

    private static Font mutedFont(int size) {
        return FontFactory.getFont(FontFactory.HELVETICA, size, MUTED);
    }

    @FunctionalInterface
    private interface DocumentBody {
        void write(Document document) throws DocumentException;
    }
}
