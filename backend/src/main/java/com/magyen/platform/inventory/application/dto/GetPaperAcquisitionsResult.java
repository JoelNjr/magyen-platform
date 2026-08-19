package com.magyen.platform.inventory.application.dto;

import java.util.List;

/**
 * Adquisiciones de papel en el período consultado.
 */
public record GetPaperAcquisitionsResult(
        List<PaperAcquisitionItem> acquisitions
) {
}
