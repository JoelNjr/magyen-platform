package com.magyen.platform.production.infrastructure.pdf;

import com.magyen.platform.production.application.dto.ProductionDocumentOperationLine;
import com.magyen.platform.production.application.dto.ProductionDocumentProductLine;
import com.magyen.platform.production.application.dto.ProductionOrderPdfDocument;
import com.magyen.platform.production.application.port.ProductionDocumentPdfPort;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Adaptador OpenPDF para Orden de Producción.
 * <p>
 * No consulta repositorios. No inventa campos. Omite la imagen si no hay bytes válidos.
 */
public class OpenPdfProductionDocumentAdapter implements ProductionDocumentPdfPort {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color INK = new Color(33, 37, 41);
    private static final Color MUTED = new Color(90, 90, 90);
    private static final Color HEADER_FILL = new Color(245, 245, 245);
    private static final Color LINE = new Color(210, 210, 210);
    private static final float REFERENCE_IMAGE_MAX_WIDTH = 420f;
    private static final float REFERENCE_IMAGE_MAX_HEIGHT = 260f;
    private static final Pattern EXTRA_SPECIFICATION_SEPARATOR = Pattern.compile(" {2}· {2}");

    @Override
    public byte[] renderProductionOrder(ProductionOrderPdfDocument document) {
        Objects.requireNonNull(document, "Production order PDF document must not be null");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document pdf = new Document(PageSize.A4, 48, 48, 86, 56);
        try {
            PdfWriter writer = PdfWriter.getInstance(pdf, output);
            writer.setPageEvent(new MagyenDocumentPageEvent(
                    "ORDEN DE PRODUCCIÓN",
                    document.orderNumber() == null ? "" : document.orderNumber()
            ));
            pdf.open();
            buildDocument(pdf, document);
        } catch (DocumentException exception) {
            throw new IllegalStateException("Unable to generate production PDF", exception);
        } finally {
            if (pdf.isOpen()) {
                pdf.close();
            }
        }
        return output.toByteArray();
    }

    private void buildDocument(Document pdf, ProductionOrderPdfDocument document) throws DocumentException {
        addTitle(pdf, "ORDEN DE PRODUCCIÓN");
        Paragraph notice = new Paragraph(
                "Documento operativo de planta. No es una factura.",
                mutedFont(8)
        );
        notice.setSpacingAfter(8);
        pdf.add(notice);

        addMetaTable(pdf, productionMetaRows(document));
        addCustomerSection(pdf, document.customerName());
        addOrderSection(pdf, document);
        addObservations(pdf, document.observations());
        addGarmentCards(pdf, document.lines());
        addOperationsTable(pdf, document.operations());
        addReferenceImage(pdf, document.referenceImage());
    }

    private List<String[]> productionMetaRows(ProductionOrderPdfDocument document) {
        List<String[]> rows = new ArrayList<>();
        rows.add(meta("Pedido comercial", present(document.orderNumber())));
        rows.add(meta("Fecha", formatDate(document.creationDate())));
        rows.add(meta("Estado", present(document.statusLabel())));
        rows.add(meta("Prioridad", present(document.priorityLabel())));
        if (document.plannedStartDate() != null || document.plannedEndDate() != null) {
            rows.add(meta("Inicio planificado", formatDate(document.plannedStartDate())));
            rows.add(meta("Fin planificado", formatDate(document.plannedEndDate())));
        }
        if (document.actualStartDate() != null) {
            rows.add(meta("Inicio real", formatDate(document.actualStartDate())));
        }
        if (document.actualCompletionDate() != null) {
            rows.add(meta("Finalización real", formatDate(document.actualCompletionDate())));
        }
        return rows;
    }

    private void addTitle(Document pdf, String title) throws DocumentException {
        Paragraph brand = new Paragraph("MAGYEN  ·  Confecciones Magyen", mutedFont(8));
        brand.setSpacingAfter(1);
        pdf.add(brand);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, INK);
        Paragraph heading = new Paragraph(title, titleFont);
        heading.setSpacingAfter(6);
        pdf.add(heading);
    }

    private void addMetaTable(Document pdf, List<String[]> rows) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{32, 68});
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);
        table.setKeepTogether(true);
        for (String[] row : rows) {
            table.addCell(compactLabelCell(row[0]));
            table.addCell(compactValueCell(row[1]));
        }
        pdf.add(table);
    }

    private void addCustomerSection(Document pdf, String customerName) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);
        table.setKeepTogether(true);
        table.addCell(compactSectionCell("Cliente", present(customerName)));
        pdf.add(table);
    }

    private void addOrderSection(Document pdf, ProductionOrderPdfDocument document) throws DocumentException {
        if (document.orderDescription() == null || document.orderDescription().isBlank()) {
            return;
        }
        Paragraph label = new Paragraph("Descripción del pedido", boldFont(8));
        label.setSpacingAfter(1);
        pdf.add(label);
        Paragraph value = new Paragraph(document.orderDescription(), mutedFont(8));
        value.setSpacingAfter(8);
        pdf.add(value);
    }

    private void addObservations(Document pdf, String observations) throws DocumentException {
        if (observations == null || observations.isBlank()) {
            return;
        }
        Paragraph label = new Paragraph("Observaciones", boldFont(8));
        label.setSpacingAfter(1);
        pdf.add(label);
        Paragraph value = new Paragraph(observations, mutedFont(8));
        value.setSpacingAfter(8);
        pdf.add(value);
    }

    private void addGarmentCards(
            Document pdf,
            List<ProductionDocumentProductLine> lines
    ) throws DocumentException {
        Paragraph heading = new Paragraph("PRENDAS A FABRICAR", boldFont(16));
        heading.setSpacingBefore(6);
        heading.setSpacingAfter(10);
        pdf.add(heading);

        if (lines == null || lines.isEmpty()) {
            Paragraph empty = new Paragraph("Sin prendas registradas.", bodyFont(12));
            empty.setSpacingAfter(12);
            pdf.add(empty);
            return;
        }

        int total = lines.size();
        int index = 1;
        for (ProductionDocumentProductLine line : lines) {
            addGarmentCard(pdf, line, index, total);
            index++;
        }
    }

    private void addGarmentCard(
            Document pdf,
            ProductionDocumentProductLine line,
            int index,
            int total
    ) throws DocumentException {
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(100);
        card.setSpacingAfter(14);
        card.setSplitLate(false);
        card.setSplitRows(true);

        card.addCell(garmentTitleCell(line.productName(), index, total));
        card.addCell(garmentQuantityCell(line.quantity(), line.sizes()));
        card.addCell(garmentSpecificationsCell(line));
        pdf.add(card);
    }

    private PdfPCell garmentTitleCell(String productName, int index, int total) {
        Paragraph content = new Paragraph();
        if (total > 1) {
            content.add(new Phrase("Prenda " + index + " de " + total + "\n", mutedFont(9)));
        }
        content.add(new Phrase(present(productName), boldFont(16)));
        PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(HEADER_FILL);
        cell.setBorderColor(LINE);
        cell.setPadding(10);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell garmentQuantityCell(int quantity, String sizes) {
        Paragraph content = new Paragraph();
        content.add(new Phrase("Cantidad total\n", mutedFont(9)));
        content.add(new Phrase(String.valueOf(quantity) + "\n", boldFont(18)));
        content.add(new Phrase("\nTallas\n", mutedFont(9)));
        content.add(new Phrase(present(sizes), boldFont(13)));
        PdfPCell cell = new PdfPCell(content);
        cell.setBorderColor(LINE);
        cell.setPadding(10);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        return cell;
    }

    private PdfPCell garmentSpecificationsCell(ProductionDocumentProductLine line) {
        Paragraph content = new Paragraph("ESPECIFICACIONES DE FABRICACIÓN", boldFont(12));
        content.setSpacingAfter(6);

        PdfPTable specs = new PdfPTable(new float[]{34, 66});
        specs.setWidthPercentage(100);
        specs.setSplitLate(false);
        boolean hasSpecification = false;
        hasSpecification |= addSpecificationRow(specs, "Tipo de prenda", line.garmentType());
        hasSpecification |= addSpecificationRow(specs, "Cuello", line.collarType());
        hasSpecification |= addSpecificationRow(specs, "Manga", line.sleeveType());
        hasSpecification |= addSpecificationRow(specs, "Puño", line.cuffLabel());
        hasSpecification |= addExtraSpecificationRows(specs, line.extraSpecifications());
        hasSpecification |= addSpecificationRow(specs, "Observaciones", line.itemObservations());
        if (!hasSpecification) {
            PdfPCell empty = specificationValueCell("Sin especificaciones registradas.");
            empty.setColspan(2);
            specs.addCell(empty);
        }

        PdfPCell cell = new PdfPCell();
        cell.addElement(content);
        cell.addElement(specs);
        cell.setBorderColor(LINE);
        cell.setPadding(10);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        return cell;
    }

    private boolean addExtraSpecificationRows(PdfPTable specs, String extraSpecifications) {
        if (extraSpecifications == null || extraSpecifications.isBlank()) {
            return false;
        }
        boolean added = false;
        for (String part : EXTRA_SPECIFICATION_SEPARATOR.split(extraSpecifications)) {
            String value = part == null ? "" : part.trim();
            if (value.isEmpty()) {
                continue;
            }
            int separator = value.indexOf(':');
            if (separator > 0 && separator < value.length() - 1) {
                added |= addSpecificationRow(specs, value.substring(0, separator).trim(), value.substring(separator + 1).trim());
            } else {
                PdfPCell extra = specificationValueCell(value);
                extra.setColspan(2);
                specs.addCell(extra);
                added = true;
            }
        }
        return added;
    }

    private boolean addSpecificationRow(PdfPTable specs, String label, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        specs.addCell(specificationLabelCell(label));
        specs.addCell(specificationValueCell(value));
        return true;
    }

    private void addOperationsTable(
            Document pdf,
            List<ProductionDocumentOperationLine> operations
    ) throws DocumentException {
        if (operations == null || operations.isEmpty()) {
            return;
        }

        Paragraph label = new Paragraph("Operaciones", boldFont(10));
        label.setSpacingAfter(6);
        pdf.add(label);

        PdfPTable table = new PdfPTable(new float[]{22, 16, 22, 20, 20});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        table.setSplitLate(false);
        table.setSpacingAfter(12);

        table.addCell(headerCell("Operación"));
        table.addCell(headerCell("Estado"));
        table.addCell(headerCell("Operario"));
        table.addCell(headerCell("Inicio plan."));
        table.addCell(headerCell("Fin plan."));

        for (ProductionDocumentOperationLine operation : operations) {
            table.addCell(operationCell(operation));
            table.addCell(valueCell(present(operation.statusLabel())));
            table.addCell(valueCell(present(operation.assignedOperator())));
            table.addCell(valueCell(formatDate(operation.plannedStartDate())));
            table.addCell(valueCell(formatDate(operation.plannedEndDate())));
        }
        pdf.add(table);
    }

    private void addReferenceImage(Document pdf, byte[] referenceImage) throws DocumentException {
        if (referenceImage == null || referenceImage.length == 0) {
            return;
        }

        Image image;
        try {
            image = Image.getInstance(referenceImage);
        } catch (Exception exception) {
            return;
        }

        Paragraph label = new Paragraph("Imagen de referencia", boldFont(10));
        label.setSpacingAfter(6);
        pdf.add(label);

        image.scaleToFit(REFERENCE_IMAGE_MAX_WIDTH, REFERENCE_IMAGE_MAX_HEIGHT);
        image.setAlignment(Element.ALIGN_LEFT);
        image.setSpacingAfter(12);
        pdf.add(image);
    }

    private PdfPCell operationCell(ProductionDocumentOperationLine operation) {
        Paragraph content = new Paragraph();
        content.add(new Phrase(present(operation.typeLabel()), boldFont(9)));
        if (operation.observations() != null && !operation.observations().isBlank()) {
            content.add(new Phrase("\n" + operation.observations(), mutedFont(8)));
        }
        PdfPCell cell = new PdfPCell(content);
        styleValue(cell);
        return cell;
    }

    private PdfPCell compactSectionCell(String title, String value) {
        Paragraph content = new Paragraph();
        content.add(new Phrase(title + "\n", mutedFont(7)));
        content.add(new Phrase(value, bodyFont(9)));
        PdfPCell cell = new PdfPCell(content);
        styleCompact(cell);
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

    private PdfPCell compactLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, mutedFont(7)));
        styleCompact(cell);
        return cell;
    }

    private PdfPCell compactValueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, bodyFont(8)));
        styleCompact(cell);
        return cell;
    }

    private PdfPCell specificationLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, mutedFont(10)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        cell.setPaddingBottom(6);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        return cell;
    }

    private PdfPCell specificationValueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, bodyFont(12)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        cell.setPaddingBottom(6);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, bodyFont(9)));
        styleValue(cell);
        return cell;
    }

    private void styleValue(PdfPCell cell) {
        cell.setBorderColor(LINE);
        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
    }

    private void styleCompact(PdfPCell cell) {
        cell.setBorderColor(LINE);
        cell.setPadding(4);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
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

    private static Font bodyFont(int size) {
        return FontFactory.getFont(FontFactory.HELVETICA, size, INK);
    }

    private static Font boldFont(int size) {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, size, INK);
    }

    private static Font mutedFont(int size) {
        return FontFactory.getFont(FontFactory.HELVETICA, size, MUTED);
    }
}
