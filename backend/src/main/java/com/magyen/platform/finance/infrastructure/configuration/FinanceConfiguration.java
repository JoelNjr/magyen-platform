package com.magyen.platform.finance.infrastructure.configuration;

import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.finance.application.usecase.GetPaymentUseCase;
import com.magyen.platform.finance.application.usecase.GetPaymentsByOrderUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.finance.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import com.magyen.platform.finance.presentation.payment.mapper.PaymentPresentationMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los beans del módulo financiero que no se registran por estereotipos Spring.
 */
@Configuration
public class FinanceConfiguration {

    @Bean
    public PaymentPresentationMapper paymentPresentationMapper() {
        return new PaymentPresentationMapper();
    }

    @Bean
    public PaymentPersistenceMapper paymentPersistenceMapper() {
        return new PaymentPersistenceMapper();
    }

    @Bean
    public RegisterPaymentUseCase registerPaymentUseCase(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository
    ) {
        return new RegisterPaymentUseCase(orderRepository, paymentRepository);
    }

    @Bean
    public GetPaymentUseCase getPaymentUseCase(PaymentRepository paymentRepository) {
        return new GetPaymentUseCase(paymentRepository);
    }

    @Bean
    public GetPaymentsByOrderUseCase getPaymentsByOrderUseCase(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository
    ) {
        return new GetPaymentsByOrderUseCase(orderRepository, paymentRepository);
    }
}
