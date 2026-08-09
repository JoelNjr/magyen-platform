package com.magyen.platform.commercial.presentation.customer.controller;

import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerResult;
import com.magyen.platform.commercial.application.dto.GetCustomersResult;
import com.magyen.platform.commercial.application.dto.UpdateCustomerCommand;
import com.magyen.platform.commercial.application.dto.UpdateCustomerResult;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.application.usecase.UpdateCustomerUseCase;
import com.magyen.platform.commercial.presentation.customer.mapper.CustomerPresentationMapper;
import com.magyen.platform.commercial.presentation.customer.request.CreateCustomerRequest;
import com.magyen.platform.commercial.presentation.customer.request.UpdateCustomerRequest;
import com.magyen.platform.commercial.presentation.customer.response.CreateCustomerResponse;
import com.magyen.platform.commercial.presentation.customer.response.GetCustomersResponse;
import com.magyen.platform.commercial.presentation.customer.response.UpdateCustomerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de clientes.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final GetCustomersUseCase getCustomersUseCase;
    private final CustomerPresentationMapper customerPresentationMapper;

    public CustomerController(
            CreateCustomerUseCase createCustomerUseCase,
            UpdateCustomerUseCase updateCustomerUseCase,
            GetCustomersUseCase getCustomersUseCase,
            CustomerPresentationMapper customerPresentationMapper
    ) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
        this.getCustomersUseCase = getCustomersUseCase;
        this.customerPresentationMapper = customerPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetCustomersResponse> getCustomers() {
        GetCustomersResult result = getCustomersUseCase.execute();
        GetCustomersResponse response = customerPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CreateCustomerResponse> createCustomer(
            @RequestBody CreateCustomerRequest request
    ) {
        CreateCustomerCommand command = customerPresentationMapper.toCommand(request);
        CreateCustomerResult result = createCustomerUseCase.execute(command);
        CreateCustomerResponse response = customerPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<UpdateCustomerResponse> updateCustomer(
            @PathVariable UUID customerId,
            @RequestBody UpdateCustomerRequest request
    ) {
        UpdateCustomerCommand command = customerPresentationMapper.toUpdateCommand(customerId, request);
        UpdateCustomerResult result = updateCustomerUseCase.execute(command);
        UpdateCustomerResponse response = customerPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
