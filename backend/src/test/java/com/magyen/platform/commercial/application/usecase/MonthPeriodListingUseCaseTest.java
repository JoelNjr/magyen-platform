package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetOrdersQuery;
import com.magyen.platform.commercial.application.dto.GetQuotationsQuery;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class MonthPeriodListingUseCaseTest {

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
    void quotationsFilterByCreationDateIncludingMonthBoundaryAndEmptyMonth() {
        UUID sellerId = seller();
        UUID marchId = createQuotation(sellerId, LocalDate.of(2099, 3, 1), "March Q").quotationId();
        UUID aprilId = createQuotation(sellerId, LocalDate.of(2099, 4, 1), "April Q").quotationId();
        UUID decemberId = createQuotation(sellerId, LocalDate.of(2098, 12, 31), "Dec Q").quotationId();

        var march = getQuotationsUseCase.execute(
                new GetQuotationsQuery(LocalDate.of(2099, 3, 1), LocalDate.of(2099, 3, 31))
        );
        assertTrue(march.quotations().stream().anyMatch(item -> marchId.equals(item.quotationId())));
        assertTrue(march.quotations().stream().noneMatch(item -> aprilId.equals(item.quotationId())));
        assertTrue(march.quotations().stream().noneMatch(item -> decemberId.equals(item.quotationId())));

        var april = getQuotationsUseCase.execute(
                new GetQuotationsQuery(LocalDate.of(2099, 4, 1), LocalDate.of(2099, 4, 30))
        );
        assertTrue(april.quotations().stream().anyMatch(item -> aprilId.equals(item.quotationId())));
        assertTrue(april.quotations().stream().noneMatch(item -> marchId.equals(item.quotationId())));

        var empty = getQuotationsUseCase.execute(
                new GetQuotationsQuery(LocalDate.of(2097, 1, 1), LocalDate.of(2097, 1, 31))
        );
        assertEquals(0, empty.quotations().size());

        var yearChange = getQuotationsUseCase.execute(
                new GetQuotationsQuery(LocalDate.of(2098, 12, 1), LocalDate.of(2098, 12, 31))
        );
        assertTrue(yearChange.quotations().stream().anyMatch(item -> decemberId.equals(item.quotationId())));
        assertTrue(yearChange.quotations().stream().noneMatch(item -> marchId.equals(item.quotationId())));

        var all = getQuotationsUseCase.execute();
        assertTrue(all.quotations().stream().anyMatch(item -> marchId.equals(item.quotationId())));
        assertTrue(all.quotations().stream().anyMatch(item -> aprilId.equals(item.quotationId())));
    }

    @Test
    void ordersFilterByConfirmationDateAndRejectPartialRange() {
        UUID sellerId = seller();
        var july = createOrder(sellerId, LocalDate.of(2099, 7, 31), "July order");
        var august = createOrder(sellerId, LocalDate.of(2099, 8, 1), "August order");

        var julyList = getOrdersUseCase.execute(
                new GetOrdersQuery(LocalDate.of(2099, 7, 1), LocalDate.of(2099, 7, 31))
        );
        assertTrue(julyList.orders().stream().anyMatch(item -> july.orderId().equals(item.orderId())));
        assertTrue(julyList.orders().stream().noneMatch(item -> august.orderId().equals(item.orderId())));

        var augustList = getOrdersUseCase.execute(
                new GetOrdersQuery(LocalDate.of(2099, 8, 1), LocalDate.of(2099, 8, 31))
        );
        assertTrue(augustList.orders().stream().anyMatch(item -> august.orderId().equals(item.orderId())));
        assertTrue(augustList.orders().stream().noneMatch(item -> july.orderId().equals(item.orderId())));

        assertThrows(QuotationDomainException.class, () ->
                getQuotationsUseCase.execute(new GetQuotationsQuery(LocalDate.of(2099, 3, 1), null))
        );
        assertThrows(OrderDomainException.class, () ->
                getOrdersUseCase.execute(new GetOrdersQuery(null, LocalDate.of(2099, 8, 31)))
        );
    }

    private UUID seller() {
        return FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-month-" + UUID.randomUUID().toString().substring(0, 8)
        );
    }

    private com.magyen.platform.commercial.application.dto.CreateQuotationResult createQuotation(
            UUID sellerId,
            LocalDate creationDate,
            String name
    ) {
        var customer = createCustomerUseCase.execute(new CreateCustomerCommand(name + " " + UUID.randomUUID()));
        return createQuotationUseCase.execute(new CreateQuotationCommand(
                customer.customerId(),
                creationDate.plusDays(10),
                sellerId,
                name,
                creationDate
        ));
    }

    private com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult createOrder(
            UUID sellerId,
            LocalDate confirmationDate,
            String name
    ) {
        var quotation = createQuotation(sellerId, confirmationDate.minusDays(2), name);
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                name,
                1,
                "Sudáfrica",
                "Negro",
                new BigDecimal("25000"),
                null
        ));
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
