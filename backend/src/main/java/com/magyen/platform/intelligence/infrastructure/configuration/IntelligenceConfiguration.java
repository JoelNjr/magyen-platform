package com.magyen.platform.intelligence.infrastructure.configuration;

import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.intelligence.application.usecase.GetInventoryReportUseCase;
import com.magyen.platform.intelligence.application.usecase.GetNotificationsUseCase;
import com.magyen.platform.intelligence.application.usecase.GetPaymentsReportUseCase;
import com.magyen.platform.intelligence.application.usecase.GetProductionReportUseCase;
import com.magyen.platform.intelligence.application.usecase.GetSalesReportUseCase;
import com.magyen.platform.intelligence.presentation.notification.mapper.NotificationPresentationMapper;
import com.magyen.platform.intelligence.presentation.report.mapper.IntelligencePresentationMapper;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los beans del módulo de intelligence que no se registran por estereotipos Spring.
 */
@Configuration
public class IntelligenceConfiguration {

    @Bean
    public IntelligencePresentationMapper intelligencePresentationMapper() {
        return new IntelligencePresentationMapper();
    }

    @Bean
    public NotificationPresentationMapper notificationPresentationMapper() {
        return new NotificationPresentationMapper();
    }

    @Bean
    public GetSalesReportUseCase getSalesReportUseCase(OrderRepository orderRepository) {
        return new GetSalesReportUseCase(orderRepository);
    }

    @Bean
    public GetProductionReportUseCase getProductionReportUseCase(
            ProductionOrderRepository productionOrderRepository
    ) {
        return new GetProductionReportUseCase(productionOrderRepository);
    }

    @Bean
    public GetInventoryReportUseCase getInventoryReportUseCase(
            InventoryItemRepository inventoryItemRepository
    ) {
        return new GetInventoryReportUseCase(inventoryItemRepository);
    }

    @Bean
    public GetPaymentsReportUseCase getPaymentsReportUseCase(PaymentRepository paymentRepository) {
        return new GetPaymentsReportUseCase(paymentRepository);
    }

    @Bean
    public GetNotificationsUseCase getNotificationsUseCase(
            OrderRepository orderRepository,
            ProductionOrderRepository productionOrderRepository,
            InventoryItemRepository inventoryItemRepository,
            PaymentRepository paymentRepository
    ) {
        return new GetNotificationsUseCase(
                orderRepository,
                productionOrderRepository,
                inventoryItemRepository,
                paymentRepository
        );
    }
}
