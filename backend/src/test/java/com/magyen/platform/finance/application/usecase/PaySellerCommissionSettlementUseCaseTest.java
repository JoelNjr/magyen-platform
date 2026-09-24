package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsQuery;
import com.magyen.platform.finance.application.dto.PaySellerCommissionSettlementCommand;
import com.magyen.platform.finance.application.dto.PaySellerCommissionSettlementResult;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.SellerCommissionSettlementRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.domain.exception.SellerCommissionAlreadyPaidException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PaySellerCommissionSettlementUseCaseTest {

    private static final LocalDate SEPTEMBER_START = LocalDate.of(2026, 9, 1);

    @Autowired
    private PaySellerCommissionSettlementUseCase paySellerCommissionSettlementUseCase;

    @Autowired
    private GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private DeactivatePayrollEmployeeUseCase deactivatePayrollEmployeeUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private SellerCommissionSettlementRepository sellerCommissionSettlementRepository;

    @Test
    void septemberPaymentCreatesOneSettlementAndOnePayrollExpense() {
        CreatePayrollEmployeeResult seller = createSeller("Vendedor-Pago-" + suffix());
        Order confirmed = saveOrder(seller.employeeId(), OrderStatus.CONFIRMED, "200000.00", LocalDate.of(2026, 9, 4));
        saveOrder(seller.employeeId(), OrderStatus.DELIVERED, "300000.00", LocalDate.of(2026, 9, 18));
        long financeBefore = financialTransactionRepository.findAllNewestFirst().size();

        PaySellerCommissionSettlementResult paid = paySellerCommissionSettlementUseCase.execute(
                new PaySellerCommissionSettlementCommand(
                        seller.employeeId(),
                        SEPTEMBER_START,
                        LocalDate.of(2026, 9, 30),
                        "comisión septiembre"
                )
        );

        assertEquals(2, paid.orderCountSnapshot());
        assertEquals(new BigDecimal("500000.00"), paid.salesSnapshot());
        assertEquals(new BigDecimal("25000.00"), paid.commissionSnapshot());
        assertEquals("PAID", paid.status().name());
        assertEquals(LocalDate.of(2026, 9, 30), paid.actualPaymentDate());
        assertEquals(financeBefore + 1, financialTransactionRepository.findAllNewestFirst().size());

        FinancialTransaction expense = financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.SELLER_COMMISSION, paid.settlementId())
                .orElseThrow();
        assertEquals(FinancialTransactionType.EXPENSE, expense.getType());
        assertEquals(FinancialCategory.PAYROLL.name(), expense.getCategory());
        assertEquals(paid.commissionSnapshot(), expense.getAmount().getValue());
        assertEquals(paid.actualPaymentDate(), expense.getTransactionDate());
        assertEquals(paid.financialTransactionId(), expense.getId());
        assertTrue(expense.getDescription().contains(seller.displayName()));
        assertTrue(expense.getDescription().contains("2026-09"));

        var readModel = getPayrollEmployeeCommissionsUseCase.execute(
                new GetPayrollEmployeeCommissionsQuery(
                        seller.employeeId(),
                        SEPTEMBER_START,
                        LocalDate.of(2026, 9, 30)
                )
        );
        assertEquals("PAID", readModel.settlementStatus());
        assertEquals(paid.commissionSnapshot(), readModel.paidCommissionSnapshot());
        assertEquals(2, readModel.orders().size());

        saveOrder(
                confirmed.getId(),
                seller.employeeId(),
                OrderStatus.CONFIRMED,
                "900000.00",
                LocalDate.of(2026, 9, 4)
        );
        assertEquals(
                new BigDecimal("25000.00"),
                sellerCommissionSettlementRepository.findById(paid.settlementId())
                        .orElseThrow()
                        .getCommissionSnapshot()
                        .getValue()
        );
        assertEquals(
                new BigDecimal("25000.00"),
                expense.getAmount().getValue()
        );
    }

    @Test
    void duplicatePaymentDoesNotCreateASecondExpense() {
        CreatePayrollEmployeeResult seller = createSeller("Vendedor-Dup-" + suffix());
        saveOrder(seller.employeeId(), OrderStatus.CONFIRMED, "100000.00", LocalDate.of(2026, 9, 2));
        paySellerCommissionSettlementUseCase.execute(new PaySellerCommissionSettlementCommand(
                seller.employeeId(),
                SEPTEMBER_START,
                LocalDate.of(2026, 9, 30),
                null
        ));
        long financeAfterFirst = financialTransactionRepository.findAllNewestFirst().size();

        assertThrows(SellerCommissionAlreadyPaidException.class, () ->
                paySellerCommissionSettlementUseCase.execute(new PaySellerCommissionSettlementCommand(
                        seller.employeeId(),
                        SEPTEMBER_START,
                        LocalDate.of(2026, 10, 1),
                        null
                )));
        assertEquals(financeAfterFirst, financialTransactionRepository.findAllNewestFirst().size());
        assertEquals(1, sellerCommissionSettlementRepository.findByPeriodStart(SEPTEMBER_START).stream()
                .filter(settlement -> seller.employeeId().equals(settlement.getEmployeeId()))
                .count());
    }

    @Test
    void augustAndZeroCommissionAreRejected() {
        CreatePayrollEmployeeResult seller = createSeller("Vendedor-Ago-" + suffix());
        saveOrder(seller.employeeId(), OrderStatus.CLOSED, "100000.00", LocalDate.of(2026, 8, 10));
        assertThrows(FinanceDomainException.class, () ->
                paySellerCommissionSettlementUseCase.execute(new PaySellerCommissionSettlementCommand(
                        seller.employeeId(),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31),
                        null
                )));
        assertThrows(FinanceDomainException.class, () ->
                paySellerCommissionSettlementUseCase.execute(new PaySellerCommissionSettlementCommand(
                        seller.employeeId(),
                        LocalDate.of(2026, 9, 15),
                        LocalDate.of(2026, 9, 30),
                        null
                )));

        CreatePayrollEmployeeResult empty = createSeller("Vendedor-Cero-" + suffix());
        assertThrows(FinanceDomainException.class, () ->
                paySellerCommissionSettlementUseCase.execute(new PaySellerCommissionSettlementCommand(
                        empty.employeeId(),
                        SEPTEMBER_START,
                        LocalDate.of(2026, 9, 30),
                        null
                )));
    }

    @Test
    void inactiveAndNonParticipantHistoricalSellersCanStillBePaid() {
        CreatePayrollEmployeeResult inactive = createSeller("Vendedor-Inact-" + suffix());
        saveOrder(inactive.employeeId(), OrderStatus.IN_PRODUCTION, "400000.00", LocalDate.of(2026, 9, 9));
        deactivatePayrollEmployeeUseCase.execute(new DeactivatePayrollEmployeeCommand(inactive.employeeId()));

        PaySellerCommissionSettlementResult inactivePaid = paySellerCommissionSettlementUseCase.execute(
                new PaySellerCommissionSettlementCommand(
                        inactive.employeeId(),
                        SEPTEMBER_START,
                        LocalDate.of(2026, 9, 20),
                        null
                )
        );
        assertEquals(new BigDecimal("20000.00"), inactivePaid.commissionSnapshot());

        CreatePayrollEmployeeResult historical = createFixed("Vendedor-Hist-" + suffix());
        saveOrder(historical.employeeId(), OrderStatus.READY_FOR_DELIVERY, "100000.00", LocalDate.of(2026, 9, 11));
        PaySellerCommissionSettlementResult historicalPaid = paySellerCommissionSettlementUseCase.execute(
                new PaySellerCommissionSettlementCommand(
                        historical.employeeId(),
                        SEPTEMBER_START,
                        LocalDate.of(2026, 9, 21),
                        null
                )
        );
        assertEquals(new BigDecimal("5000.00"), historicalPaid.commissionSnapshot());
    }

    private Order saveOrder(UUID sellerId, OrderStatus status, String unitPrice, LocalDate confirmationDate) {
        return saveOrder(UUID.randomUUID(), sellerId, status, unitPrice, confirmationDate);
    }

    private Order saveOrder(
            UUID orderId,
            UUID sellerId,
            OrderStatus status,
            String unitPrice,
            LocalDate confirmationDate
    ) {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto comisión pago",
                1,
                "Sudáfrica",
                "Negro",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );
        Money total = item.getSubtotal();
        return orderRepository.save(Order.reconstitute(
                orderId,
                OrderNumber.of("ORD-PAY-" + suffix()),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmationDate,
                status,
                DeliveryCommitment.of(confirmationDate.plusDays(7)),
                PaymentSummary.forConfirmedOrder(total),
                sellerId,
                null,
                "Pedido comisión pago",
                List.of(item)
        ));
    }

    private CreatePayrollEmployeeResult createSeller(String name) {
        return createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                name,
                PayrollCompensationType.FIXED_PAYROLL,
                new BigDecimal("1500000.00"),
                LocalDate.of(2026, 8, 1),
                null,
                true
        ));
    }

    private CreatePayrollEmployeeResult createFixed(String name) {
        return createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                name,
                PayrollCompensationType.FIXED_PAYROLL,
                new BigDecimal("1500000.00"),
                LocalDate.of(2026, 8, 1),
                null
        ));
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
