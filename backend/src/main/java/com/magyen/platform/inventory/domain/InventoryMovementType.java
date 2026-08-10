package com.magyen.platform.inventory.domain;

/**
 * Tipo de movimiento de inventario.
 */
public enum InventoryMovementType {

    /**
     * Entrada de material. Incrementa el stock con cantidad positiva.
     */
    IN,

    /**
     * Salida de material. Decrementa el stock con cantidad positiva sin permitir negativos.
     */
    OUT,

    /**
     * Corrección controlada de existencias.
     * <p>
     * La cantidad es un delta con signo: positivo aumenta stock, negativo lo reduce.
     * El stock resultante nunca puede ser negativo.
     */
    ADJUSTMENT
}
