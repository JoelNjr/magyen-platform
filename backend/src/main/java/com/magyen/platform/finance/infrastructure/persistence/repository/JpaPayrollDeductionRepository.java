package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.PayrollDeduction;
import com.magyen.platform.finance.domain.PayrollDeductionRepository;
import com.magyen.platform.finance.domain.PayrollDeductionStatus;
import com.magyen.platform.finance.infrastructure.persistence.entity.PayrollDeductionEntity;
import com.magyen.platform.finance.infrastructure.persistence.mapper.PayrollDeductionPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link PayrollDeductionRepository}.
 */
@Repository
public class JpaPayrollDeductionRepository implements PayrollDeductionRepository {

    private final SpringDataPayrollDeductionRepository springDataPayrollDeductionRepository;
    private final PayrollDeductionPersistenceMapper payrollDeductionPersistenceMapper;

    public JpaPayrollDeductionRepository(
            SpringDataPayrollDeductionRepository springDataPayrollDeductionRepository,
            PayrollDeductionPersistenceMapper payrollDeductionPersistenceMapper
    ) {
        this.springDataPayrollDeductionRepository = Objects.requireNonNull(
                springDataPayrollDeductionRepository,
                "Spring Data payroll deduction repository must not be null"
        );
        this.payrollDeductionPersistenceMapper = Objects.requireNonNull(
                payrollDeductionPersistenceMapper,
                "Payroll deduction persistence mapper must not be null"
        );
    }

    @Override
    public PayrollDeduction save(PayrollDeduction deduction) {
        Objects.requireNonNull(deduction, "Payroll deduction must not be null");

        PayrollDeductionEntity entity = payrollDeductionPersistenceMapper.toEntity(deduction);
        PayrollDeductionEntity saved = springDataPayrollDeductionRepository.save(entity);
        return payrollDeductionPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<PayrollDeduction> findById(UUID id) {
        Objects.requireNonNull(id, "Payroll deduction id must not be null");
        return springDataPayrollDeductionRepository.findById(id)
                .map(payrollDeductionPersistenceMapper::toDomain);
    }

    @Override
    public List<PayrollDeduction> findByEmployeeId(UUID employeeId) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        return springDataPayrollDeductionRepository
                .findByEmployeeIdOrderByDeductionDateDescCreatedAtDescIdDesc(employeeId)
                .stream()
                .map(payrollDeductionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<PayrollDeduction> findByEmployeeIdAndStatus(UUID employeeId, PayrollDeductionStatus status) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        Objects.requireNonNull(status, "Payroll deduction status must not be null");
        return springDataPayrollDeductionRepository
                .findByEmployeeIdAndStatusOrderByDeductionDateDescCreatedAtDescIdDesc(employeeId, status)
                .stream()
                .map(payrollDeductionPersistenceMapper::toDomain)
                .toList();
    }
}
