package com.magyen.platform.production.application;

import com.magyen.platform.production.application.dto.ProductionReferenceImageContent;
import com.magyen.platform.production.application.port.ProductionReferenceImageStoragePort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Fake de almacenamiento para tests. No habla con R2.
 */
public class InMemoryProductionReferenceImageStorage implements ProductionReferenceImageStoragePort {

    private final Map<String, ProductionReferenceImageContent> objects = new HashMap<>();
    private RuntimeException putFailure;
    private RuntimeException getFailure;
    private RuntimeException deleteFailure;

    @Override
    public void put(String objectKey, byte[] content, String contentType) {
        if (putFailure != null) {
            throw putFailure;
        }
        objects.put(objectKey, new ProductionReferenceImageContent(content, contentType));
    }

    @Override
    public Optional<ProductionReferenceImageContent> get(String objectKey) {
        if (getFailure != null) {
            throw getFailure;
        }
        return Optional.ofNullable(objects.get(objectKey));
    }

    @Override
    public void delete(String objectKey) {
        if (deleteFailure != null) {
            throw deleteFailure;
        }
        objects.remove(objectKey);
    }

    public void failOnPut(RuntimeException exception) {
        this.putFailure = exception;
    }

    public void failOnGet(RuntimeException exception) {
        this.getFailure = exception;
    }

    public void failOnDelete(RuntimeException exception) {
        this.deleteFailure = exception;
    }

    public boolean contains(String objectKey) {
        return objects.containsKey(objectKey);
    }

    public int size() {
        return objects.size();
    }
}
