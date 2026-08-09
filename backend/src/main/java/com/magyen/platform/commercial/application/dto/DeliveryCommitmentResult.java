package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;

/**
 * Representación del compromiso de entrega de una Orden para casos de uso de consulta.
 */
public record DeliveryCommitmentResult(
        LocalDate promisedDeliveryDate,
        String deliveryObservations
) {
}
