package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.EnsurePlotterInternalServiceLedgerCommand;
import com.magyen.platform.finance.application.dto.EnsurePlotterInternalServiceLedgerResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Crea exactamente un EXPENSE y un INCOME del mismo valor para un trabajo Plotter interno.
 * <p>
 * Impacto neto de caja = 0. No representa compra de papel ni venta a cliente externo.
 * Idempotente por {@code plotterJobId}.
 */
public class EnsurePlotterInternalServiceLedgerUseCase {

    static final String EXPENSE_DESCRIPTION = "Servicio Plotter interno Magyen";
    static final String INCOME_DESCRIPTION = "Reclasificación servicio Plotter interno Magyen";

    private final FinancialTransactionRepository financialTransactionRepository;

    public EnsurePlotterInternalServiceLedgerUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    @Transactional
    public EnsurePlotterInternalServiceLedgerResult execute(EnsurePlotterInternalServiceLedgerCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.plotterJobId(), "Plotter job id must not be null");
        Objects.requireNonNull(command.amount(), "Amount must not be null");
        Objects.requireNonNull(command.transactionDate(), "Transaction date must not be null");
        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceDomainException("Internal Plotter service amount must be greater than zero");
        }

        Optional<FinancialTransaction> existingExpense = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE,
                command.plotterJobId()
        );
        Optional<FinancialTransaction> existingIncome = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME,
                command.plotterJobId()
        );

        FinancialTransaction expense = existingExpense.orElseGet(() -> createExpense(command));
        FinancialTransaction income = existingIncome.orElseGet(() -> createIncome(command));

        return new EnsurePlotterInternalServiceLedgerResult(
                command.plotterJobId(),
                expense.getId(),
                income.getId(),
                command.amount(),
                existingExpense.isPresent() && existingIncome.isPresent()
        );
    }

    /**
     * Actualiza monto y fecha de los dos lados del par. No crea asientos adicionales
     * si el par ya existe. Si falta un solo lado, rechaza la corrección.
     */
    @Transactional
    public void synchronize(EnsurePlotterInternalServiceLedgerCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.plotterJobId(), "Plotter job id must not be null");
        Objects.requireNonNull(command.amount(), "Amount must not be null");
        Objects.requireNonNull(command.transactionDate(), "Transaction date must not be null");
        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceDomainException("Internal Plotter service amount must be greater than zero");
        }

        Optional<FinancialTransaction> existingExpense = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE,
                command.plotterJobId()
        );
        Optional<FinancialTransaction> existingIncome = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME,
                command.plotterJobId()
        );
        if (existingExpense.isEmpty() && existingIncome.isEmpty()) {
            execute(command);
            return;
        }
        if (existingExpense.isEmpty() || existingIncome.isEmpty()) {
            throw new FinanceDomainException(
                    "Internal Plotter ledger pair is incomplete for job: " + command.plotterJobId()
            );
        }

        FinancialAmount amount = FinancialAmount.of(command.amount());
        replaceSideIfNeeded(existingExpense.get(), amount, command.transactionDate());
        replaceSideIfNeeded(existingIncome.get(), amount, command.transactionDate());
    }

    /**
     * Elimina los dos lados del par interno. No deja un solo lado.
     * Si el par ya no existe, el reintento no hace nada.
     */
    public boolean hasPair(UUID plotterJobId) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        boolean expense = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE,
                plotterJobId
        ).isPresent();
        boolean income = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME,
                plotterJobId
        ).isPresent();
        return expense || income;
    }

    @Transactional
    public void reversePair(UUID plotterJobId) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        Optional<FinancialTransaction> existingExpense = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE,
                plotterJobId
        );
        Optional<FinancialTransaction> existingIncome = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME,
                plotterJobId
        );
        if (existingExpense.isEmpty() && existingIncome.isEmpty()) {
            return;
        }
        if (existingExpense.isEmpty() || existingIncome.isEmpty()) {
            throw new FinanceDomainException(
                    "Internal Plotter ledger pair is incomplete for job: " + plotterJobId
            );
        }
        financialTransactionRepository.deleteById(existingExpense.get().getId());
        financialTransactionRepository.deleteById(existingIncome.get().getId());
    }

    private void replaceSideIfNeeded(
            FinancialTransaction current,
            FinancialAmount amount,
            java.time.LocalDate transactionDate
    ) {
        boolean sameAmount = current.getAmount().getValue().compareTo(amount.getValue()) == 0;
        boolean sameDate = current.getTransactionDate().equals(transactionDate);
        if (sameAmount && sameDate) {
            return;
        }
        financialTransactionRepository.save(current.withAmountAndDate(amount, transactionDate));
    }

    private FinancialTransaction createExpense(EnsurePlotterInternalServiceLedgerCommand command) {
        return saveIdempotent(
                FinancialTransactionType.EXPENSE,
                FinancialCategory.INTERNAL_PLOTTER_SERVICE_EXPENSE,
                EXPENSE_DESCRIPTION,
                FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE,
                command
        );
    }

    private FinancialTransaction createIncome(EnsurePlotterInternalServiceLedgerCommand command) {
        return saveIdempotent(
                FinancialTransactionType.INCOME,
                FinancialCategory.INTERNAL_PLOTTER_SERVICE_INCOME,
                INCOME_DESCRIPTION,
                FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME,
                command
        );
    }

    private FinancialTransaction saveIdempotent(
            FinancialTransactionType type,
            FinancialCategory category,
            String description,
            FinancialTransactionSourceType sourceType,
            EnsurePlotterInternalServiceLedgerCommand command
    ) {
        FinancialTransaction transaction = FinancialTransaction.create(
                type,
                FinancialAmount.of(command.amount()),
                command.transactionDate(),
                category.name(),
                description,
                command.observation(),
                sourceType,
                command.plotterJobId()
        );
        try {
            return financialTransactionRepository.save(transaction);
        } catch (DataIntegrityViolationException exception) {
            return financialTransactionRepository
                    .findBySourceTypeAndSourceId(sourceType, command.plotterJobId())
                    .orElseThrow(() -> exception);
        }
    }
}
