package com.magyen.platform.commercial.presentation.customer.mapper;

import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerResult;
import com.magyen.platform.commercial.application.dto.CustomerResult;
import com.magyen.platform.commercial.application.dto.GetCustomersResult;
import com.magyen.platform.commercial.application.dto.UpdateCustomerCommand;
import com.magyen.platform.commercial.application.dto.UpdateCustomerResult;
import com.magyen.platform.commercial.presentation.customer.request.CreateCustomerRequest;
import com.magyen.platform.commercial.presentation.customer.request.UpdateCustomerRequest;
import com.magyen.platform.commercial.presentation.customer.response.CreateCustomerResponse;
import com.magyen.platform.commercial.presentation.customer.response.CustomerResponse;
import com.magyen.platform.commercial.presentation.customer.response.GetCustomersResponse;
import com.magyen.platform.commercial.presentation.customer.response.UpdateCustomerResponse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application para clientes.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class CustomerPresentationMapper {

    public CreateCustomerCommand toCommand(CreateCustomerRequest request) {
        Objects.requireNonNull(request, "CreateCustomerRequest must not be null");

        return new CreateCustomerCommand(request.name());
    }

    public CreateCustomerResponse toResponse(CreateCustomerResult result) {
        Objects.requireNonNull(result, "CreateCustomerResult must not be null");

        return new CreateCustomerResponse(
                result.customerId(),
                result.name()
        );
    }

    public UpdateCustomerCommand toUpdateCommand(UUID customerId, UpdateCustomerRequest request) {
        Objects.requireNonNull(customerId, "Customer id must not be null");
        Objects.requireNonNull(request, "UpdateCustomerRequest must not be null");

        return new UpdateCustomerCommand(customerId, request.name());
    }

    public UpdateCustomerResponse toResponse(UpdateCustomerResult result) {
        Objects.requireNonNull(result, "UpdateCustomerResult must not be null");

        return new UpdateCustomerResponse(
                result.customerId(),
                result.name()
        );
    }

    public GetCustomersResponse toResponse(GetCustomersResult result) {
        Objects.requireNonNull(result, "GetCustomersResult must not be null");

        List<CustomerResponse> customers = result.customers().stream()
                .map(this::toCustomerResponse)
                .toList();

        return new GetCustomersResponse(customers);
    }

    private CustomerResponse toCustomerResponse(CustomerResult customer) {
        return new CustomerResponse(
                customer.customerId(),
                customer.name()
        );
    }
}
