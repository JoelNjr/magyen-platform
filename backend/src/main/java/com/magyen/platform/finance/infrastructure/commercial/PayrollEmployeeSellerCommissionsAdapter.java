package com.magyen.platform.finance.infrastructure.commercial;

import com.magyen.platform.commercial.application.dto.GetSellerCommissionQuery;
import com.magyen.platform.commercial.application.dto.GetSellerCommissionResult;
import com.magyen.platform.commercial.application.dto.SellerCommissionOrderLine;
import com.magyen.platform.commercial.application.usecase.GetSellerCommissionPerformanceUseCase;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Finance → Commercial para comisión analítica de vendedor.
 */
public class PayrollEmployeeSellerCommissionsAdapter implements EmployeeSellerCommissionsPort {

    private final GetSellerCommissionPerformanceUseCase getSellerCommissionPerformanceUseCase;

    public PayrollEmployeeSellerCommissionsAdapter(
            GetSellerCommissionPerformanceUseCase getSellerCommissionPerformanceUseCase
    ) {
        this.getSellerCommissionPerformanceUseCase = Objects.requireNonNull(
                getSellerCommissionPerformanceUseCase,
                "Get seller commission performance use case must not be null"
        );
    }

    @Override
    public EmployeeSellerCommissionsSnapshot findCommissions(
            UUID sellerEmployeeId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return toSnapshot(getSellerCommissionPerformanceUseCase.execute(
                new GetSellerCommissionQuery(sellerEmployeeId, fromDate, toDate)
        ));
    }

    @Override
    public List<EmployeeSellerCommissionsSnapshot> findCommissionsForMonth(LocalDate fromDate, LocalDate toDate) {
        return getSellerCommissionPerformanceUseCase.listForMonth(fromDate, toDate).stream()
                .map(PayrollEmployeeSellerCommissionsAdapter::toSnapshot)
                .toList();
    }

    @Override
    public EmployeeSellerCommissionsSnapshot findUnboundedCommissions(UUID sellerEmployeeId) {
        return toSnapshot(getSellerCommissionPerformanceUseCase.executeUnbounded(
                new GetSellerCommissionQuery(sellerEmployeeId, null, null)
        ));
    }

    private static EmployeeSellerCommissionsSnapshot toSnapshot(GetSellerCommissionResult result) {
        List<CommissionOrderLine> orders = result.orders().stream()
                .map(PayrollEmployeeSellerCommissionsAdapter::toLine)
                .toList();
        return new EmployeeSellerCommissionsSnapshot(
                result.sellerEmployeeId(),
                result.fromDate(),
                result.toDate(),
                result.numberOfEligibleOrders(),
                result.totalSales(),
                result.commissionRate(),
                result.accumulatedCommission(),
                result.settlementStatus().name(),
                orders
        );
    }

    private static CommissionOrderLine toLine(SellerCommissionOrderLine line) {
        return new CommissionOrderLine(
                line.orderId(),
                line.orderNumber(),
                line.customerName(),
                line.confirmationDate(),
                line.orderTotal(),
                line.commissionRate(),
                line.commissionAmount()
        );
    }
}
