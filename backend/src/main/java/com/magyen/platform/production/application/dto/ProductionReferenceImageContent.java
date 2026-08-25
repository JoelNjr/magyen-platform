package com.magyen.platform.production.application.dto;

/**
 * Bytes y tipo de la imagen de referencia recuperada del almacenamiento.
 */
public record ProductionReferenceImageContent(
        byte[] content,
        String contentType
) {
}
