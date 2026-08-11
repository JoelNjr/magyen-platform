package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetFinancialTransactionQuery;
import com.magyen.platform.finance.application.dto.GetFinancialTransactionResult;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionResult;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RegisterFinancialTransactionUseCaseTest {

    @Autowired
    private RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase;

    @Autowired
    private GetFinancialTransactionUseCase getFinancialTransactionUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void registersIncome() {
        RegisterFinancialTransactionResult result = registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        new BigDecimal("500000.00"),
                        LocalDate.of(2026, 8, 10),
                        "Ventas",
                        "Cobro pedido",
                        null,
                        FinancialTransactionSourceType.MANUAL,
                        null
                )
        );

        assertEquals(FinancialTransactionType.INCOME, result.type());
        assertEquals(new BigDecimal("500000.00"), result.amount());
        assertEquals("Ventas", result.category());
        assertEquals(FinancialTransactionSourceType.MANUAL, result.sourceType());

        FinancialTransaction persisted = financialTransactionRepository.findById(result.transactionId())
                .orElseThrow();
        assertEquals(result.transactionId(), persisted.getId());
        assertEquals(new BigDecimal("500000.00"), persisted.getAmount().getValue());
    }

    @Test
    void registersExpense() {
        UUID sourceId = UUID.randomUUID();

        RegisterFinancialTransactionResult result = registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.EXPENSE,
                        new BigDecimal("150000.00"),
                        LocalDate.of(2026, 8, 10),
                        "Servicios",
                        "Pago de energía",
                        "Factura agosto",
                        FinancialTransactionSourceType.SERVICE,
                        sourceId
                )
        );

        assertEquals(FinancialTransactionType.EXPENSE, result.type());
        assertEquals(new BigDecimal("150000.00"), result.amount());
        assertEquals(FinancialTransactionSourceType.SERVICE, result.sourceType());
        assertEquals(sourceId, result.sourceId());
    }

    @Test
    void rejectsNotFoundRead() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getFinancialTransactionUseCase.execute(
                        new GetFinancialTransactionQuery(UUID.randomUUID())
                )
        );

        assertTrue(exception.getMessage().contains("Financial transaction not found"));
    }

    @Test
    void persistsThroughRepositoryAndReloads() {
        RegisterFinancialTransactionResult created = registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.EXPENSE,
                        new BigDecimal("10.5"),
                        LocalDate.of(2026, 8, 9),
                        "Materiales",
                        null,
                        null,
                        null,
                        null
                )
        );

        GetFinancialTransactionResult reloaded = getFinancialTransactionUseCase.execute(
                new GetFinancialTransactionQuery(created.transactionId())
        );

        assertEquals(created.transactionId(), reloaded.transactionId());
        assertEquals(new BigDecimal("10.50"), reloaded.amount());
        assertEquals(FinancialTransactionSourceType.MANUAL, reloaded.sourceType());
        assertEquals("Materiales", reloaded.category());
    }

    @Test
    void rejectsInvalidAmount() {
        assertThrows(FinanceDomainException.class, () -> registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.EXPENSE,
                        BigDecimal.ZERO,
                        LocalDate.of(2026, 8, 10),
                        "Servicios",
                        null,
                        null,
                        FinancialTransactionSourceType.MANUAL,
                        null
                )
        ));
    }
}
