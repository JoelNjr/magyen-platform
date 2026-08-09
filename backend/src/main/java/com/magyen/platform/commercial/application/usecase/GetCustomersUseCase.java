package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CustomerResult;
import com.magyen.platform.commercial.application.dto.GetCustomersResult;
import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consulta los clientes existentes.
 */
public class GetCustomersUseCase {

    private final CustomerRepository customerRepository;

    public GetCustomersUseCase(CustomerRepository customerRepository) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "Customer repository must not be null");
    }

    public GetCustomersResult execute() {
        List<CustomerResult> customers = customerRepository.findAll().stream()
                .map(this::toCustomerResult)
                .toList();

        return new GetCustomersResult(customers);
    }

    private CustomerResult toCustomerResult(Customer customer) {
        return new CustomerResult(
                customer.getId(),
                customer.getName()
        );
    }
}
