package com.magyen.platform.commercial.infrastructure.configuration;

import com.magyen.platform.commercial.application.CustomerNameResolver;
import com.magyen.platform.commercial.application.SellerNameResolver;
import com.magyen.platform.commercial.application.CommercialCatalogValidator;
import com.magyen.platform.commercial.application.port.CommercialCatalogPort;
import com.magyen.platform.commercial.application.port.CommercialSellerEmployeePort;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.application.port.PlotterOrderCostPort;
import com.magyen.platform.commercial.application.port.ProductionOrderCostPort;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetCommercialCatalogsUseCase;
import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.application.usecase.GetSellersUseCase;
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
import com.magyen.platform.commercial.domain.SellerRepository;
import com.magyen.platform.commercial.infrastructure.administration.CommercialCatalogAdapter;
import com.magyen.platform.commercial.infrastructure.finance.CommercialSellerEmployeeAdapter;
import com.magyen.platform.commercial.infrastructure.finance.OrderPaymentCollectionAdapter;
import com.magyen.platform.commercial.infrastructure.plotter.PlotterOrderCostAdapter;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.CustomerPersistenceMapper;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.QuotationPersistenceMapper;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.SellerPersistenceMapper;
import com.magyen.platform.commercial.infrastructure.production.ProductionOrderCostAdapter;
import com.magyen.platform.commercial.presentation.catalog.mapper.CommercialCatalogPresentationMapper;
import com.magyen.platform.commercial.presentation.customer.mapper.CustomerPresentationMapper;
import com.magyen.platform.commercial.presentation.order.mapper.OrderPresentationMapper;
import com.magyen.platform.commercial.presentation.quotation.mapper.QuotationPresentationMapper;
import com.magyen.platform.commercial.presentation.seller.mapper.SellerPresentationMapper;
import com.magyen.platform.administration.application.usecase.ListAdministrationCatalogEntriesUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeesUseCase;
import com.magyen.platform.finance.application.usecase.GetPaymentsByOrderUseCase;
import com.magyen.platform.plotter.application.usecase.GetInternalPlotterOrderCostsUseCase;
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
    public CommercialCatalogPresentationMapper commercialCatalogPresentationMapper() {
        return new CommercialCatalogPresentationMapper();
    }

    @Bean
    public GetCommercialCatalogsUseCase getCommercialCatalogsUseCase(CommercialCatalogPort commercialCatalogPort) {
        return new GetCommercialCatalogsUseCase(commercialCatalogPort);
    }

    @Bean
    public CustomerPresentationMapper customerPresentationMapper() {
        return new CustomerPresentationMapper();
    }

    @Bean
    public SellerPresentationMapper sellerPresentationMapper() {
        return new SellerPresentationMapper();
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
    public SellerPersistenceMapper sellerPersistenceMapper() {
        return new SellerPersistenceMapper();
    }

    @Bean
    public CommercialCatalogPort commercialCatalogPort(
            ListAdministrationCatalogEntriesUseCase listAdministrationCatalogEntriesUseCase
    ) {
        return new CommercialCatalogAdapter(listAdministrationCatalogEntriesUseCase);
    }

    @Bean
    public CommercialCatalogValidator commercialCatalogValidator(CommercialCatalogPort commercialCatalogPort) {
        return new CommercialCatalogValidator(commercialCatalogPort);
    }

    @Bean
    public CommercialSellerEmployeePort commercialSellerEmployeePort(
            GetPayrollEmployeeUseCase getPayrollEmployeeUseCase,
            GetPayrollEmployeesUseCase getPayrollEmployeesUseCase
    ) {
        return new CommercialSellerEmployeeAdapter(
                getPayrollEmployeeUseCase,
                getPayrollEmployeesUseCase
        );
    }

    @Bean
    public SellerNameResolver sellerNameResolver(
            CommercialSellerEmployeePort commercialSellerEmployeePort,
            SellerRepository leftoverSellerRepository
    ) {
        return new SellerNameResolver(commercialSellerEmployeePort, leftoverSellerRepository);
    }

    @Bean
    public CustomerNameResolver customerNameResolver(CustomerRepository customerRepository) {
        return new CustomerNameResolver(customerRepository);
    }

    @Bean
    public GetSellersUseCase getSellersUseCase(CommercialSellerEmployeePort commercialSellerEmployeePort) {
        return new GetSellersUseCase(commercialSellerEmployeePort);
    }

    @Bean
    public CreateQuotationUseCase createQuotationUseCase(
            QuotationRepository quotationRepository,
            QuotationNumberGenerator quotationNumberGenerator,
            SellerNameResolver sellerNameResolver
    ) {
        return new CreateQuotationUseCase(
                quotationRepository,
                quotationNumberGenerator,
                sellerNameResolver
        );
    }

    @Bean
    public ApproveQuotationUseCase approveQuotationUseCase(QuotationRepository quotationRepository) {
        return new ApproveQuotationUseCase(quotationRepository);
    }

    @Bean
    public AddQuotationItemUseCase addQuotationItemUseCase(
            QuotationRepository quotationRepository,
            CommercialCatalogValidator commercialCatalogValidator
    ) {
        return new AddQuotationItemUseCase(quotationRepository, commercialCatalogValidator);
    }

    @Bean
    public GetQuotationsUseCase getQuotationsUseCase(
            QuotationRepository quotationRepository,
            SellerNameResolver sellerNameResolver
    ) {
        return new GetQuotationsUseCase(quotationRepository, sellerNameResolver);
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
            OrderRepository orderRepository,
            SellerNameResolver sellerNameResolver
    ) {
        return new GetQuotationUseCase(quotationRepository, orderRepository, sellerNameResolver);
    }

    @Bean
    public CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase(
            QuotationRepository quotationRepository,
            OrderRepository orderRepository
    ) {
        return new CreateOrderFromQuotationUseCase(quotationRepository, orderRepository);
    }

    @Bean
    public GetOrdersUseCase getOrdersUseCase(
            OrderRepository orderRepository,
            QuotationRepository quotationRepository,
            SellerNameResolver sellerNameResolver,
            CustomerNameResolver customerNameResolver
    ) {
        return new GetOrdersUseCase(
                orderRepository,
                quotationRepository,
                sellerNameResolver,
                customerNameResolver
        );
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(
            OrderRepository orderRepository,
            QuotationRepository quotationRepository,
            SellerNameResolver sellerNameResolver,
            CustomerNameResolver customerNameResolver
    ) {
        return new GetOrderUseCase(
                orderRepository,
                quotationRepository,
                sellerNameResolver,
                customerNameResolver
        );
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
    public PlotterOrderCostPort plotterOrderCostPort(
            GetInternalPlotterOrderCostsUseCase getInternalPlotterOrderCostsUseCase
    ) {
        return new PlotterOrderCostAdapter(getInternalPlotterOrderCostsUseCase);
    }

    @Bean
    public GetOrderProfitabilityUseCase getOrderProfitabilityUseCase(
            OrderRepository orderRepository,
            OrderPaymentCollectionPort orderPaymentCollectionPort,
            ProductionOrderCostPort productionOrderCostPort,
            PlotterOrderCostPort plotterOrderCostPort
    ) {
        return new GetOrderProfitabilityUseCase(
                orderRepository,
                orderPaymentCollectionPort,
                productionOrderCostPort,
                plotterOrderCostPort
        );
    }

    @Bean
    public ReplaceOrderItemSizesUseCase replaceOrderItemSizesUseCase(OrderRepository orderRepository) {
        return new ReplaceOrderItemSizesUseCase(orderRepository);
    }

    @Bean
    public UpdateOrderItemProductSpecificationUseCase updateOrderItemProductSpecificationUseCase(
            OrderRepository orderRepository,
            CommercialCatalogValidator commercialCatalogValidator
    ) {
        return new UpdateOrderItemProductSpecificationUseCase(orderRepository, commercialCatalogValidator);
    }
}
