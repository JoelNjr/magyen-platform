package com.magyen.platform.production.infrastructure.storage;

import com.magyen.platform.production.application.dto.ProductionReferenceImageContent;
import com.magyen.platform.production.application.port.ProductionReferenceImageStoragePort;

import java.util.Optional;

/**
 * Puerto cuando MAGYEN_OBJECT_STORAGE_* no está configurado.
 * <p>
 * get devuelve vacío para no romper el PDF. put/delete fallan de forma explícita.
 */
public class UnconfiguredProductionReferenceImageStorageAdapter implements ProductionReferenceImageStoragePort {

    @Override
    public void put(String objectKey, byte[] content, String contentType) {
        throw new IllegalStateException(
                "Object storage is not configured. Set MAGYEN_OBJECT_STORAGE_ACCESS_KEY, "
                        + "MAGYEN_OBJECT_STORAGE_SECRET_KEY and MAGYEN_OBJECT_STORAGE_BUCKET."
        );
    }

    @Override
    public Optional<ProductionReferenceImageContent> get(String objectKey) {
        return Optional.empty();
    }

    @Override
    public void delete(String objectKey) {
        throw new IllegalStateException(
                "Object storage is not configured. Set MAGYEN_OBJECT_STORAGE_ACCESS_KEY, "
                        + "MAGYEN_OBJECT_STORAGE_SECRET_KEY and MAGYEN_OBJECT_STORAGE_BUCKET."
        );
    }
}
