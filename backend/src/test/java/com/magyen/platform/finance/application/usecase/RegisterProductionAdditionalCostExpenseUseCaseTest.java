package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.RegisterProductionAdditionalCostExpenseCommand;
import com.magyen.platform.finance.application.dto.RegisterProductionAdditionalCostExpenseResult;
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

class RegisterProductionAdditionalCostExpenseUseCaseTest {

    private InMemoryFinancialTransactionRepository repository;
    private RegisterProductionAdditionalCostExpenseUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFinancialTransactionRepository();
        useCase = new RegisterProductionAdditionalCostExpenseUseCase(repository);
    }

    @Test
    void registersOneExpenseWithDescriptionAndProductionSource() {
        UUID additionalCostId = UUID.randomUUID();
        RegisterProductionAdditionalCostExpenseResult result = useCase.execute(
                new RegisterProductionAdditionalCostExpenseCommand(
                        additionalCostId,
                        new BigDecimal("80000.00"),
                        LocalDate.of(2026, 8, 20),
                        "Envío de uniformes a Cartagena"
                )
        );

        assertEquals(new BigDecimal("80000.00"), result.amount());
        assertEquals(1, repository.findAllNewestFirst().size());

        FinancialTransaction transaction = repository.findAllNewestFirst().getFirst();
        assertEquals(FinancialTransactionType.EXPENSE, transaction.getType());
        assertEquals(FinancialCategory.OTHER_EXPENSE.name(), transaction.getCategory());
        assertEquals(FinancialTransactionSourceType.PRODUCTION, transaction.getSourceType());
        assertEquals(additionalCostId, transaction.getSourceId());
        assertEquals("Envío de uniformes a Cartagena", transaction.getDescription());
    }

    @Test
    void rejectsSecondExpenseForTheSameAdditionalCost() {
        UUID additionalCostId = UUID.randomUUID();
        RegisterProductionAdditionalCostExpenseCommand command =
                new RegisterProductionAdditionalCostExpenseCommand(
                        additionalCostId,
                        new BigDecimal("80000.00"),
                        LocalDate.of(2026, 8, 20),
                        "Envío de uniformes a Cartagena"
                );
        useCase.execute(command);

        assertThrows(FinanceDomainException.class, () -> useCase.execute(command));
        assertEquals(1, repository.findAllNewestFirst().size());
        assertEquals(
                new BigDecimal("80000.00"),
                repository.findAllNewestFirst().getFirst().getAmount().getValue()
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
