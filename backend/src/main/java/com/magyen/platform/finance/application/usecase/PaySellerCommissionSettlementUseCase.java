package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.PaySellerCommissionSettlementCommand;
import com.magyen.platform.finance.application.dto.PaySellerCommissionSettlementResult;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort.EmployeeSellerCommissionsSnapshot;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.SellerCommissionSettlement;
import com.magyen.platform.finance.domain.SellerCommissionSettlementRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.domain.exception.PayrollEmployeeNotFoundException;
import com.magyen.platform.finance.domain.exception.SellerCommissionAlreadyPaidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

/**
 * Paga la comisión de un vendedor-mes: una liquidación PAID y un gasto EXPENSE.
 * <p>
 * Ambos se confirman en la misma transacción. El monto es el cálculo vivo al pagar
 * y queda congelado. Un segundo pago del mismo mes termina en conflicto.
 */
public class PaySellerCommissionSettlementUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final SellerCommissionSettlementRepository sellerCommissionSettlementRepository;
    private final EmployeeSellerCommissionsPort employeeSellerCommissionsPort;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final Clock clock;

    public PaySellerCommissionSettlementUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            SellerCommissionSettlementRepository sellerCommissionSettlementRepository,
            EmployeeSellerCommissionsPort employeeSellerCommissionsPort,
            FinancialTransactionRepository financialTransactionRepository,
            Clock clock
    ) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
        this.sellerCommissionSettlementRepository = Objects.requireNonNull(
                sellerCommissionSettlementRepository,
                "Seller commission settlement repository must not be null"
        );
        this.employeeSellerCommissionsPort = Objects.requireNonNull(
                employeeSellerCommissionsPort,
                "Employee seller commissions port must not be null"
        );
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Transactional
    public PaySellerCommissionSettlementResult execute(PaySellerCommissionSettlementCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.employeeId(), "Employee id must not be null");
        SellerCommissionSettlement.requirePayableMonth(command.periodStart());
        LocalDate periodEnd = SellerCommissionSettlement.periodEndOf(command.periodStart());

        PayrollEmployee employee = payrollEmployeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new PayrollEmployeeNotFoundException(
                        "Payroll employee not found: " + command.employeeId()
                ));

        if (sellerCommissionSettlementRepository
                .findByEmployeeIdAndPeriodStart(employee.getId(), command.periodStart())
                .isPresent()) {
            throw new SellerCommissionAlreadyPaidException();
        }

        EmployeeSellerCommissionsSnapshot snapshot = employeeSellerCommissionsPort.findCommissions(
                employee.getId(),
                command.periodStart(),
                periodEnd
        );
        if (snapshot.accumulatedCommission() == null
                || snapshot.accumulatedCommission().compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceDomainException("A zero commission cannot be paid");
        }

        LocalDate paymentDate = command.paymentDate() == null
                ? LocalDate.now(clock)
                : command.paymentDate();
        LocalDateTime paidAt = LocalDateTime.now(clock);
        UUID settlementId = UUID.randomUUID();
        String description = "Comisión vendedor " + employee.getDisplayName()
                + " - " + YearMonth.from(command.periodStart());

        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                FinancialAmount.of(snapshot.accumulatedCommission()),
                paymentDate,
                FinancialCategory.PAYROLL.name(),
                description,
                command.observation(),
                FinancialTransactionSourceType.SELLER_COMMISSION,
                settlementId
        );
        SellerCommissionSettlement settlement = SellerCommissionSettlement.createPaid(
                settlementId,
                employee.getId(),
                command.periodStart(),
                periodEnd,
                FinancialAmount.of(snapshot.totalSales()),
                snapshot.numberOfEligibleOrders(),
                FinancialAmount.of(snapshot.accumulatedCommission()),
                paymentDate,
                paidAt,
                transaction.getId(),
                command.observation()
        );

        try {
            SellerCommissionSettlement savedSettlement = sellerCommissionSettlementRepository.save(settlement);
            FinancialTransaction savedTransaction = financialTransactionRepository.save(transaction);
            return new PaySellerCommissionSettlementResult(
                    savedSettlement.getId(),
                    savedSettlement.getEmployeeId(),
                    employee.getDisplayName(),
                    savedSettlement.getPeriodStart(),
                    savedSettlement.getPeriodEnd(),
                    savedSettlement.getSalesSnapshot().getValue(),
                    savedSettlement.getOrderCountSnapshot(),
                    savedSettlement.getCommissionSnapshot().getValue(),
                    savedSettlement.getStatus(),
                    savedSettlement.getActualPaymentDate(),
                    savedSettlement.getPaidAt(),
                    savedTransaction.getId(),
                    savedSettlement.getObservation()
            );
        } catch (DataIntegrityViolationException exception) {
            throw new SellerCommissionAlreadyPaidException();
        }
    }
}
