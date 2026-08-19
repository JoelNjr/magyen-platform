package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionOrdersQuery;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProductionMonthPeriodListingUseCaseTest {

    @Autowired
    private GetProductionOrdersUseCase getProductionOrdersUseCase;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Test
    void filtersByCreationDateIncludingBoundaryYearChangeAndEmptyMonth() {
        UUID marchId = saveProduction(LocalDate.of(2099, 3, 1));
        UUID aprilId = saveProduction(LocalDate.of(2099, 4, 1));
        UUID decemberId = saveProduction(LocalDate.of(2098, 12, 31));

        var march = getProductionOrdersUseCase.execute(
                new GetProductionOrdersQuery(LocalDate.of(2099, 3, 1), LocalDate.of(2099, 3, 31))
        );
        assertTrue(march.productionOrders().stream()
                .anyMatch(item -> marchId.equals(item.productionOrderId())));
        assertTrue(march.productionOrders().stream()
                .noneMatch(item -> aprilId.equals(item.productionOrderId())));

        var april = getProductionOrdersUseCase.execute(
                new GetProductionOrdersQuery(LocalDate.of(2099, 4, 1), LocalDate.of(2099, 4, 30))
        );
        assertTrue(april.productionOrders().stream()
                .anyMatch(item -> aprilId.equals(item.productionOrderId())));
        assertTrue(april.productionOrders().stream()
                .noneMatch(item -> marchId.equals(item.productionOrderId())));

        var yearChange = getProductionOrdersUseCase.execute(
                new GetProductionOrdersQuery(LocalDate.of(2098, 12, 1), LocalDate.of(2098, 12, 31))
        );
        assertTrue(yearChange.productionOrders().stream()
                .anyMatch(item -> decemberId.equals(item.productionOrderId())));

        var empty = getProductionOrdersUseCase.execute(
                new GetProductionOrdersQuery(LocalDate.of(2097, 2, 1), LocalDate.of(2097, 2, 28))
        );
        assertEquals(0, empty.productionOrders().size());

        var all = getProductionOrdersUseCase.execute();
        assertTrue(all.productionOrders().stream()
                .anyMatch(item -> marchId.equals(item.productionOrderId())));

        assertThrows(ProductionDomainException.class, () ->
                getProductionOrdersUseCase.execute(new GetProductionOrdersQuery(LocalDate.of(2099, 3, 1), null))
        );
    }

    private UUID saveProduction(LocalDate creationDate) {
        ProductionOrder productionOrder = ProductionOrder.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                creationDate,
                ProductionStatus.CREATED,
                ProductionPriority.NORMAL,
                null,
                null,
                "month listing",
                List.of(),
                List.of()
        );
        return productionOrderRepository.save(productionOrder).getId();
    }
}
