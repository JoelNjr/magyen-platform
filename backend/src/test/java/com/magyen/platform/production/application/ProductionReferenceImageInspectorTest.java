package com.magyen.platform.production.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionReferenceImageInspectorTest {

    private final ProductionReferenceImageInspector inspector = new ProductionReferenceImageInspector();

    @Test
    void acceptsValidJpeg() throws Exception {
        var inspected = inspector.inspect(ProductionReferenceImageFixtures.jpegBytes(), "mockup.jpg", "image/jpeg");
        assertEquals("image/jpeg", inspected.contentType());
        assertEquals("jpg", inspected.extension());
    }

    @Test
    void acceptsValidPng() throws Exception {
        var inspected = inspector.inspect(ProductionReferenceImageFixtures.pngBytes(), "referencia.png", "image/png");
        assertEquals("image/png", inspected.contentType());
        assertEquals("png", inspected.extension());
    }

    @Test
    void rejectsUnsupportedFormat() {
        byte[] gif = "GIF89a".getBytes();
        assertThrows(
                IllegalArgumentException.class,
                () -> inspector.inspect(gif, "archivo.gif", "image/gif")
        );
    }

    @Test
    void rejectsOversizedFile() {
        byte[] oversized = new byte[ProductionReferenceImageInspector.MAX_SIZE_BYTES + 1];
        oversized[0] = (byte) 0xFF;
        oversized[1] = (byte) 0xD8;
        oversized[2] = (byte) 0xFF;
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inspector.inspect(oversized, "grande.jpg", "image/jpeg")
        );
        assertEquals("Reference image must not exceed 5 MB", exception.getMessage());
    }

    @Test
    void rejectsMagicBytesThatAreNotADecodableImage() {
        byte[] fakePng = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x01, 0x02, 0x03
        };
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inspector.inspect(fakePng, "roto.png", "image/png")
        );
        assertEquals("Reference image is not a valid JPEG or PNG", exception.getMessage());
    }

    @Test
    void rejectsExtensionThatDoesNotMatchContents() throws Exception {
        assertThrows(
                IllegalArgumentException.class,
                () -> inspector.inspect(ProductionReferenceImageFixtures.jpegBytes(), "foto.png", "image/jpeg")
        );
    }
}
