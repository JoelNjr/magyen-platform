package com.magyen.platform.production.infrastructure.persistence;

import com.magyen.platform.production.domain.ProductSpecification;
import com.magyen.platform.production.domain.ProductionItem;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import com.magyen.platform.production.domain.SizeBreakdown;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que el snapshot productivo sobreviva persistencia y recarga.
 */
@SpringBootTest
@Transactional
class ProductionItemSnapshotPersistenceTest {

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsSnapshotProductSpecificationAndSizeBreakdownsAcrossReload() {
        ProductSpecification specification = ProductSpecification.of(
                "Camiseta",
                "Redondo",
                "Corta",
                "Dry-fit",
                true,
                false,
                true,
                "Full print",
                true,
                true,
                false,
                "Roster completo",
                "Prioridad alta"
        );

        SizeBreakdown sizeS = SizeBreakdown.create("S", 3);
        SizeBreakdown sizeM = SizeBreakdown.create("M", 7);
        SizeBreakdown sizeL = SizeBreakdown.create("L", 6);
        SizeBreakdown sizeXl = SizeBreakdown.create("XL", 4);

        ProductionItem item = ProductionItem.create(
                "Camiseta Deportiva",
                20,
                specification,
                List.of(sizeS, sizeM, sizeL, sizeXl)
        );

        ProductionOrder productionOrder = ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                "Snapshot persistence verification",
                List.of(item)
        );

        ProductionOrder saved = productionOrderRepository.save(productionOrder);
        UUID productionOrderId = saved.getId();
        UUID itemId = saved.getItems().getFirst().getId();
        UUID sizeSId = sizeS.getId();
        UUID sizeMId = sizeM.getId();
        UUID sizeLId = sizeL.getId();
        UUID sizeXlId = sizeXl.getId();

        entityManager.flush();
        entityManager.clear();

        ProductionOrder reloaded = productionOrderRepository.findById(productionOrderId).orElseThrow();
        assertEquals(ProductionStatus.CREATED, reloaded.getStatus());
        assertEquals(1, reloaded.getItems().size());

        ProductionItem reloadedItem = reloaded.getItems().stream()
                .filter(productionItem -> productionItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow();

        assertEquals("Camiseta Deportiva", reloadedItem.getProductName());
        assertEquals(20, reloadedItem.getQuantity());

        ProductSpecification reloadedSpecification = reloadedItem.getProductSpecification();
        assertEquals("Camiseta", reloadedSpecification.getGarmentType());
        assertEquals("Redondo", reloadedSpecification.getCollarType());
        assertEquals("Corta", reloadedSpecification.getSleeveType());
        assertEquals("Dry-fit", reloadedSpecification.getGarmentVariant());
        assertTrue(reloadedSpecification.isSublimationRequired());
        assertFalse(reloadedSpecification.isEmbroideryRequired());
        assertTrue(reloadedSpecification.isDtfRequired());
        assertEquals("Full print", reloadedSpecification.getDecorationNotes());
        assertTrue(reloadedSpecification.isIncludesNames());
        assertTrue(reloadedSpecification.isIncludesNumbers());
        assertFalse(reloadedSpecification.isIncludesLogos());
        assertEquals("Roster completo", reloadedSpecification.getPersonalizationNotes());
        assertEquals("Prioridad alta", reloadedSpecification.getItemObservations());

        Map<UUID, SizeBreakdown> sizesById = reloadedItem.getSizeBreakdowns().stream()
                .collect(Collectors.toMap(SizeBreakdown::getId, sizeBreakdown -> sizeBreakdown));

        assertEquals(4, sizesById.size());
        assertEquals("S", sizesById.get(sizeSId).getSize());
        assertEquals(3, sizesById.get(sizeSId).getQuantity());
        assertEquals("M", sizesById.get(sizeMId).getSize());
        assertEquals(7, sizesById.get(sizeMId).getQuantity());
        assertEquals("L", sizesById.get(sizeLId).getSize());
        assertEquals(6, sizesById.get(sizeLId).getQuantity());
        assertEquals("XL", sizesById.get(sizeXlId).getSize());
        assertEquals(4, sizesById.get(sizeXlId).getQuantity());
    }

    @Test
    void persistsProductionItemWithEmptySizes() {
        ProductionItem item = ProductionItem.create(
                "Pantalón",
                12,
                ProductSpecification.empty(),
                List.of()
        );

        ProductionOrder productionOrder = ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.LOW,
                null,
                null,
                "Empty sizes snapshot",
                List.of(item)
        );

        ProductionOrder saved = productionOrderRepository.save(productionOrder);
        UUID productionOrderId = saved.getId();

        entityManager.flush();
        entityManager.clear();

        ProductionOrder reloaded = productionOrderRepository.findById(productionOrderId).orElseThrow();
        assertEquals(1, reloaded.getItems().size());
        assertEquals("Pantalón", reloaded.getItems().getFirst().getProductName());
        assertEquals(12, reloaded.getItems().getFirst().getQuantity());
        assertTrue(reloaded.getItems().getFirst().getProductSpecification().isEmpty());
        assertTrue(reloaded.getItems().getFirst().getSizeBreakdowns().isEmpty());
    }
}
