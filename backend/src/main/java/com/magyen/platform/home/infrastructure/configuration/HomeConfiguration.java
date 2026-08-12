package com.magyen.platform.home.infrastructure.configuration;

import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.finance.application.usecase.GetFinancialPeriodSummaryUseCase;
import com.magyen.platform.finance.application.usecase.GetOverdueFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetPendingFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetUpcomingFinancialObligationOccurrencesUseCase;
import com.magyen.platform.home.application.port.CommercialDashboardPort;
import com.magyen.platform.home.application.port.FinanceDashboardPort;
import com.magyen.platform.home.application.port.InventoryDashboardPort;
import com.magyen.platform.home.application.port.ProductionDashboardPort;
import com.magyen.platform.home.application.usecase.GetHomeDashboardUseCase;
import com.magyen.platform.home.infrastructure.commercial.CommercialDashboardAdapter;
import com.magyen.platform.home.infrastructure.finance.FinanceDashboardAdapter;
import com.magyen.platform.home.infrastructure.inventory.InventoryDashboardAdapter;
import com.magyen.platform.home.infrastructure.production.ProductionDashboardAdapter;
import com.magyen.platform.home.presentation.dashboard.mapper.HomeDashboardPresentationMapper;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemsUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOrdersUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Ensambla los beans del módulo Home que no se registran por estereotipos Spring.
 */
@Configuration
public class HomeConfiguration {

    @Bean
    public HomeDashboardPresentationMapper homeDashboardPresentationMapper() {
        return new HomeDashboardPresentationMapper();
    }

    @Bean
    public FinanceDashboardPort financeDashboardPort(
            GetFinancialPeriodSummaryUseCase getFinancialPeriodSummaryUseCase,
            GetPendingFinancialObligationOccurrencesUseCase getPendingFinancialObligationOccurrencesUseCase,
            GetOverdueFinancialObligationOccurrencesUseCase getOverdueFinancialObligationOccurrencesUseCase,
            GetUpcomingFinancialObligationOccurrencesUseCase getUpcomingFinancialObligationOccurrencesUseCase
    ) {
        return new FinanceDashboardAdapter(
                getFinancialPeriodSummaryUseCase,
                getPendingFinancialObligationOccurrencesUseCase,
                getOverdueFinancialObligationOccurrencesUseCase,
                getUpcomingFinancialObligationOccurrencesUseCase
        );
    }

    @Bean
    public CommercialDashboardPort commercialDashboardPort(
            GetOrdersUseCase getOrdersUseCase,
            OrderPaymentCollectionPort orderPaymentCollectionPort,
            GetOrderProfitabilityUseCase getOrderProfitabilityUseCase
    ) {
        return new CommercialDashboardAdapter(
                getOrdersUseCase,
                orderPaymentCollectionPort,
                getOrderProfitabilityUseCase
        );
    }

    @Bean
    public InventoryDashboardPort inventoryDashboardPort(GetInventoryItemsUseCase getInventoryItemsUseCase) {
        return new InventoryDashboardAdapter(getInventoryItemsUseCase);
    }

    @Bean
    public ProductionDashboardPort productionDashboardPort(
            GetProductionOrdersUseCase getProductionOrdersUseCase
    ) {
        return new ProductionDashboardAdapter(getProductionOrdersUseCase);
    }

    @Bean
    public GetHomeDashboardUseCase getHomeDashboardUseCase(
            FinanceDashboardPort financeDashboardPort,
            CommercialDashboardPort commercialDashboardPort,
            InventoryDashboardPort inventoryDashboardPort,
            ProductionDashboardPort productionDashboardPort,
            Clock clock
    ) {
        return new GetHomeDashboardUseCase(
                financeDashboardPort,
                commercialDashboardPort,
                inventoryDashboardPort,
                productionDashboardPort,
                clock
        );
    }
}
