package com.magyen.platform.production.application;

import com.magyen.platform.production.domain.ProductionReferenceImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

/**
 * Valida bytes de imagen de referencia: tamaño, MIME, extensión, magic bytes y decodificación.
 */
public final class ProductionReferenceImageInspector {

    public static final int MAX_SIZE_BYTES = 5 * 1024 * 1024;

    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    public record InspectedReferenceImage(byte[] content, String contentType, String extension) {
    }

    public InspectedReferenceImage inspect(byte[] content, String originalFilename, String declaredContentType) {
        Objects.requireNonNull(content, "Image content must not be null");
        if (content.length == 0) {
            throw new IllegalArgumentException("Reference image must not be empty");
        }
        if (content.length > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Reference image must not exceed 5 MB");
        }

        String detectedType = detectContentType(content);
        String declared = normalizeDeclaredContentType(declaredContentType);
        if (!detectedType.equals(declared)) {
            throw new IllegalArgumentException("Reference image content type does not match the file contents");
        }

        String filename = originalFilename == null ? "" : originalFilename.trim();
        if (!filename.isBlank() && !extensionMatches(filename, detectedType)) {
            throw new IllegalArgumentException("Reference image extension does not match the file contents");
        }

        if (!canDecodeImage(content)) {
            throw new IllegalArgumentException("Reference image is not a valid JPEG or PNG");
        }

        return new InspectedReferenceImage(content, detectedType, extensionFor(detectedType));
    }

    private static String detectContentType(byte[] content) {
        if (startsWith(content, JPEG_MAGIC)) {
            return ProductionReferenceImage.JPEG_CONTENT_TYPE;
        }
        if (startsWith(content, PNG_MAGIC)) {
            return ProductionReferenceImage.PNG_CONTENT_TYPE;
        }
        throw new IllegalArgumentException("Reference image must be JPEG or PNG");
    }

    private static String normalizeDeclaredContentType(String declaredContentType) {
        if (declaredContentType == null || declaredContentType.isBlank()) {
            throw new IllegalArgumentException("Reference image content type is required");
        }
        String mime = declaredContentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if ("image/jpg".equals(mime)) {
            mime = ProductionReferenceImage.JPEG_CONTENT_TYPE;
        }
        if (!ProductionReferenceImage.JPEG_CONTENT_TYPE.equals(mime)
                && !ProductionReferenceImage.PNG_CONTENT_TYPE.equals(mime)) {
            throw new IllegalArgumentException("Reference image must be JPEG or PNG");
        }
        return mime;
    }

    private static boolean extensionMatches(String filename, String contentType) {
        String lower = filename.toLowerCase(Locale.ROOT);
        int separator = Math.max(lower.lastIndexOf('/'), lower.lastIndexOf('\\'));
        String simpleName = separator >= 0 ? lower.substring(separator + 1) : lower;
        if (simpleName.contains("..")) {
            throw new IllegalArgumentException("Reference image filename is invalid");
        }
        if (ProductionReferenceImage.JPEG_CONTENT_TYPE.equals(contentType)) {
            return simpleName.endsWith(".jpg") || simpleName.endsWith(".jpeg");
        }
        return simpleName.endsWith(".png");
    }

    private static boolean canDecodeImage(byte[] content) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(content)) {
            BufferedImage image = ImageIO.read(input);
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (IOException exception) {
            return false;
        }
    }

    private static boolean startsWith(byte[] content, byte[] magic) {
        if (content.length < magic.length) {
            return false;
        }
        for (int index = 0; index < magic.length; index++) {
            if (content[index] != magic[index]) {
                return false;
            }
        }
        return true;
    }

    private static String extensionFor(String contentType) {
        return ProductionReferenceImage.PNG_CONTENT_TYPE.equals(contentType) ? "png" : "jpg";
    }
}
