package com.magyen.platform.commercial.application.port;

import java.util.List;

/**
 * Puerto de Application para leer catálogos de Administración.
 * <p>
 * Comercial no accede a repositorios de Administración. La tela de catálogo
 * no implica existencia en Inventario.
 */
public interface CommercialCatalogPort {

    List<CommercialCatalogOption> listActiveGarments();

    List<CommercialCatalogOption> listActiveFabrics();

    List<CommercialCatalogOption> listActiveCollars();

    List<CommercialCatalogOption> listActiveSleeves();

    /**
     * Resuelve una prenda activa. {@code null} o blanco es válido (opcional).
     */
    String requireActiveGarment(String value);

    /**
     * Resuelve una tela activa. Obligatoria para productos nuevos.
     */
    String requireActiveFabric(String value);

    /**
     * Resuelve una tela secundaria activa. Blanco se interpreta como ausente.
     */
    String requireActiveSecondaryFabric(String value);

    String requireActiveCollar(String value);

    String requireActiveSleeve(String value);
}
