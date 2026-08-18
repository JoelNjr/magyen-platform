package com.magyen.platform.inventory.domain;

/**
 * Genera códigos de material consecutivos de negocio (MAT-001, MAT-002, ...).
 */
public interface MaterialCodeGenerator {

    MaterialCode nextMaterialCode();
}
