package com.magyen.platform.production.domain;

/**
 * Representa el ciclo de vida de una Orden de Producción.
 */
public enum ProductionStatus {

    CREATED,
    PLANNED,
    IN_PROGRESS,
    COMPLETED
}
