package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetFinancialTransactionsQuery;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionsResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetFinancialTransactionsUseCaseTest {

    private InMemoryFinancialTransactionRepository repository;
    private GetFinancialTransactionsUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFinancialTransactionRepository();
        useCase = new GetFinancialTransactionsUseCase(repository);
        repository.save(transaction(LocalDate.of(2026, 7, 31), "1000.00"));
        repository.save(transaction(LocalDate.of(2026, 8, 1), "2000.00"));
        repository.save(transaction(LocalDate.of(2026, 8, 31), "3000.00"));
        repository.save(transaction(LocalDate.of(2026, 9, 1), "4000.00"));
    }

    @Test
    void filtersInclusiveMonthlyRange() {
        GetFinancialTransactionsResult result = useCase.execute(
                new GetFinancialTransactionsQuery(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))
        );
        assertEquals(2, result.transactions().size());
        assertEquals(LocalDate.of(2026, 8, 31), result.transactions().getFirst().transactionDate());
        assertEquals(LocalDate.of(2026, 8, 1), result.transactions().get(1).transactionDate());
    }

    @Test
    void rejectsIncompleteRange() {
        assertThrows(
                FinanceDomainException.class,
                () -> useCase.execute(new GetFinancialTransactionsQuery(LocalDate.of(2026, 8, 1), null))
        );
    }

    private static FinancialTransaction transaction(LocalDate date, String amount) {
        return FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                FinancialAmount.of(new BigDecimal(amount)),
                date,
                FinancialCategory.OTHER_EXPENSE.name(),
                "Movimiento de prueba",
                null,
                FinancialTransactionSourceType.MANUAL,
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
        public BigDecimal sumAmountByTypeBetween(
                FinancialTransactionType type,
                LocalDate fromDate,
                LocalDate toDate
        ) {
            return findByTransactionDateBetweenNewestFirst(fromDate, toDate).stream()
                    .filter(transaction -> transaction.getType() == type)
                    .map(transaction -> transaction.getAmount().getValue())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        public long countByTransactionDateBetween(LocalDate fromDate, LocalDate toDate) {
            return findByTransactionDateBetweenNewestFirst(fromDate, toDate).size();
        }
    }
}
