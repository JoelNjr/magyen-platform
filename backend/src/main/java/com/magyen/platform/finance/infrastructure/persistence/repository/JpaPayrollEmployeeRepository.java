package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.infrastructure.persistence.entity.PayrollEmployeeEntity;
import com.magyen.platform.finance.infrastructure.persistence.mapper.PayrollEmployeePersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link PayrollEmployeeRepository}.
 */
@Repository
public class JpaPayrollEmployeeRepository implements PayrollEmployeeRepository {

    private final SpringDataPayrollEmployeeRepository springDataPayrollEmployeeRepository;
    private final PayrollEmployeePersistenceMapper payrollEmployeePersistenceMapper;

    public JpaPayrollEmployeeRepository(
            SpringDataPayrollEmployeeRepository springDataPayrollEmployeeRepository,
            PayrollEmployeePersistenceMapper payrollEmployeePersistenceMapper
    ) {
        this.springDataPayrollEmployeeRepository = Objects.requireNonNull(
                springDataPayrollEmployeeRepository,
                "Spring Data payroll employee repository must not be null"
        );
        this.payrollEmployeePersistenceMapper = Objects.requireNonNull(
                payrollEmployeePersistenceMapper,
                "Payroll employee persistence mapper must not be null"
        );
    }

    @Override
    public PayrollEmployee save(PayrollEmployee employee) {
        Objects.requireNonNull(employee, "Payroll employee must not be null");

        PayrollEmployeeEntity entity = payrollEmployeePersistenceMapper.toEntity(employee);
        PayrollEmployeeEntity saved = springDataPayrollEmployeeRepository.save(entity);
        return payrollEmployeePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<PayrollEmployee> findById(UUID id) {
        Objects.requireNonNull(id, "Payroll employee id must not be null");
        return springDataPayrollEmployeeRepository.findById(id)
                .map(payrollEmployeePersistenceMapper::toDomain);
    }

    @Override
    public List<PayrollEmployee> findAll() {
        return springDataPayrollEmployeeRepository.findAllByOrderByDisplayNameAscIdAsc()
                .stream()
                .map(payrollEmployeePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<PayrollEmployee> findByActive(boolean active) {
        return springDataPayrollEmployeeRepository.findByActiveOrderByDisplayNameAscIdAsc(active)
                .stream()
                .map(payrollEmployeePersistenceMapper::toDomain)
                .toList();
    }
}
