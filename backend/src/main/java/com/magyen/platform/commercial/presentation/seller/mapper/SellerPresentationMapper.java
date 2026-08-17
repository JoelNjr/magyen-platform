package com.magyen.platform.commercial.presentation.seller.mapper;

import com.magyen.platform.commercial.application.dto.CreateSellerCommand;
import com.magyen.platform.commercial.application.dto.CreateSellerResult;
import com.magyen.platform.commercial.application.dto.GetSellersResult;
import com.magyen.platform.commercial.application.dto.SellerResult;
import com.magyen.platform.commercial.presentation.seller.request.CreateSellerRequest;
import com.magyen.platform.commercial.presentation.seller.response.CreateSellerResponse;
import com.magyen.platform.commercial.presentation.seller.response.GetSellersResponse;
import com.magyen.platform.commercial.presentation.seller.response.SellerResponse;

import java.util.List;
import java.util.Objects;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application para vendedores.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class SellerPresentationMapper {

    public CreateSellerCommand toCommand(CreateSellerRequest request) {
        Objects.requireNonNull(request, "CreateSellerRequest must not be null");

        return new CreateSellerCommand(request.name());
    }

    public CreateSellerResponse toResponse(CreateSellerResult result) {
        Objects.requireNonNull(result, "CreateSellerResult must not be null");

        return new CreateSellerResponse(
                result.sellerId(),
                result.name(),
                result.active()
        );
    }

    public GetSellersResponse toResponse(GetSellersResult result) {
        Objects.requireNonNull(result, "GetSellersResult must not be null");

        List<SellerResponse> sellers = result.sellers().stream()
                .map(this::toSellerResponse)
                .toList();

        return new GetSellersResponse(sellers);
    }

    private SellerResponse toSellerResponse(SellerResult seller) {
        return new SellerResponse(
                seller.sellerId(),
                seller.name(),
                seller.active()
        );
    }
}
