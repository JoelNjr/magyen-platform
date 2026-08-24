package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.QuotationStatus;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderFromQuotationUseCaseTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private OrderRepository orderRepository;

    private CreateOrderFromQuotationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateOrderFromQuotationUseCase(quotationRepository, orderRepository);
    }

    @Test
    void assignsOrderNumberFromReservedQuotationNumber() {
        Quotation quotation = approvedQuotation(QuotationNumber.of(14L));
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(orderRepository.findByQuotationId(quotation.getId())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderFromQuotationResult result = useCase.execute(new CreateOrderFromQuotationCommand(
                quotation.getId(),
                "Pedido de uniformes",
                LocalDate.of(2026, 8, 23),
                quotation.getDeliveryDate(),
                quotation.getObservations()
        ));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        assertEquals("14", orderCaptor.getValue().getOrderNumber().getValue());
        assertEquals("14", result.orderNumber());
    }

    @Test
    void rejectsOrderWhenQuotationHasNoReservedNumber() {
        Quotation quotation = historicalApprovedQuotationWithoutNumber();
        when(quotationRepository.findById(quotation.getId())).thenReturn(Optional.of(quotation));
        when(orderRepository.findByQuotationId(quotation.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(new CreateOrderFromQuotationCommand(
                        quotation.getId(),
                        null,
                        LocalDate.of(2026, 8, 23),
                        quotation.getDeliveryDate(),
                        null
                ))
        );

        assertEquals(
                "Quotation has no commercial number reserved; an order cannot be created",
                exception.getMessage()
        );
    }

    private static Quotation approvedQuotation(QuotationNumber quotationNumber) {
        Quotation quotation = Quotation.create(
                quotationNumber,
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 25),
                UUID.randomUUID(),
                "Observaciones"
        );
        quotation.addItem(
                "Uniformes de futbol",
                10,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("96000"))
        );
        quotation.approve();
        return quotation;
    }

    private static Quotation historicalApprovedQuotationWithoutNumber() {
        Quotation draft = Quotation.create(
                QuotationNumber.of(1L),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 25),
                UUID.randomUUID(),
                null
        );
        draft.addItem(
                "Camiseta",
                5,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("40000"))
        );

        return Quotation.reconstitute(
                draft.getId(),
                null,
                draft.getCustomerId(),
                draft.getCreationDate(),
                draft.getDeliveryDate(),
                QuotationStatus.APPROVED,
                draft.getSellerId(),
                draft.getObservations(),
                List.copyOf(draft.getItems())
        );
    }
}
