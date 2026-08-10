package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemsQuery;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PaperRollInventoryUseCaseTest {

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private GetInventoryItemsUseCase getInventoryItemsUseCase;

    @Test
    void createsPaperRollWithAutomaticUniqueRpNumber() {
        CreateInventoryItemResult first = createPaperRoll("ROLL-A-" + uuidSuffix());
        CreateInventoryItemResult second = createPaperRoll("ROLL-B-" + uuidSuffix());

        assertTrue(first.plotterPaperRoll());
        assertTrue(second.plotterPaperRoll());
        assertEquals(InventoryMaterialType.PAPER, first.materialType());
        assertEquals("METER", first.unitOfMeasure());
        assertNotNull(first.paperRollNumber());
        assertTrue(first.paperRollNumber().matches("RP-\\d{3,}"));
        assertNotNull(second.paperRollNumber());
        assertFalse(first.paperRollNumber().equals(second.paperRollNumber()));
    }

    @Test
    void nonPaperItemHasNoRpNumber() {
        CreateInventoryItemResult fabric = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        "FAB-" + uuidSuffix(),
                        "Tela",
                        "FABRIC",
                        "METER",
                        new BigDecimal("10.0000"),
                        null,
                        null,
                        null,
                        "FABRIC",
                        false
                )
        );

        assertEquals(InventoryMaterialType.FABRIC, fabric.materialType());
        assertNull(fabric.paperRollNumber());
        assertFalse(fabric.plotterPaperRoll());
    }

    @Test
    void rejectsPlotterPaperRollWithoutPaperType() {
        assertThrows(InventoryDomainException.class, () ->
                createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                        "BAD-" + uuidSuffix(),
                        "Bad",
                        "FABRIC",
                        "METER",
                        new BigDecimal("10.0000"),
                        null,
                        null,
                        null,
                        "FABRIC",
                        true
                ))
        );
    }

    @Test
    void filtersPlotterPaperRolls() {
        CreateInventoryItemResult roll = createPaperRoll("ROLL-F-" + uuidSuffix());
        createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "FAB-F-" + uuidSuffix(),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("5.0000"),
                null,
                null,
                null,
                "FABRIC",
                false
        ));

        var rolls = getInventoryItemsUseCase.execute(new GetInventoryItemsQuery(null, true));
        assertTrue(rolls.items().stream()
                .anyMatch(item -> item.inventoryItemId().equals(roll.inventoryItemId())));
        assertTrue(rolls.items().stream().allMatch(item -> item.plotterPaperRoll()));
    }

    @Test
    void generatedRpNumbersRemainUniqueInBatch() {
        Set<String> numbers = new HashSet<>();
        for (int index = 0; index < 5; index++) {
            CreateInventoryItemResult roll = createPaperRoll("BATCH-" + uuidSuffix());
            assertTrue(numbers.add(roll.paperRollNumber()));
        }
    }

    private CreateInventoryItemResult createPaperRoll(String code) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                code,
                "Papel sublimación",
                "PAPER",
                "UNIT",
                new BigDecimal("100.0000"),
                new BigDecimal("20.0000"),
                "Rollo plotter",
                new BigDecimal("4500.00"),
                "PAPER",
                true
        ));
    }

    private static String uuidSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
