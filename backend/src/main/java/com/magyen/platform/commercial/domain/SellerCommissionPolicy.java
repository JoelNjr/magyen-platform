package com.magyen.platform.commercial.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

/**
 * Comisión V1 de vendedor: 5 % sobre el valor comercial de pedidos con vendedor.
 * <p>
 * Acumulan {@link OrderStatus#CONFIRMED}, {@link OrderStatus#IN_PRODUCTION},
 * {@link OrderStatus#READY_FOR_DELIVERY}, {@link OrderStatus#DELIVERED} y {@link OrderStatus#CLOSED}.
 * El cálculo es analítico: no crea asientos Finance ni altera rentabilidad.
 */
public final class SellerCommissionPolicy {

    public static final BigDecimal RATE = new BigDecimal("0.05");
    public static final BigDecimal RATE_PERCENTAGE = new BigDecimal("5.00");
    public static final Set<OrderStatus> ELIGIBLE_STATUSES = EnumSet.of(
            OrderStatus.CONFIRMED,
            OrderStatus.IN_PRODUCTION,
            OrderStatus.READY_FOR_DELIVERY,
            OrderStatus.DELIVERED,
            OrderStatus.CLOSED
    );

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private SellerCommissionPolicy() {
    }

    public static boolean includes(OrderStatus status) {
        return status != null && ELIGIBLE_STATUSES.contains(status);
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

    public static BigDecimal commissionOnSales(BigDecimal totalSales) {
        if (totalSales == null || totalSales.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO_MONEY;
        }
        return totalSales.multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal money(BigDecimal amount) {
        if (amount == null) {
            return ZERO_MONEY;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
