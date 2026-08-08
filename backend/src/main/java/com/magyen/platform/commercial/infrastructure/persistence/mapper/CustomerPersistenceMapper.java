package com.magyen.platform.commercial.infrastructure.persistence.mapper;

import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.infrastructure.persistence.entity.CustomerEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link Customer} y su modelo JPA.
 * <p>
 * No contiene reglas de negocio ni accede a la base de datos.
 */
public class CustomerPersistenceMapper {

    public CustomerEntity toEntity(Customer customer) {
        Objects.requireNonNull(customer, "Customer must not be null");

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId(customer.getId());
        customerEntity.setName(customer.getName());
        return customerEntity;
    }

    public Customer toDomain(CustomerEntity customerEntity) {
        Objects.requireNonNull(customerEntity, "Customer entity must not be null");

        return Customer.reconstitute(
                customerEntity.getId(),
                customerEntity.getName()
        );
    }
}
