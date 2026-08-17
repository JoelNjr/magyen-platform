package com.magyen.platform.production.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.dto.CompleteProductionOrderCommand;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class HistoricalBusinessDateUseCaseTest {

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
    private GetOrderUseCase getOrderUseCase;

    @Autowired
    private CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private CompleteProductionOrderUseCase completeProductionOrderUseCase;

    @Autowired
    private GetProductionOrderUseCase getProductionOrderUseCase;

    @Test
    void preservesHistoricalConfirmationAndProductionDatesAndRejectsBrokenChronology() {
        HistoricalCase historicalCase = createHistoricalCommercialCase();

        assertEquals(LocalDate.of(2026, 7, 29), historicalCase.order().confirmationDate());
        assertEquals(LocalDate.of(2026, 8, 6), historicalCase.order().deliveryCommitment().promisedDeliveryDate());

        CreateProductionOrderResult production = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        historicalCase.order().orderId(),
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        null
                )
        );

        assertThrows(ProductionDomainException.class, () -> planProductionOrderUseCase.execute(
                new PlanProductionOrderCommand(
                        production.productionOrderId(),
                        LocalDate.of(2026, 7, 28),
                        LocalDate.of(2026, 8, 5),
                        ProductionPriority.NORMAL
                )
        ));

        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                production.productionOrderId(),
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 5),
                ProductionPriority.NORMAL
        ));

        assertThrows(ProductionDomainException.class, () -> startProductionOrderUseCase.execute(
                new StartProductionOrderCommand(production.productionOrderId(), LocalDate.of(2026, 7, 28))
        ));

        startProductionOrderUseCase.execute(
                new StartProductionOrderCommand(production.productionOrderId(), LocalDate.of(2026, 8, 3))
        );

        assertThrows(ProductionDomainException.class, () -> completeProductionOrderUseCase.execute(
                new CompleteProductionOrderCommand(production.productionOrderId(), LocalDate.of(2026, 8, 7))
        ));

        completeProductionOrderUseCase.execute(
                new CompleteProductionOrderCommand(production.productionOrderId(), LocalDate.of(2026, 8, 5))
        );

        GetProductionOrderResult productionOrder = getProductionOrderUseCase.execute(
                new GetProductionOrderCommand(production.productionOrderId())
        );
        assertEquals(ProductionStatus.COMPLETED, productionOrder.status());
        assertEquals(LocalDate.of(2026, 8, 3), productionOrder.actualStartDate());
        assertEquals(LocalDate.of(2026, 8, 5), productionOrder.actualCompletionDate());
    }

    private HistoricalCase createHistoricalCommercialCase() {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-Hist-" + UUID.randomUUID().toString().substring(0, 8)
        );
        UUID customerId = createCustomerUseCase.execute(
                new CreateCustomerCommand("Customer-Hist-" + UUID.randomUUID().toString().substring(0, 8))
        ).customerId();

        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customerId,
                LocalDate.of(2026, 8, 6),
                sellerId,
                "Histórica",
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

        assertThrows(IllegalArgumentException.class, () -> createOrderFromQuotationUseCase.execute(
                new CreateOrderFromQuotationCommand(
                        quotation.quotationId(),
                        "SHOULD-FAIL",
                        null,
                        LocalDate.of(2026, 7, 26),
                        LocalDate.of(2026, 8, 6),
                        null
                )
        ));

        CreateOrderFromQuotationResult order = createOrderFromQuotationUseCase.execute(
                new CreateOrderFromQuotationCommand(
                        quotation.quotationId(),
                        "ORD-HIST-" + UUID.randomUUID().toString().substring(0, 8),
                        "Pedido histórico de prueba",
                        LocalDate.of(2026, 7, 29),
                        LocalDate.of(2026, 8, 6),
                        null
                )
        );
        GetOrderResult orderDetail = getOrderUseCase.execute(new GetOrderCommand(order.orderId()));
        return new HistoricalCase(quotation.quotationId(), orderDetail);
    }

    private record HistoricalCase(UUID quotationId, GetOrderResult order) {
    }
}
