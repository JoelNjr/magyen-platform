package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetSellersResult;
import com.magyen.platform.commercial.application.dto.SellerResult;
import com.magyen.platform.commercial.domain.Seller;
import com.magyen.platform.commercial.domain.SellerRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consulta los vendedores internos existentes.
 */
public class GetSellersUseCase {

    private final SellerRepository sellerRepository;

    public GetSellersUseCase(SellerRepository sellerRepository) {
        this.sellerRepository = Objects.requireNonNull(sellerRepository, "Seller repository must not be null");
    }

    public GetSellersResult execute() {
        List<SellerResult> sellers = sellerRepository.findAll().stream()
                .map(this::toSellerResult)
                .toList();

        return new GetSellersResult(sellers);
    }

    private SellerResult toSellerResult(Seller seller) {
        return new SellerResult(
                seller.getId(),
                seller.getName(),
                seller.isActive()
        );
    }
}
