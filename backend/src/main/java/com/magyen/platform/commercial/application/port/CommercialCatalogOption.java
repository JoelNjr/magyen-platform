package com.magyen.platform.commercial.application.port;

/**
 * Opción de catálogo comercial consumida por Cotizaciones y Órdenes.
 * {@code value} y {@code label} son el nombre de negocio persistido.
 */
public record CommercialCatalogOption(String value, String label) {
}
