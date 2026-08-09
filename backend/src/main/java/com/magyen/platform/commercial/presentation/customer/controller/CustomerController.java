package com.magyen.platform.commercial.presentation.customer.controller;

import com.magyen.platform.commercial.application.dto.GetCustomersResult;
import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.presentation.customer.mapper.CustomerPresentationMapper;
import com.magyen.platform.commercial.presentation.customer.response.GetCustomersResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la API REST de clientes.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final GetCustomersUseCase getCustomersUseCase;
    private final CustomerPresentationMapper customerPresentationMapper;

    public CustomerController(
            GetCustomersUseCase getCustomersUseCase,
            CustomerPresentationMapper customerPresentationMapper
    ) {
        this.getCustomersUseCase = getCustomersUseCase;
        this.customerPresentationMapper = customerPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetCustomersResponse> getCustomers() {
        GetCustomersResult result = getCustomersUseCase.execute();
        GetCustomersResponse response = customerPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
