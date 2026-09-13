package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Aplica el contenido comercial persistido de una cotización a su Orden asociada.
 * <p>
 * No transporta un parche de ítems: la cotización persistida es la fuente de verdad.
 */
public record ApplyQuotationChangesToOrderCommand(UUID quotationId) {
}
