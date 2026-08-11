package com.magyen.platform.commercial.infrastructure.configuration;

import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.application.port.ProductionOrderCostPort;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.commercial.application.usecase.GetQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetQuotationsUseCase;
import com.magyen.platform.commercial.application.usecase.ReplaceOrderItemSizesUseCase;
import com.magyen.platform.commercial.application.usecase.UpdateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.UpdateOrderItemProductSpecificationUseCase;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.QuotationNumberGenerator;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.infrastructure.finance.OrderPaymentCollectionAdapter;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.CustomerPersistenceMapper;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.QuotationPersistenceMapper;
import com.magyen.platform.commercial.infrastructure.production.ProductionOrderCostAdapter;
import com.magyen.platform.commercial.presentation.customer.mapper.CustomerPresentationMapper;
import com.magyen.platform.commercial.presentation.order.mapper.OrderPresentationMapper;
import com.magyen.platform.commercial.presentation.quotation.mapper.QuotationPresentationMapper;
import com.magyen.platform.finance.application.usecase.GetPaymentsByOrderUseCase;
import com.magyen.platform.production.application.usecase.GetProductionCostsByCommercialOrderUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los beans del módulo comercial que no se registran por estereotipos Spring.
 */
@Configuration
public class CommercialConfiguration {

    @Bean
    public QuotationPresentationMapper quotationPresentationMapper() {
        return new QuotationPresentationMapper();
    }

    @Bean
    public OrderPresentationMapper orderPresentationMapper() {
        return new OrderPresentationMapper();
    }

    @Bean
    public CustomerPresentationMapper customerPresentationMapper() {
        return new CustomerPresentationMapper();
    }

    @Bean
    public QuotationPersistenceMapper quotationPersistenceMapper() {
        return new QuotationPersistenceMapper();
    }

    @Bean
    public CustomerPersistenceMapper customerPersistenceMapper() {
        return new CustomerPersistenceMapper();
    }

    @Bean
    public OrderPersistenceMapper orderPersistenceMapper() {
        return new OrderPersistenceMapper();
    }

    @Bean
    public CreateQuotationUseCase createQuotationUseCase(
            QuotationRepository quotationRepository,
            QuotationNumberGenerator quotationNumberGenerator
    ) {
        return new CreateQuotationUseCase(quotationRepository, quotationNumberGenerator);
    }

    @Bean
    public ApproveQuotationUseCase approveQuotationUseCase(QuotationRepository quotationRepository) {
        return new ApproveQuotationUseCase(quotationRepository);
    }

    @Bean
    public AddQuotationItemUseCase addQuotationItemUseCase(QuotationRepository quotationRepository) {
        return new AddQuotationItemUseCase(quotationRepository);
    }

    @Bean
    public GetQuotationsUseCase getQuotationsUseCase(QuotationRepository quotationRepository) {
        return new GetQuotationsUseCase(quotationRepository);
    }

    @Bean
    public GetCustomersUseCase getCustomersUseCase(CustomerRepository customerRepository) {
        return new GetCustomersUseCase(customerRepository);
    }

    @Bean
    public CreateCustomerUseCase createCustomerUseCase(CustomerRepository customerRepository) {
        return new CreateCustomerUseCase(customerRepository);
    }

    @Bean
    public UpdateCustomerUseCase updateCustomerUseCase(CustomerRepository customerRepository) {
        return new UpdateCustomerUseCase(customerRepository);
    }

    @Bean
    public GetQuotationUseCase getQuotationUseCase(
            QuotationRepository quotationRepository,
            OrderRepository orderRepository
    ) {
        return new GetQuotationUseCase(quotationRepository, orderRepository);
    }

    @Bean
    public CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase(
            QuotationRepository quotationRepository,
            OrderRepository orderRepository
    ) {
        return new CreateOrderFromQuotationUseCase(quotationRepository, orderRepository);
    }

    @Bean
    public GetOrdersUseCase getOrdersUseCase(OrderRepository orderRepository) {
        return new GetOrdersUseCase(orderRepository);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(OrderRepository orderRepository) {
        return new GetOrderUseCase(orderRepository);
    }

    @Bean
    public OrderPaymentCollectionPort orderPaymentCollectionPort(
            GetPaymentsByOrderUseCase getPaymentsByOrderUseCase
    ) {
        return new OrderPaymentCollectionAdapter(getPaymentsByOrderUseCase);
    }

    @Bean
    public ProductionOrderCostPort productionOrderCostPort(
            GetProductionCostsByCommercialOrderUseCase getProductionCostsByCommercialOrderUseCase
    ) {
        return new ProductionOrderCostAdapter(getProductionCostsByCommercialOrderUseCase);
    }

    @Bean
    public GetOrderProfitabilityUseCase getOrderProfitabilityUseCase(
            OrderRepository orderRepository,
            OrderPaymentCollectionPort orderPaymentCollectionPort,
            ProductionOrderCostPort productionOrderCostPort
    ) {
        return new GetOrderProfitabilityUseCase(
                orderRepository,
                orderPaymentCollectionPort,
                productionOrderCostPort
        );
    }

    @Bean
    public ReplaceOrderItemSizesUseCase replaceOrderItemSizesUseCase(OrderRepository orderRepository) {
        return new ReplaceOrderItemSizesUseCase(orderRepository);
    }

    @Bean
    public UpdateOrderItemProductSpecificationUseCase updateOrderItemProductSpecificationUseCase(
            OrderRepository orderRepository
    ) {
        return new UpdateOrderItemProductSpecificationUseCase(orderRepository);
    }
}
