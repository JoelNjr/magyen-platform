package com.magyen.platform.inventory.domain.exception;

/**
 * Excepción base para violaciones de reglas de negocio del módulo de inventario.
 */
public class InventoryDomainException extends RuntimeException {

    public InventoryDomainException(String message) {
        super(message);
    }
}
