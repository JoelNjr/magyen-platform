package com.magyen.platform.inventory.application.dto;

import java.util.List;

/**
 * Adquisiciones de tinta en el período consultado.
 */
public record GetInkAcquisitionsResult(
        List<InkAcquisitionItem> acquisitions
) {
}
