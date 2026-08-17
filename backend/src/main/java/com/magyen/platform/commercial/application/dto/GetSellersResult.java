package com.magyen.platform.commercial.application.dto;

import java.util.List;

/**
 * Resultado del caso de uso que consulta los vendedores existentes.
 */
public record GetSellersResult(
        List<SellerResult> sellers
) {
}
