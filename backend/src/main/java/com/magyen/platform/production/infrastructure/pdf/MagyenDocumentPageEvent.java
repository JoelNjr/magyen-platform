package com.magyen.platform.production.infrastructure.pdf;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfTemplate;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;

/**
 * Encabezado y pie repetibles. Numera páginas sin mostrar identidades técnicas.
 */
final class MagyenDocumentPageEvent extends PdfPageEventHelper {

    private static final Color INK = new Color(33, 37, 41);
    private static final Color MUTED = new Color(90, 90, 90);

    private final String documentTitle;
    private final String documentNumber;
    private PdfTemplate totalPagesTemplate;

    MagyenDocumentPageEvent(String documentTitle, String documentNumber) {
        this.documentTitle = documentTitle;
        this.documentNumber = documentNumber == null ? "" : documentNumber;
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        totalPagesTemplate = writer.getDirectContent().createTemplate(28, 12);
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte canvas = writer.getDirectContent();
        Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, INK);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, MUTED);

        float left = document.left();
        float right = document.right();
        float headerY = document.top() + 28;
        float footerY = document.bottom() - 28;

        ColumnText.showTextAligned(
                canvas,
                Element.ALIGN_LEFT,
                new Phrase("MAGYEN  ·  Confecciones Magyen", brandFont),
                left,
                headerY,
                0
        );
        ColumnText.showTextAligned(
                canvas,
                Element.ALIGN_RIGHT,
                new Phrase((documentTitle + "  " + documentNumber).trim(), metaFont),
                right,
                headerY,
                0
        );

        canvas.setColorStroke(new Color(200, 200, 200));
        canvas.setLineWidth(0.6f);
        canvas.moveTo(left, headerY - 8);
        canvas.lineTo(right, headerY - 8);
        canvas.stroke();
        canvas.moveTo(left, footerY + 12);
        canvas.lineTo(right, footerY + 12);
        canvas.stroke();

        String pageLabel = "Página " + writer.getPageNumber() + " de ";
        ColumnText.showTextAligned(
                canvas,
                Element.ALIGN_RIGHT,
                new Phrase(pageLabel, metaFont),
                right - 18,
                footerY,
                0
        );
        canvas.addTemplate(totalPagesTemplate, right - 16, footerY);
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, MUTED);
        ColumnText.showTextAligned(
                totalPagesTemplate,
                Element.ALIGN_LEFT,
                new Phrase(String.valueOf(writer.getPageNumber() - 1), metaFont),
                0,
                0,
                0
        );
    }
}
