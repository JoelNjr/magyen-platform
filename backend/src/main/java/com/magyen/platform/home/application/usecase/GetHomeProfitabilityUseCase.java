package com.magyen.platform.home.application.usecase;

import com.magyen.platform.home.application.HomePeriodResolver;
import com.magyen.platform.home.application.HomePeriodResolver.ResolvedPeriod;
import com.magyen.platform.home.application.dto.GetHomeProfitabilityQuery;
import com.magyen.platform.home.application.dto.GetHomeProfitabilityResult;
import com.magyen.platform.home.application.dto.HomeProfitabilitySummary;
import com.magyen.platform.home.application.port.CommercialDashboardPort;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Lee la rentabilidad Home para un período, sin tocar el resto del dashboard.
 * <p>
 * Reutiliza el cálculo comercial existente. No recalcula costos ni márgenes.
 */
public class GetHomeProfitabilityUseCase {

    private final CommercialDashboardPort commercialDashboardPort;
    private final Clock clock;

    public GetHomeProfitabilityUseCase(
            CommercialDashboardPort commercialDashboardPort,
            Clock clock
    ) {
        this.commercialDashboardPort = Objects.requireNonNull(
                commercialDashboardPort,
                "Commercial dashboard port must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public GetHomeProfitabilityResult execute(GetHomeProfitabilityQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        ResolvedPeriod period = HomePeriodResolver.resolve(query.fromDate(), query.toDate(), clock);
        CommercialDashboardPort.HomeProfitabilitySummarySnapshot profitability =
                commercialDashboardPort.getProfitabilitySummary(period.fromDate(), period.toDate());

        return new GetHomeProfitabilityResult(
                period.fromDate(),
                period.toDate(),
                Instant.now(clock),
                new HomeProfitabilitySummary(
                        profitability.evaluatedOrderCount(),
                        profitability.completeOrderCount(),
                        profitability.partiallyUnvaluedOrderCount(),
                        profitability.noCostDataOrderCount(),
                        profitability.totalOrderValue(),
                        profitability.totalDirectCost(),
                        profitability.totalDirectProfit(),
                        profitability.averageMarginPercentage(),
                        profitability.unvaluedCostCount()
                )
        );
    }
}
