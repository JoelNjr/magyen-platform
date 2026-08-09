package com.magyen.platform.commercial.presentation.customer.mapper;

import com.magyen.platform.commercial.application.dto.CustomerResult;
import com.magyen.platform.commercial.application.dto.GetCustomersResult;
import com.magyen.platform.commercial.presentation.customer.response.CustomerResponse;
import com.magyen.platform.commercial.presentation.customer.response.GetCustomersResponse;

import java.util.List;
import java.util.Objects;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application para clientes.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class CustomerPresentationMapper {

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
