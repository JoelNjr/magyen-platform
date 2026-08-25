package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.util.Objects;

/**
 * Referencia a la imagen operativa asociada a una Orden de Producción.
 * <p>
 * No contiene bytes. El objeto vive en almacenamiento externo.
 */
public final class ProductionReferenceImage {

    public static final String JPEG_CONTENT_TYPE = "image/jpeg";
    public static final String PNG_CONTENT_TYPE = "image/png";

    private final String objectKey;
    private final String contentType;

    private ProductionReferenceImage(String objectKey, String contentType) {
        this.objectKey = objectKey;
        this.contentType = contentType;
    }

    public static ProductionReferenceImage of(String objectKey, String contentType) {
        String normalizedKey = requireSafeObjectKey(objectKey);
        String normalizedType = requireSupportedContentType(contentType);
        return new ProductionReferenceImage(normalizedKey, normalizedType);
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getContentType() {
        return contentType;
    }

    private static String requireSafeObjectKey(String objectKey) {
        Objects.requireNonNull(objectKey, "Reference image object key must not be null");
        String trimmed = objectKey.trim();
        if (trimmed.isBlank()) {
            throw new ProductionDomainException("Reference image object key must not be blank");
        }
        if (trimmed.contains("..") || trimmed.contains("\\") || trimmed.startsWith("/")) {
            throw new ProductionDomainException("Reference image object key is invalid");
        }
        return trimmed;
    }

    private static String requireSupportedContentType(String contentType) {
        Objects.requireNonNull(contentType, "Reference image content type must not be null");
        String normalized = contentType.trim().toLowerCase();
        if (normalized.equals("image/jpg")) {
            normalized = JPEG_CONTENT_TYPE;
        }
        if (!JPEG_CONTENT_TYPE.equals(normalized) && !PNG_CONTENT_TYPE.equals(normalized)) {
            throw new ProductionDomainException("Reference image must be JPEG or PNG");
        }
        return normalized;
    }
}
