package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationResult;
import com.magyen.platform.commercial.application.dto.GetOrdersQuery;
import com.magyen.platform.commercial.application.dto.GetQuotationsQuery;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.application.dto.QuotationResult;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class DocumentListingOrderUseCaseTest {

    private static final LocalDate QUOTATION_MONTH_START = LocalDate.of(2096, 5, 1);
    private static final LocalDate QUOTATION_MONTH_END = LocalDate.of(2096, 5, 31);
    private static final LocalDate ORDER_MONTH_START = LocalDate.of(2096, 8, 1);
    private static final LocalDate ORDER_MONTH_END = LocalDate.of(2096, 8, 31);

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;

    @Autowired
    private CreateQuotationUseCase createQuotationUseCase;

    @Autowired
    private AddQuotationItemUseCase addQuotationItemUseCase;

    @Autowired
    private ApproveQuotationUseCase approveQuotationUseCase;

    @Autowired
    private CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase;

    @Autowired
    private GetQuotationsUseCase getQuotationsUseCase;

    @Autowired
    private GetOrdersUseCase getOrdersUseCase;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Test
    void quotationsStayOrderedByConsecutiveAfterUpdateAndIgnoreCreationDate() {
        UUID sellerId = seller();
        CreateQuotationResult first = createQuotation(sellerId, LocalDate.of(2096, 5, 15), "First");
        CreateQuotationResult second = createQuotation(sellerId, LocalDate.of(2096, 5, 1), "Second");
        CreateQuotationResult third = createQuotation(sellerId, LocalDate.of(2096, 5, 20), "Third");

        addItem(third.quotationId(), "Producto editado");

        List<Long> numbers = getQuotationsUseCase.execute(
                new GetQuotationsQuery(QUOTATION_MONTH_START, QUOTATION_MONTH_END)
        ).quotations().stream().map(QuotationResult::quotationNumber).toList();

        assertEquals(
                List.of(first.quotationNumber(), second.quotationNumber(), third.quotationNumber()),
                numbers
        );
    }

    @Test
    void ordersStayOrderedByNumericCommercialNumberAndIgnoreConfirmationDate() {
        UUID sellerId = seller();
        CreateOrderFromQuotationResult first = createOrder(
                sellerId,
                LocalDate.of(2096, 8, 15),
                LocalDate.of(2096, 5, 10),
                "Order first"
        );
        CreateOrderFromQuotationResult second = createOrder(
                sellerId,
                LocalDate.of(2096, 8, 1),
                LocalDate.of(2096, 5, 11),
                "Order second"
        );
        CreateOrderFromQuotationResult third = createOrder(
                sellerId,
                LocalDate.of(2096, 8, 20),
                LocalDate.of(2096, 5, 12),
                "Order third"
        );

        List<String> numbers = getOrdersUseCase.execute(
                new GetOrdersQuery(ORDER_MONTH_START, ORDER_MONTH_END)
        ).orders().stream().map(OrderResult::orderNumber).toList();

        assertEquals(
                List.of(first.orderNumber(), second.orderNumber(), third.orderNumber()),
                numbers
        );
    }

    private UUID seller() {
        return FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-listing-order-" + UUID.randomUUID().toString().substring(0, 8)
        );
    }

    private CreateQuotationResult createQuotation(UUID sellerId, LocalDate creationDate, String name) {
        var customer = createCustomerUseCase.execute(new CreateCustomerCommand(name + " " + UUID.randomUUID()));
        return createQuotationUseCase.execute(new CreateQuotationCommand(
                customer.customerId(),
                creationDate.plusDays(10),
                sellerId,
                name,
                creationDate
        ));
    }

    private void addItem(UUID quotationId, String productName) {
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotationId,
                productName,
                1,
                "Sudáfrica",
                "Negro",
                new BigDecimal("25000"),
                null
        ));
    }

    private CreateOrderFromQuotationResult createOrder(
            UUID sellerId,
            LocalDate confirmationDate,
            LocalDate quotationCreationDate,
            String name
    ) {
        CreateQuotationResult quotation = createQuotation(sellerId, quotationCreationDate, name);
        addItem(quotation.quotationId(), name);
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        return createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                name,
                confirmationDate,
                confirmationDate.plusDays(7),
                null
        ));
    }
}
