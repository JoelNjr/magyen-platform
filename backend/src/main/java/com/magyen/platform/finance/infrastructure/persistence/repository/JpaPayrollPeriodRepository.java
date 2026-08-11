package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.PayrollPeriod;
import com.magyen.platform.finance.domain.PayrollPeriodRepository;
import com.magyen.platform.finance.domain.PayrollPeriodStatus;
import com.magyen.platform.finance.infrastructure.persistence.entity.PayrollPeriodEntity;
import com.magyen.platform.finance.infrastructure.persistence.mapper.PayrollPeriodPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura del port {@link PayrollPeriodRepository}.
 */
@Repository
public class JpaPayrollPeriodRepository implements PayrollPeriodRepository {

    private final SpringDataPayrollPeriodRepository springDataRepository;
    private final PayrollPeriodPersistenceMapper persistenceMapper;

    public JpaPayrollPeriodRepository(
            SpringDataPayrollPeriodRepository springDataRepository,
            PayrollPeriodPersistenceMapper persistenceMapper
    ) {
        this.springDataRepository = Objects.requireNonNull(
                springDataRepository,
                "Spring Data payroll period repository must not be null"
        );
        this.persistenceMapper = Objects.requireNonNull(
                persistenceMapper,
                "Payroll period persistence mapper must not be null"
        );
    }

    @Override
    public PayrollPeriod save(PayrollPeriod payrollPeriod) {
        Objects.requireNonNull(payrollPeriod, "Payroll period must not be null");

        PayrollPeriodEntity entity = persistenceMapper.toEntity(payrollPeriod);
        PayrollPeriodEntity saved = springDataRepository.save(entity);
        return persistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<PayrollPeriod> findById(UUID id) {
        Objects.requireNonNull(id, "Payroll period id must not be null");
        return springDataRepository.findById(id).map(persistenceMapper::toDomain);
    }

    @Override
    public Optional<PayrollPeriod> findByEmployeeIdAndPeriodStart(UUID employeeId, LocalDate periodStart) {
        Objects.requireNonNull(employeeId, "Employee id must not be null");
        Objects.requireNonNull(periodStart, "Period start must not be null");

        return springDataRepository
                .findByEmployeeIdAndPeriodStart(employeeId, periodStart)
                .map(persistenceMapper::toDomain);
    }

    @Override
    public List<PayrollPeriod> findAllNewestFirst() {
        return springDataRepository.findAllByOrderByExpectedPaymentDateDescIdDesc().stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<PayrollPeriod> findByExpectedPaymentDateBetween(LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");
        return springDataRepository
                .findByExpectedPaymentDateBetweenOrderByExpectedPaymentDateAscIdAsc(fromDate, toDate)
                .stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<PayrollPeriod> findByStatus(PayrollPeriodStatus status) {
        Objects.requireNonNull(status, "Status must not be null");
        return springDataRepository.findByStatusOrderByExpectedPaymentDateAscIdAsc(status).stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }
}
