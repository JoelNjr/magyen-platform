package com.magyen.platform.commercial.presentation.seller.controller;

import com.magyen.platform.commercial.application.dto.CreateSellerCommand;
import com.magyen.platform.commercial.application.dto.CreateSellerResult;
import com.magyen.platform.commercial.application.dto.GetSellersResult;
import com.magyen.platform.commercial.application.usecase.CreateSellerUseCase;
import com.magyen.platform.commercial.application.usecase.GetSellersUseCase;
import com.magyen.platform.commercial.presentation.seller.mapper.SellerPresentationMapper;
import com.magyen.platform.commercial.presentation.seller.request.CreateSellerRequest;
import com.magyen.platform.commercial.presentation.seller.response.CreateSellerResponse;
import com.magyen.platform.commercial.presentation.seller.response.GetSellersResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la API REST de vendedores internos.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/sellers")
public class SellerController {

    private final CreateSellerUseCase createSellerUseCase;
    private final GetSellersUseCase getSellersUseCase;
    private final SellerPresentationMapper sellerPresentationMapper;

    public SellerController(
            CreateSellerUseCase createSellerUseCase,
            GetSellersUseCase getSellersUseCase,
            SellerPresentationMapper sellerPresentationMapper
    ) {
        this.createSellerUseCase = createSellerUseCase;
        this.getSellersUseCase = getSellersUseCase;
        this.sellerPresentationMapper = sellerPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetSellersResponse> getSellers() {
        GetSellersResult result = getSellersUseCase.execute();
        GetSellersResponse response = sellerPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CreateSellerResponse> createSeller(
            @RequestBody CreateSellerRequest request
    ) {
        CreateSellerCommand command = sellerPresentationMapper.toCommand(request);
        CreateSellerResult result = createSellerUseCase.execute(command);
        CreateSellerResponse response = sellerPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
