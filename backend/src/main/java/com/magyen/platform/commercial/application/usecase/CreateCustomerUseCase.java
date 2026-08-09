package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerResult;
import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la creación de un nuevo cliente.
 */
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public CreateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "Customer repository must not be null");
    }

    public CreateCustomerResult execute(CreateCustomerCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Customer customer = Customer.create(command.name());
        Customer savedCustomer = customerRepository.save(customer);

        return new CreateCustomerResult(
                savedCustomer.getId(),
                savedCustomer.getName()
        );
    }

    private void validateCommand(CreateCustomerCommand command) {
        if (command.name() == null) {
            throw new IllegalArgumentException("Customer name must not be null");
        }

        if (command.name().isBlank()) {
            throw new IllegalArgumentException("Customer name must not be blank");
        }
    }
}
