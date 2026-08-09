package com.magyen.platform.commercial.infrastructure.persistence.repository;

import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.commercial.infrastructure.persistence.entity.CustomerEntity;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.CustomerPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link CustomerRepository}.
 * <p>
 * Traduce entre el agregado de dominio y el modelo JPA; nunca expone entidades de persistencia.
 */
@Repository
public class JpaCustomerRepository implements CustomerRepository {

    private final SpringDataCustomerJpaRepository springDataCustomerJpaRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;

    public JpaCustomerRepository(
            SpringDataCustomerJpaRepository springDataCustomerJpaRepository,
            CustomerPersistenceMapper customerPersistenceMapper
    ) {
        this.springDataCustomerJpaRepository = Objects.requireNonNull(
                springDataCustomerJpaRepository,
                "Spring Data Customer JPA repository must not be null"
        );
        this.customerPersistenceMapper = Objects.requireNonNull(
                customerPersistenceMapper,
                "Customer persistence mapper must not be null"
        );
    }

    @Override
    public Customer save(Customer customer) {
        Objects.requireNonNull(customer, "Customer must not be null");

        CustomerEntity customerEntity = customerPersistenceMapper.toEntity(customer);
        CustomerEntity savedCustomerEntity = springDataCustomerJpaRepository.save(customerEntity);
        return customerPersistenceMapper.toDomain(savedCustomerEntity);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        Objects.requireNonNull(id, "Customer id must not be null");

        return springDataCustomerJpaRepository.findById(id)
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return springDataCustomerJpaRepository.findAll().stream()
                .map(customerPersistenceMapper::toDomain)
                .toList();
    }
}
