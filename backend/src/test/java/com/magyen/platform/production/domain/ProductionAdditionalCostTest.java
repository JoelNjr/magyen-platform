package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionAdditionalCostTest {

    @Test
    void otherRequiresDescriptionAndPositiveAmount() {
        ProductionAdditionalCost cost = ProductionAdditionalCost.create(
                UUID.randomUUID(),
                ProductionDirectCostCategory.OTHER,
                "Envío de uniformes a Cartagena",
                Money.of(new BigDecimal("80000.00")),
                LocalDate.of(2026, 8, 20)
        );

        assertEquals(ProductionDirectCostCategory.OTHER, cost.getCategory());
        assertEquals("Envío de uniformes a Cartagena", cost.getDescription());
        assertEquals(new BigDecimal("80000.00"), cost.getAmount().getAmount());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(
                ProductionDomainException.class,
                () -> ProductionAdditionalCost.create(
                        UUID.randomUUID(),
                        ProductionDirectCostCategory.OTHER,
                        "  ",
                        Money.of(new BigDecimal("80000.00")),
                        LocalDate.of(2026, 8, 20)
                )
        );
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(
                ProductionDomainException.class,
                () -> ProductionAdditionalCost.create(
                        UUID.randomUUID(),
                        ProductionDirectCostCategory.OTHER,
                        "Medias adicionales",
                        Money.zero(),
                        LocalDate.of(2026, 8, 20)
                )
        );
    }
}
