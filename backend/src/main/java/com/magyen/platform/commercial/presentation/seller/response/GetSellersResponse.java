package com.magyen.platform.commercial.presentation.seller.response;

import java.util.List;

/**
 * Respuesta HTTP con los vendedores internos existentes.
 */
public record GetSellersResponse(
        List<SellerResponse> sellers
) {
}
