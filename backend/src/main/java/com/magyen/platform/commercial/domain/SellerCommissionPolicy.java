package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.OrderDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Comisión V1 de vendedor: 5 % sobre {@code order.total}, en el mes de confirmación.
 * <p>
 * El estado del pedido, la entrega, la producción y los pagos no deciden la comisión.
 * El cálculo es analítico: no crea asientos Finance ni liquida al vendedor.
 */
public final class SellerCommissionPolicy {

    public static final BigDecimal RATE = new BigDecimal("0.05");
    public static final BigDecimal RATE_PERCENTAGE = new BigDecimal("5.00");
    public static final LocalDate PAYABLE_FROM = LocalDate.of(2026, 9, 1);

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private SellerCommissionPolicy() {
    }

    public static BigDecimal commissionForOrder(BigDecimal orderTotal) {
        if (orderTotal == null || orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO_MONEY;
        }
        return orderTotal.multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal commissionOnSales(BigDecimal totalSales) {
        return commissionForOrder(totalSales);
    }

    public static BigDecimal money(BigDecimal amount) {
        if (amount == null) {
            return ZERO_MONEY;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean confirmationDateInRange(LocalDate confirmationDate, LocalDate fromDate, LocalDate toDate) {
        if (confirmationDate == null) {
            return false;
        }
        if (fromDate != null && confirmationDate.isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && confirmationDate.isAfter(toDate)) {
            return false;
        }
        return true;
    }

    public static void requireCalendarMonth(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new OrderDomainException("A commission month requires periodStart and periodEnd");
        }
        LocalDate monthStart = fromDate.withDayOfMonth(1);
        LocalDate monthEnd = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
        if (!fromDate.equals(monthStart) || !toDate.equals(monthEnd)) {
            throw new OrderDomainException("Commission period must be exactly one calendar month");
        }
    }

    public static SellerCommissionReadStatus readStatus(LocalDate periodStart) {
        if (periodStart.isBefore(PAYABLE_FROM)) {
            return SellerCommissionReadStatus.HISTORICAL;
        }
        return SellerCommissionReadStatus.CALCULATED;
    }
}
