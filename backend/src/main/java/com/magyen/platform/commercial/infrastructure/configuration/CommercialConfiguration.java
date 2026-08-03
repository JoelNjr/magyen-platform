package com.magyen.platform.commercial.infrastructure.configuration;

import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.QuotationPersistenceMapper;
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
    public QuotationPersistenceMapper quotationPersistenceMapper() {
        return new QuotationPersistenceMapper();
    }

    @Bean
    public CreateQuotationUseCase createQuotationUseCase(QuotationRepository quotationRepository) {
        return new CreateQuotationUseCase(quotationRepository);
    }
}
