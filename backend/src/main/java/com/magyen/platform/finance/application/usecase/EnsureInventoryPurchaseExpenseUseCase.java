package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.EnsureInventoryPurchaseExpenseCommand;
import com.magyen.platform.finance.application.dto.EnsureInventoryPurchaseExpenseResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Garantiza exactamente un EXPENSE de caja por compra de inventario.
 * <p>
 * {@code sourceType = INVENTORY_PURCHASE}, {@code sourceId = purchaseId}.
 * No representa consumo de producción.
 */
public class EnsureInventoryPurchaseExpenseUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;

    public EnsureInventoryPurchaseExpenseUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    @Transactional
    public EnsureInventoryPurchaseExpenseResult execute(EnsureInventoryPurchaseExpenseCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.purchaseId(), "Purchase id must not be null");
        Objects.requireNonNull(command.amount(), "Amount must not be null");
        Objects.requireNonNull(command.purchaseDate(), "Purchase date must not be null");

        FinancialCategory category = resolveExpenseCategory(command.category());

        Optional<FinancialTransaction> existing = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.INVENTORY_PURCHASE,
                command.purchaseId()
        );
        if (existing.isPresent()) {
            return toResult(existing.get(), command.purchaseId(), true);
        }

        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                FinancialAmount.of(command.amount()),
                command.purchaseDate(),
                category.name(),
                command.description(),
                command.observation(),
                FinancialTransactionSourceType.INVENTORY_PURCHASE,
                command.purchaseId()
        );

        try {
            FinancialTransaction saved = financialTransactionRepository.save(transaction);
            return toResult(saved, command.purchaseId(), false);
        } catch (DataIntegrityViolationException exception) {
            return financialTransactionRepository
                    .findBySourceTypeAndSourceId(
                            FinancialTransactionSourceType.INVENTORY_PURCHASE,
                            command.purchaseId()
                    )
                    .map(found -> toResult(found, command.purchaseId(), true))
                    .orElseThrow(() -> exception);
        }
    }

    private static FinancialCategory resolveExpenseCategory(String category) {
        FinancialCategory parsed = FinancialCategory.of(category);
        if (parsed.getTransactionType() != FinancialTransactionType.EXPENSE) {
            throw new FinanceDomainException("Inventory purchase category must be an expense category");
        }
        return parsed;
    }

    private static EnsureInventoryPurchaseExpenseResult toResult(
            FinancialTransaction transaction,
            UUID purchaseId,
            boolean alreadyProcessed
    ) {
        return new EnsureInventoryPurchaseExpenseResult(
                transaction.getId(),
                purchaseId,
                transaction.getAmount().getValue(),
                transaction.getCategory(),
                alreadyProcessed
        );
    }
}
