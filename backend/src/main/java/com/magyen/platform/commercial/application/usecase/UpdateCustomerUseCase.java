package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.UpdateCustomerCommand;
import com.magyen.platform.commercial.application.dto.UpdateCustomerResult;
import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la actualización del nombre de un cliente existente.
 */
public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public UpdateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "Customer repository must not be null");
    }

    public UpdateCustomerResult execute(UpdateCustomerCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found: " + command.customerId()
                ));

        customer.rename(command.name());
        Customer savedCustomer = customerRepository.save(customer);

        return new UpdateCustomerResult(
                savedCustomer.getId(),
                savedCustomer.getName()
        );
    }

    private void validateCommand(UpdateCustomerCommand command) {
        if (command.customerId() == null) {
            throw new IllegalArgumentException("Customer id must not be null");
        }

        if (command.name() == null) {
            throw new IllegalArgumentException("Customer name must not be null");
        }

        if (command.name().isBlank()) {
            throw new IllegalArgumentException("Customer name must not be blank");
        }
    }
}
