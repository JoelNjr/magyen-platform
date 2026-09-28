package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsResult;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import com.magyen.platform.shared.testsupport.PlotterCustomerFixture;
import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PlotterCustomerFilterUseCaseTest {

    private static final LocalDate FROM = LocalDate.of(2099, 11, 1);
    private static final LocalDate TO = LocalDate.of(2099, 11, 30);

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
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private GetPlotterJobsUseCase getPlotterJobsUseCase;

    @Test
    void filtersByCustomerAndDateWithoutChangingUnfilteredBehavior() {
        UUID plotterCustomerId = PlotterCustomerFixture.create(createCustomerUseCase);
        UUID otherPlotterCustomerId = PlotterCustomerFixture.create(createCustomerUseCase);
        UUID magyenCustomerId = createCustomerUseCase.execute(
                new CreateCustomerCommand("Magyen filtro " + UUID.randomUUID())
        ).customerId();
        UUID orderId = createOrder(magyenCustomerId);
        CreateInventoryItemResult roll = createPaperRoll();

        UUID inRange = createExternal(plotterCustomerId, roll, LocalDate.of(2099, 11, 5));
        UUID outOfRange = createExternal(plotterCustomerId, roll, LocalDate.of(2099, 12, 5));
        UUID otherCustomer = createExternal(otherPlotterCustomerId, roll, LocalDate.of(2099, 11, 6));
        UUID internalJob = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                orderId,
                LocalDate.of(2099, 11, 7),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("8000"),
                "interno del cliente Magyen",
                PlotterJobType.INTERNAL_MAGYEN,
                null
        )).plotterJobId();

        GetPlotterJobsResult filtered = getPlotterJobsUseCase.execute(new GetPlotterJobsQuery(FROM, TO, plotterCustomerId));
        assertEquals(1, filtered.jobs().size());
        assertEquals(inRange, filtered.jobs().getFirst().plotterJobId());

        GetPlotterJobsResult magyenJobs = getPlotterJobsUseCase.execute(new GetPlotterJobsQuery(FROM, TO, magyenCustomerId));
        assertEquals(1, magyenJobs.jobs().size());
        assertEquals(internalJob, magyenJobs.jobs().getFirst().plotterJobId());
        assertEquals(PlotterJobType.INTERNAL_MAGYEN, magyenJobs.jobs().getFirst().jobType());

        GetPlotterJobsResult byDate = getPlotterJobsUseCase.execute(new GetPlotterJobsQuery(FROM, TO));
        GetPlotterJobsResult byDateWithoutCustomer = getPlotterJobsUseCase.execute(new GetPlotterJobsQuery(FROM, TO, null));
        assertEquals(
                byDate.jobs().stream().map(job -> job.plotterJobId()).sorted().toList(),
                byDateWithoutCustomer.jobs().stream().map(job -> job.plotterJobId()).sorted().toList()
        );
        assertTrue(byDate.jobs().stream().anyMatch(job -> job.plotterJobId().equals(inRange)));
        assertTrue(byDate.jobs().stream().anyMatch(job -> job.plotterJobId().equals(otherCustomer)));
        assertTrue(byDate.jobs().stream().anyMatch(job -> job.plotterJobId().equals(internalJob)));
        assertTrue(byDate.jobs().stream().noneMatch(job -> job.plotterJobId().equals(outOfRange)));

        GetPlotterJobsResult customerOnly = getPlotterJobsUseCase.execute(
                new GetPlotterJobsQuery(null, null, plotterCustomerId)
        );
        assertTrue(customerOnly.jobs().stream().anyMatch(job -> job.plotterJobId().equals(inRange)));
        assertTrue(customerOnly.jobs().stream().anyMatch(job -> job.plotterJobId().equals(outOfRange)));
        assertTrue(customerOnly.jobs().stream().noneMatch(job -> job.plotterJobId().equals(otherCustomer)));
    }

    @Test
    void filtersInternalMagyenJobsWithoutUsingACustomerId() {
        UUID plotterCustomerId = PlotterCustomerFixture.create(createCustomerUseCase);
        UUID magyenCustomerId = createCustomerUseCase.execute(
                new CreateCustomerCommand("Magyen interno " + UUID.randomUUID())
        ).customerId();
        UUID orderId = createOrder(magyenCustomerId);
        CreateInventoryItemResult roll = createPaperRoll();

        UUID externalJob = createExternal(plotterCustomerId, roll, LocalDate.of(2099, 11, 8));
        UUID internalJob = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                orderId,
                LocalDate.of(2099, 11, 9),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("8000"),
                "interno agrupado",
                PlotterJobType.INTERNAL_MAGYEN,
                null
        )).plotterJobId();

        GetPlotterJobsResult internalOnly = getPlotterJobsUseCase.execute(
                new GetPlotterJobsQuery(FROM, TO, null, PlotterJobType.INTERNAL_MAGYEN)
        );
        assertTrue(internalOnly.jobs().stream().anyMatch(job -> job.plotterJobId().equals(internalJob)));
        assertTrue(internalOnly.jobs().stream().allMatch(job -> job.jobType() == PlotterJobType.INTERNAL_MAGYEN));
        assertTrue(internalOnly.jobs().stream().noneMatch(job -> job.plotterJobId().equals(externalJob)));

        GetPlotterJobsResult plotterCustomer = getPlotterJobsUseCase.execute(
                new GetPlotterJobsQuery(FROM, TO, plotterCustomerId, null)
        );
        assertEquals(1, plotterCustomer.jobs().size());
        assertEquals(externalJob, plotterCustomer.jobs().getFirst().plotterJobId());
        assertEquals(PlotterJobType.EXTERNAL, plotterCustomer.jobs().getFirst().jobType());
    }

    private UUID createExternal(UUID customerId, CreateInventoryItemResult roll, LocalDate date) {
        return createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                customerId,
                null,
                date,
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("1000"),
                null,
                PlotterJobType.EXTERNAL,
                null
        )).plotterJobId();
    }

    private UUID createOrder(UUID customerId) {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor filtro " + UUID.randomUUID()
        );
        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customerId,
                LocalDate.of(2099, 11, 20),
                sellerId,
                null,
                LocalDate.of(2099, 11, 1)
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Camiseta filtro",
                1,
                "Sudáfrica",
                "Blanco",
                new BigDecimal("10000"),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        return createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                null,
                LocalDate.of(2099, 11, 2),
                LocalDate.of(2099, 11, 20),
                null
        )).orderId();
    }

    private CreateInventoryItemResult createPaperRoll() {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "FLT-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel filtro",
                "PAPER",
                "METER",
                new BigDecimal("50.0000"),
                new BigDecimal("1.0000"),
                null,
                new BigDecimal("1000.00"),
                "PAPER",
                true
        ));
    }
}
