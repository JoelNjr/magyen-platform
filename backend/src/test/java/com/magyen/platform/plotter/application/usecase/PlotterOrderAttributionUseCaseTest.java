package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PlotterOrderAttributionUseCaseTest {

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

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
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private GetPlotterJobUseCase getPlotterJobUseCase;

    @Test
    void attributesHistoricalPlotterJobToCommercialOrderWithoutCreatingFinanceIncome() {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-Plot-" + UUID.randomUUID().toString().substring(0, 8)
        );
        String customerName = "Customer-Plot-" + UUID.randomUUID().toString().substring(0, 8);
        UUID customerId = createCustomerUseCase.execute(
                new CreateCustomerCommand(customerName)
        ).customerId();

        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customerId,
                LocalDate.of(2026, 8, 6),
                sellerId,
                null,
                LocalDate.of(2026, 7, 27)
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Camiseta de voleibol",
                10,
                "Sudáfrica",
                "Blanco",
                new BigDecimal("40000"),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        var order = createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                "ORD-PLOT-" + UUID.randomUUID().toString().substring(0, 8),
                null,
                LocalDate.of(2026, 7, 29),
                LocalDate.of(2026, 8, 6),
                null
        ));

        CreateInventoryItemResult roll = createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "PLTA-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel plotter atribución",
                "PAPER",
                "METER",
                new BigDecimal("20.0000"),
                new BigDecimal("1.0000"),
                null,
                new BigDecimal("4500.00"),
                "PAPER",
                true
        ));

        assertThrows(PlotterDomainException.class, () -> createPlotterJobUseCase.execute(
                new CreatePlotterJobCommand(
                        customerId,
                        UUID.randomUUID(),
                        LocalDate.of(2026, 8, 3),
                        roll.inventoryItemId(),
                        new BigDecimal("6.0000"),
                        new BigDecimal("8000"),
                        null
                )
        ));

        assertThrows(PlotterDomainException.class, () -> createPlotterJobUseCase.execute(
                new CreatePlotterJobCommand(
                        customerId,
                        order.orderId(),
                        LocalDate.of(2026, 7, 28),
                        roll.inventoryItemId(),
                        new BigDecimal("6.0000"),
                        new BigDecimal("8000"),
                        null
                )
        ));

        CreatePlotterJobResult created = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                customerId,
                order.orderId(),
                LocalDate.of(2026, 8, 3),
                roll.inventoryItemId(),
                new BigDecimal("6.0000"),
                new BigDecimal("8000"),
                "Atribución histórica"
        ));

        assertEquals(order.orderId(), created.orderId());
        assertEquals(PlotterJobType.INTERNAL_MAGYEN, created.jobType());
        assertEquals(order.orderNumber(), created.orderNumber());
        assertEquals(customerName, created.customerName());
        assertEquals(LocalDate.of(2026, 8, 3), created.creationDate());
        assertEquals(new BigDecimal("48000.00"), created.totalAmount());

        GetPlotterJobResult detail = getPlotterJobUseCase.execute(new GetPlotterJobQuery(created.plotterJobId()));
        assertEquals(order.orderId(), detail.orderId());
        assertEquals(order.orderNumber(), detail.orderNumber());
        assertEquals(customerName, detail.customerName());
        assertEquals(PlotterJobType.INTERNAL_MAGYEN, detail.jobType());
        assertEquals(new BigDecimal("0.00"), detail.outstandingAmount());
        assertEquals(new BigDecimal("0.00"), detail.paidAmount());
        assertEquals(new BigDecimal("48000.00"), detail.totalAmount());
    }

    @Test
    void allowsPlotterJobWithoutOrderAttribution() {
        CreateInventoryItemResult roll = createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "PLTN-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel plotter sin orden",
                "PAPER",
                "METER",
                new BigDecimal("20.0000"),
                new BigDecimal("1.0000"),
                null,
                new BigDecimal("4500.00"),
                "PAPER",
                true
        ));

        CreatePlotterJobResult created = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                null,
                LocalDate.of(2026, 8, 3),
                roll.inventoryItemId(),
                new BigDecimal("6.0000"),
                new BigDecimal("8000"),
                null
        ));

        assertNull(created.orderId());
        assertEquals(PlotterJobType.EXTERNAL, created.jobType());
        assertEquals(LocalDate.of(2026, 8, 3), created.creationDate());
    }
}
