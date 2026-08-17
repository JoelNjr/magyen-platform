package com.magyen.platform.commercial.presentation.seller.controller;

import com.magyen.platform.commercial.application.dto.GetSellersResult;
import com.magyen.platform.commercial.application.usecase.GetSellersUseCase;
import com.magyen.platform.commercial.presentation.seller.mapper.SellerPresentationMapper;
import com.magyen.platform.commercial.presentation.seller.response.GetSellersResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la API REST de lectura de vendedores elegibles.
 * <p>
 * No es un catálogo independiente. La fuente de verdad es Finance {@code PayrollEmployee}.
 */
@RestController
@RequestMapping("/api/v1/sellers")
public class SellerController {

    private final GetSellersUseCase getSellersUseCase;
    private final SellerPresentationMapper sellerPresentationMapper;

    public SellerController(
            GetSellersUseCase getSellersUseCase,
            SellerPresentationMapper sellerPresentationMapper
    ) {
        this.getSellersUseCase = getSellersUseCase;
        this.sellerPresentationMapper = sellerPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetSellersResponse> getSellers() {
        GetSellersResult result = getSellersUseCase.execute();
        GetSellersResponse response = sellerPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
