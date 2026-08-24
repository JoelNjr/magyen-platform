package com.magyen.platform.production.application.port;

import com.magyen.platform.production.application.dto.ProductionOrderPdfDocument;

/**
 * Puerto de generación del PDF de Orden de Producción.
 * <p>
 * Recibe un modelo de lectura ya ensamblado. No consulta repositorios.
 */
public interface ProductionDocumentPdfPort {

    byte[] renderProductionOrder(ProductionOrderPdfDocument document);
}
