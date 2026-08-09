package com.magyen.platform.commercial.infrastructure.configuration;

import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.application.usecase.GetQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetQuotationsUseCase;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.CustomerPersistenceMapper;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.QuotationPersistenceMapper;
import com.magyen.platform.commercial.presentation.customer.mapper.CustomerPresentationMapper;
import com.magyen.platform.commercial.presentation.order.mapper.OrderPresentationMapper;
import com.magyen.platform.commercial.presentation.quotation.mapper.QuotationPresentationMapper;
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
    public CreateQuotationUseCase createQuotationUseCase(QuotationRepository quotationRepository) {
        return new CreateQuotationUseCase(quotationRepository);
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
    public GetQuotationUseCase getQuotationUseCase(QuotationRepository quotationRepository) {
        return new GetQuotationUseCase(quotationRepository);
    }

    @Bean
    public CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase(
            QuotationRepository quotationRepository,
            OrderRepository orderRepository
    ) {
        return new CreateOrderFromQuotationUseCase(quotationRepository, orderRepository);
    }
}
