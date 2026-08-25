package com.magyen.platform.shared.infrastructure.pdf;

import org.openpdf.text.DocumentException;
import org.openpdf.text.Image;
import org.openpdf.text.pdf.PdfContentByte;

import java.io.IOException;
import java.io.InputStream;

/**
 * Logo oficial de Magyen para encabezados PDF repetibles.
 * <p>
 * Carga el mismo asset de marca que usa la aplicación. No altera el contenido del documento.
 */
public final class MagyenPdfHeaderLogo {

    static final String CLASSPATH_RESOURCE = "/pdf/magyen-logo.png";
    static final float MAX_SIZE_POINTS = 26f;
    static final float TEXT_GAP_POINTS = 8f;
    private static final float BASELINE_OFFSET_POINTS = 6f;

    private static final byte[] LOGO_BYTES = readClasspathLogo();

    private MagyenPdfHeaderLogo() {
    }

    /**
     * Dibuja el logo a la izquierda del encabezado y devuelve la coordenada X del texto de marca.
     * Si el logo no puede cargarse, deja el encabezado de texto en su posición original.
     */
    public static float drawAtLeft(PdfContentByte canvas, float left, float textBaselineY) {
        Image logo = createImage();
        if (logo == null) {
            return left;
        }
        logo.scaleToFit(MAX_SIZE_POINTS, MAX_SIZE_POINTS);
        logo.setAbsolutePosition(left, textBaselineY - BASELINE_OFFSET_POINTS);
        try {
            canvas.addImage(logo);
        } catch (DocumentException exception) {
            return left;
        }
        return left + logo.getScaledWidth() + TEXT_GAP_POINTS;
    }

    static Image createImage() {
        if (LOGO_BYTES.length == 0) {
            return null;
        }
        try {
            return Image.getInstance(LOGO_BYTES);
        } catch (Exception exception) {
            return null;
        }
    }

    private static byte[] readClasspathLogo() {
        try (InputStream input = MagyenPdfHeaderLogo.class.getResourceAsStream(CLASSPATH_RESOURCE)) {
            if (input == null) {
                return new byte[0];
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            return new byte[0];
        }
    }
}
