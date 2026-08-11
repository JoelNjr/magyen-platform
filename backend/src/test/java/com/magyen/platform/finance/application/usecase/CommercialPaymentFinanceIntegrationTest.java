package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.dto.RegisterPaymentResult;
import com.magyen.platform.finance.application.dto.SynchronizeCommercialPaymentFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.SynchronizeCommercialPaymentFinancialTransactionResult;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class CommercialPaymentFinanceIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private RegisterPaymentUseCase registerPaymentUseCase;

    @Autowired
    private SynchronizeCommercialPaymentFinancialTransactionUseCase synchronizeUseCase;

    @Autowired
    private RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase;

    @Test
    void paymentCreatesExactlyOneIncomeAndResyncIsIdempotent() {
        Order order = createOrderWithTotal("1000000.00");
        LocalDate paymentDate = LocalDate.of(2026, 8, 10);

        RegisterPaymentResult payment = registerPaymentUseCase.execute(
                new RegisterPaymentCommand(
                        order.getId(),
                        new BigDecimal("500000.00"),
                        paymentDate,
                        "Abono inicial"
                )
        );

        List<FinancialTransaction> linked = findByPaymentId(payment.paymentId());
        assertEquals(1, linked.size());

        FinancialTransaction transaction = linked.getFirst();
        assertEquals(FinancialTransactionType.INCOME, transaction.getType());
        assertEquals(new BigDecimal("500000.00"), transaction.getAmount().getValue());
        assertEquals(paymentDate, transaction.getTransactionDate());
        assertEquals(FinancialCategory.SALES.name(), transaction.getCategory());
        assertEquals(FinancialTransactionSourceType.COMMERCIAL_ORDER, transaction.getSourceType());
        assertEquals(payment.paymentId(), transaction.getSourceId());
        assertEquals("Pago de orden comercial", transaction.getDescription());
        assertEquals("Abono inicial", transaction.getObservation());

        SynchronizeCommercialPaymentFinancialTransactionResult firstSync =
                synchronizeUseCase.execute(
                        new SynchronizeCommercialPaymentFinancialTransactionCommand(payment.paymentId())
                );
        SynchronizeCommercialPaymentFinancialTransactionResult secondSync =
                synchronizeUseCase.execute(
                        new SynchronizeCommercialPaymentFinancialTransactionCommand(payment.paymentId())
                );

        assertEquals(firstSync.transactionId(), secondSync.transactionId());
        assertEquals(1, findByPaymentId(payment.paymentId()).size());
        assertEquals(1, paymentRepository.findByOrderId(order.getId()).size());
    }

    @Test
    void twoPaymentsOnSameOrderCreateTwoIndependentIncomeTransactions() {
        Order order = createOrderWithTotal("1000000.00");

        RegisterPaymentResult first = registerPaymentUseCase.execute(
                new RegisterPaymentCommand(
                        order.getId(),
                        new BigDecimal("500000.00"),
                        LocalDate.of(2026, 8, 11),
                        null
                )
        );
        RegisterPaymentResult second = registerPaymentUseCase.execute(
                new RegisterPaymentCommand(
                        order.getId(),
                        new BigDecimal("500000.00"),
                        LocalDate.of(2026, 8, 12),
                        null
                )
        );

        assertEquals(2, paymentRepository.findByOrderId(order.getId()).size());
        assertEquals(1, findByPaymentId(first.paymentId()).size());
        assertEquals(1, findByPaymentId(second.paymentId()).size());

        FinancialTransaction incomeA = findByPaymentId(first.paymentId()).getFirst();
        FinancialTransaction incomeB = findByPaymentId(second.paymentId()).getFirst();
        assertEquals(new BigDecimal("500000.00"), incomeA.getAmount().getValue());
        assertEquals(new BigDecimal("500000.00"), incomeB.getAmount().getValue());
        assertTrue(!incomeA.getId().equals(incomeB.getId()));
    }

    @Test
    void manualIncomeRemainsManualAndHistoricalPaymentsAreNotBackfilled() {
        Order order = createOrderWithTotal("200000.00");

        // Payment preexistente sin ledger (simula histórico): se guarda directo en repo.
        var historical = com.magyen.platform.finance.domain.Payment.create(
                order.getId(),
                com.magyen.platform.finance.domain.PaymentAmount.of(new BigDecimal("100000.00")),
                LocalDate.of(2026, 7, 1),
                "Histórico"
        );
        paymentRepository.save(historical);

        assertTrue(
                financialTransactionRepository
                        .findBySourceTypeAndSourceId(
                                FinancialTransactionSourceType.COMMERCIAL_ORDER,
                                historical.getId()
                        )
                        .isEmpty()
        );

        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        new BigDecimal("25000.00"),
                        LocalDate.of(2026, 8, 15),
                        FinancialCategory.OTHER_INCOME.name(),
                        "Ingreso manual",
                        null,
                        FinancialTransactionSourceType.MANUAL,
                        null
                )
        );

        List<FinancialTransaction> manuals = financialTransactionRepository.findAllNewestFirst().stream()
                .filter(tx -> tx.getSourceType() == FinancialTransactionSourceType.MANUAL)
                .filter(tx -> "Ingreso manual".equals(tx.getDescription()))
                .toList();
        assertEquals(1, manuals.size());
        assertEquals(FinancialTransactionSourceType.MANUAL, manuals.getFirst().getSourceType());
    }

    private List<FinancialTransaction> findByPaymentId(UUID paymentId) {
        return financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.COMMERCIAL_ORDER, paymentId)
                .stream()
                .toList();
    }

    private Order createOrderWithTotal(String unitPrice) {
        LocalDate today = LocalDate.of(2026, 8, 1);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto prueba pago",
                1,
                "Tela",
                "Negro",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );

        Order order = Order.create(
                OrderNumber.of("ORD-PAY-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(7)),
                "Tester",
                "Orden para integración Payment→Finance",
                List.of(item)
        );

        return orderRepository.save(order);
    }
}
