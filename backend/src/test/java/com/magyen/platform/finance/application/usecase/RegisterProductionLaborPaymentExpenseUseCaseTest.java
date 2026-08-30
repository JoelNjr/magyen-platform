package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.RegisterProductionLaborPaymentExpenseCommand;
import com.magyen.platform.finance.application.dto.RegisterProductionLaborPaymentExpenseResult;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.LaborPaymentWeek;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterProductionLaborPaymentExpenseUseCaseTest {

    private InMemoryFinancialTransactionRepository repository;
    private RegisterProductionLaborPaymentExpenseUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFinancialTransactionRepository();
        useCase = new RegisterProductionLaborPaymentExpenseUseCase(repository);
    }

    @Test
    void sameWeekPaymentsAccumulateOnOneLedgerRow() {
        RegisterProductionLaborPaymentExpenseResult first = useCase.execute(command(
                "200000.00",
                LocalDate.of(2026, 8, 24)
        ));
        RegisterProductionLaborPaymentExpenseResult second = useCase.execute(command(
                "180000.00",
                LocalDate.of(2026, 8, 26)
        ));

        assertEquals(first.financialTransactionId(), second.financialTransactionId());
        assertEquals(new BigDecimal("380000.00"), second.amount());
        assertEquals(1, repository.findAllNewestFirst().size());

        FinancialTransaction weekly = repository.findAllNewestFirst().getFirst();
        assertEquals(FinancialTransactionType.EXPENSE, weekly.getType());
        assertEquals(FinancialTransactionSourceType.PAYROLL, weekly.getSourceType());
        assertEquals(LaborPaymentWeek.of(LocalDate.of(2026, 8, 24)).sourceId(), weekly.getSourceId());
        assertEquals(LocalDate.of(2026, 8, 24), weekly.getTransactionDate());
        assertEquals("pagos=2", weekly.getObservation());
    }

    @Test
    void differentWeeksCreateSeparateLedgerRows() {
        useCase.execute(command("200000.00", LocalDate.of(2026, 8, 24)));
        useCase.execute(command("150000.00", LocalDate.of(2026, 8, 31)));

        assertEquals(2, repository.findAllNewestFirst().size());
        BigDecimal total = repository.findAllNewestFirst().stream()
                .map(transaction -> transaction.getAmount().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("350000.00"), total);
    }

    @Test
    void repeatingTheSamePaymentDoesNotCreateASecondWeeklyRow() {
        UUID laborWorkId = UUID.randomUUID();
        useCase.execute(new RegisterProductionLaborPaymentExpenseCommand(
                laborWorkId,
                new BigDecimal("200000.00"),
                LocalDate.of(2026, 8, 24),
                "Juan",
                null
        ));
        useCase.execute(new RegisterProductionLaborPaymentExpenseCommand(
                laborWorkId,
                new BigDecimal("200000.00"),
                LocalDate.of(2026, 8, 24),
                "Juan",
                null
        ));

        FinancialTransaction weekly = repository.findAllNewestFirst().getFirst();
        assertEquals(1, repository.findAllNewestFirst().size());
        assertEquals(new BigDecimal("400000.00"), weekly.getAmount().getValue());
        assertEquals("pagos=2", weekly.getObservation());
    }

    private static RegisterProductionLaborPaymentExpenseCommand command(String amount, LocalDate paymentDate) {
        return new RegisterProductionLaborPaymentExpenseCommand(
                UUID.randomUUID(),
                new BigDecimal(amount),
                paymentDate,
                "Operario",
                null
        );
    }

    private static final class InMemoryFinancialTransactionRepository implements FinancialTransactionRepository {
        private final Map<UUID, FinancialTransaction> transactions = new LinkedHashMap<>();

        @Override
        public FinancialTransaction save(FinancialTransaction financialTransaction) {
            transactions.put(financialTransaction.getId(), financialTransaction);
            return financialTransaction;
        }

        @Override
        public Optional<FinancialTransaction> findById(UUID id) {
            return Optional.ofNullable(transactions.get(id));
        }

        @Override
        public Optional<FinancialTransaction> findBySourceTypeAndSourceId(
                FinancialTransactionSourceType sourceType,
                UUID sourceId
        ) {
            return transactions.values().stream()
                    .filter(transaction -> transaction.getSourceType() == sourceType)
                    .filter(transaction -> sourceId.equals(transaction.getSourceId()))
                    .findFirst();
        }

        @Override
        public List<FinancialTransaction> findAllNewestFirst() {
            return transactions.values().stream()
                    .sorted(Comparator.comparing(FinancialTransaction::getTransactionDate).reversed())
                    .toList();
        }

        @Override
        public List<FinancialTransaction> findByTransactionDateBetweenNewestFirst(
                LocalDate fromDate,
                LocalDate toDate
        ) {
            return findAllNewestFirst().stream()
                    .filter(transaction -> !transaction.getTransactionDate().isBefore(fromDate))
                    .filter(transaction -> !transaction.getTransactionDate().isAfter(toDate))
                    .toList();
        }

        @Override
        public java.math.BigDecimal sumAmountByTypeBetween(
                com.magyen.platform.finance.domain.FinancialTransactionType type,
                LocalDate fromDate,
                LocalDate toDate
        ) {
            return findByTransactionDateBetweenNewestFirst(fromDate, toDate).stream()
                    .filter(transaction -> transaction.getType() == type)
                    .map(transaction -> transaction.getAmount().getValue())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        }

        @Override
        public long countByTransactionDateBetween(LocalDate fromDate, LocalDate toDate) {
            return findByTransactionDateBetweenNewestFirst(fromDate, toDate).size();
        }
    }
}
