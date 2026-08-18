package com.magyen.platform.finance.infrastructure.commercial;

import com.magyen.platform.commercial.application.dto.GetSellerCommissionQuery;
import com.magyen.platform.commercial.application.dto.GetSellerCommissionResult;
import com.magyen.platform.commercial.application.usecase.GetSellerCommissionPerformanceUseCase;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort;

import java.time.LocalDate;
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
        GetSellerCommissionResult result = getSellerCommissionPerformanceUseCase.execute(
                new GetSellerCommissionQuery(sellerEmployeeId, fromDate, toDate)
        );
        return new EmployeeSellerCommissionsSnapshot(
                result.sellerEmployeeId(),
                result.fromDate(),
                result.toDate(),
                result.numberOfEligibleOrders(),
                result.totalSales(),
                result.commissionRate(),
                result.accumulatedCommission()
        );
    }
}
