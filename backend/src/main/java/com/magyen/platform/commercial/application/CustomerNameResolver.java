package com.magyen.platform.commercial.application;

import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve el nombre legible de un cliente a partir de su identidad estable.
 */
public class CustomerNameResolver {

    private final CustomerRepository customerRepository;

    public CustomerNameResolver(CustomerRepository customerRepository) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "Customer repository must not be null");
    }

    public String resolveName(UUID customerId) {
        if (customerId == null) {
            return null;
        }

        return customerRepository.findById(customerId)
                .map(Customer::getName)
                .orElse(null);
    }

    public Map<UUID, String> resolveNames(Collection<UUID> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return Map.of();
        }

        return customerRepository.findAll().stream()
                .filter(customer -> customerIds.contains(customer.getId()))
                .collect(Collectors.toMap(Customer::getId, Customer::getName, (left, right) -> left));
    }

    public Function<UUID, String> nameLookup(Collection<UUID> customerIds) {
        Map<UUID, String> names = resolveNames(customerIds);
        return customerId -> names.get(customerId);
    }
}
