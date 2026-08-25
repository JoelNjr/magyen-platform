package com.magyen.platform.production.application;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Genera claves de objeto seguras. Nunca reutiliza el nombre enviado por el cliente.
 */
public final class ProductionReferenceImageObjectKeys {

    private ProductionReferenceImageObjectKeys() {
    }

    public static String next(String prefix, UUID productionOrderId, String extension) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        String safePrefix = sanitizePrefix(prefix);
        String safeExtension = sanitizeExtension(extension);
        return safePrefix + "/production-orders/" + productionOrderId + "/" + UUID.randomUUID() + "." + safeExtension;
    }

    private static String sanitizePrefix(String prefix) {
        String value = prefix == null || prefix.isBlank() ? "local" : prefix.trim();
        if (value.contains("..") || value.contains("\\") || value.startsWith("/")) {
            throw new IllegalArgumentException("Object storage key prefix is invalid");
        }
        return value.replaceAll("[^a-zA-Z0-9/_-]", "-");
    }

    private static String sanitizeExtension(String extension) {
        String value = extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
        if (!"jpg".equals(value) && !"png".equals(value)) {
            throw new IllegalArgumentException("Reference image extension is invalid");
        }
        return value;
    }
}
