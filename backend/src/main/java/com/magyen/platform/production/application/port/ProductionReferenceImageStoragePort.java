package com.magyen.platform.production.application.port;

import com.magyen.platform.production.application.dto.ProductionReferenceImageContent;

import java.util.Optional;

/**
 * Puerto de almacenamiento de la imagen de referencia de producción.
 * <p>
 * No conoce HTTP ni el agregado. Las claves las genera Application.
 */
public interface ProductionReferenceImageStoragePort {

    void put(String objectKey, byte[] content, String contentType);

    Optional<ProductionReferenceImageContent> get(String objectKey);

    /**
     * Elimina el objeto. Si no existe, se considera éxito.
     */
    void delete(String objectKey);
}
