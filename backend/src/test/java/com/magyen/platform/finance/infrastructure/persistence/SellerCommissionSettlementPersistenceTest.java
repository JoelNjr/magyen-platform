package com.magyen.platform.finance.infrastructure.persistence;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.SellerCommissionSettlement;
import com.magyen.platform.finance.domain.SellerCommissionSettlementStatus;
import com.magyen.platform.finance.infrastructure.persistence.repository.SpringDataFinancialTransactionRepository;
import com.magyen.platform.finance.infrastructure.persistence.repository.SpringDataSellerCommissionSettlementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class SellerCommissionSettlementPersistenceTest {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private SpringDataSellerCommissionSettlementRepository settlementRepository;

    @Autowired
    private SpringDataFinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private com.magyen.platform.finance.infrastructure.persistence.mapper.SellerCommissionSettlementPersistenceMapper settlementMapper;

    @Autowired
    private com.magyen.platform.finance.infrastructure.persistence.mapper.FinancialTransactionPersistenceMapper transactionMapper;

    @Test
    void persistsSnapshotsAndRejectsASecondSettlementForTheSameMonth() {
        UUID employeeId = UUID.randomUUID();
        UUID settlementId = UUID.randomUUID();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        try {
            transaction.executeWithoutResult(status -> {
                var expense = transactionMapper.toEntity(expense(settlementId, "100.00"));
                var savedExpense = financialTransactionRepository.saveAndFlush(expense);
                settlementRepository.saveAndFlush(settlementMapper.toEntity(settlement(
                        settlementId,
                        employeeId,
                        savedExpense.getId(),
                        "100.00"
                )));
            });

            var reloaded = settlementMapper.toDomain(
                    settlementRepository.findById(settlementId).orElseThrow()
            );
            assertEquals(new BigDecimal("2000.00"), reloaded.getSalesSnapshot().getValue());
            assertEquals(new BigDecimal("100.00"), reloaded.getCommissionSnapshot().getValue());
            assertEquals(2, reloaded.getOrderCountSnapshot());
            assertEquals(LocalDate.of(2026, 9, 30), reloaded.getActualPaymentDate());
            assertEquals(SellerCommissionSettlementStatus.PAID, reloaded.getStatus());

            assertThrows(DataIntegrityViolationException.class, () -> transaction.executeWithoutResult(status ->
                    settlementRepository.saveAndFlush(settlementMapper.toEntity(settlement(
                            UUID.randomUUID(),
                            employeeId,
                            UUID.randomUUID(),
                            "50.00"
                    )))
            ));
        } finally {
            transaction.executeWithoutResult(status -> {
                settlementRepository.findByEmployeeIdAndPeriodStart(employeeId, LocalDate.of(2026, 9, 1))
                        .ifPresent(settlementRepository::delete);
                financialTransactionRepository.findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.SELLER_COMMISSION,
                        settlementId
                ).ifPresent(financialTransactionRepository::delete);
            });
        }
    }

    @Test
    void sellerCommissionSourceCannotBeRepeated() {
        UUID settlementId = UUID.randomUUID();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        try {
            transaction.executeWithoutResult(status ->
                    financialTransactionRepository.saveAndFlush(transactionMapper.toEntity(expense(settlementId, "80.00")))
            );
            assertThrows(DataIntegrityViolationException.class, () -> transaction.executeWithoutResult(status ->
                    financialTransactionRepository.saveAndFlush(transactionMapper.toEntity(expense(settlementId, "80.00")))
            ));
        } finally {
            transaction.executeWithoutResult(status -> financialTransactionRepository
                    .findBySourceTypeAndSourceId(FinancialTransactionSourceType.SELLER_COMMISSION, settlementId)
                    .ifPresent(financialTransactionRepository::delete));
        }
    }

    private static SellerCommissionSettlement settlement(
            UUID settlementId,
            UUID employeeId,
            UUID transactionId,
            String commission
    ) {
        return SellerCommissionSettlement.createPaid(
                settlementId,
                employeeId,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                FinancialAmount.of(new BigDecimal("2000.00")),
                2,
                FinancialAmount.of(new BigDecimal(commission)),
                LocalDate.of(2026, 9, 30),
                LocalDateTime.of(2026, 9, 30, 9, 15),
                transactionId,
                "observación"
        );
    }

    private static FinancialTransaction expense(UUID settlementId, String amount) {
        return FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                FinancialAmount.of(new BigDecimal(amount)),
                LocalDate.of(2026, 9, 30),
                FinancialCategory.PAYROLL.name(),
                "Comisión vendedor Persistencia - 2026-09",
                null,
                FinancialTransactionSourceType.SELLER_COMMISSION,
                settlementId
        );
    }
}
