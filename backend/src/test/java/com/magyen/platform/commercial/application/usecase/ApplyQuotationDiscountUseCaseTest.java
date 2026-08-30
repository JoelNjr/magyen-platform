package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.ApplyQuotationDiscountCommand;
import com.magyen.platform.commercial.application.dto.ApplyQuotationDiscountResult;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplyQuotationDiscountUseCaseTest {

    private InMemoryQuotationRepository quotationRepository;
    private InMemoryOrderRepository orderRepository;
    private ApplyQuotationDiscountUseCase applyQuotationDiscountUseCase;
    private CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase;

    @BeforeEach
    void setUp() {
        quotationRepository = new InMemoryQuotationRepository();
        orderRepository = new InMemoryOrderRepository();
        applyQuotationDiscountUseCase = new ApplyQuotationDiscountUseCase(quotationRepository);
        createOrderFromQuotationUseCase = new CreateOrderFromQuotationUseCase(
                quotationRepository,
                orderRepository
        );
    }

    @Test
    void appliesDiscountPersistsItAndCopiesItToTheOrder() {
        Quotation quotation = draftWithItem(77);
        quotation = quotationRepository.save(quotation);

        ApplyQuotationDiscountResult applied = applyQuotationDiscountUseCase.execute(
                new ApplyQuotationDiscountCommand(quotation.getId(), new BigDecimal("100000.00"))
        );
        assertEquals(new BigDecimal("1000000.00"), applied.subtotalAmount());
        assertEquals(new BigDecimal("100000.00"), applied.discountAmount());
        assertEquals(new BigDecimal("900000.00"), applied.totalAmount());

        Quotation reloaded = quotationRepository.findById(quotation.getId()).orElseThrow();
        assertEquals(new BigDecimal("900000.00"), reloaded.getTotal().getAmount());
        assertEquals(new BigDecimal("100000.00"), reloaded.getItems().getFirst().getUnitPrice().getAmount());

        reloaded.approve();
        quotationRepository.save(reloaded);

        CreateOrderFromQuotationResult created = createOrderFromQuotationUseCase.execute(
                new CreateOrderFromQuotationCommand(
                        reloaded.getId(),
                        "Pedido con descuento",
                        LocalDate.of(2026, 8, 16),
                        LocalDate.of(2026, 8, 20),
                        null
                )
        );
        Order order = orderRepository.findById(created.orderId()).orElseThrow();
        assertEquals(new BigDecimal("1000000.00"), order.getSubtotal().getAmount());
        assertEquals(new BigDecimal("100000.00"), order.getDiscount().getAmount());
        assertEquals(new BigDecimal("900000.00"), order.getTotal().getAmount());
        assertEquals(new BigDecimal("100000.00"), order.getItems().getFirst().getUnitPrice().getAmount());
    }

    @Test
    void rejectsDiscountGreaterThanSubtotalInUseCase() {
        Quotation quotation = quotationRepository.save(emptyDraft(78));
        assertThrows(
                QuotationDomainException.class,
                () -> applyQuotationDiscountUseCase.execute(
                        new ApplyQuotationDiscountCommand(quotation.getId(), new BigDecimal("10.00"))
                )
        );
    }

    private static Quotation draftWithItem(long number) {
        Quotation quotation = emptyDraft(number);
        quotation.addItem(
                "Camiseta",
                10,
                "Algodón",
                "Blanco",
                Money.of(new BigDecimal("100000.00"))
        );
        return quotation;
    }

    private static Quotation emptyDraft(long number) {
        return Quotation.create(
                QuotationNumber.of(number),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 15),
                UUID.randomUUID(),
                null
        );
    }

    private static final class InMemoryQuotationRepository implements QuotationRepository {
        private final Map<UUID, Quotation> quotations = new LinkedHashMap<>();

        @Override
        public Quotation save(Quotation quotation) {
            quotations.put(quotation.getId(), quotation);
            return quotation;
        }

        @Override
        public Optional<Quotation> findById(UUID id) {
            return Optional.ofNullable(quotations.get(id));
        }

        @Override
        public List<Quotation> findAll() {
            return new ArrayList<>(quotations.values());
        }
    }

    private static final class InMemoryOrderRepository implements OrderRepository {
        private final Map<UUID, Order> orders = new LinkedHashMap<>();

        @Override
        public Order save(Order order) {
            orders.put(order.getId(), order);
            return order;
        }

        @Override
        public Optional<Order> findById(UUID id) {
            return Optional.ofNullable(orders.get(id));
        }

        @Override
        public Optional<Order> findByQuotationId(UUID quotationId) {
            return orders.values().stream()
                    .filter(order -> order.getQuotationId().equals(quotationId))
                    .findFirst();
        }

        @Override
        public List<Order> findAll() {
            return new ArrayList<>(orders.values());
        }
    }
}
