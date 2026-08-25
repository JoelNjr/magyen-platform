package com.magyen.platform.production.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de almacenamiento de objetos S3-compatible (Cloudflare R2).
 * <p>
 * Los secretos llegan solo por variables de entorno.
 */
@ConfigurationProperties(prefix = "magyen.object-storage")
public record ObjectStorageProperties(
        String endpoint,
        String region,
        String accessKey,
        String secretKey,
        String bucket,
        String keyPrefix
) {

    public boolean isConfigured() {
        return hasText(accessKey) && hasText(secretKey) && hasText(bucket);
    }

    public String resolvedRegion() {
        return hasText(region) ? region.trim() : "us-east-1";
    }

    public String resolvedKeyPrefix() {
        return hasText(keyPrefix) ? keyPrefix.trim() : "local";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
