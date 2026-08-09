package com.magyen.platform.commercial.infrastructure.persistence;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.SizeBreakdown;
import com.magyen.platform.shared.domain.Money;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica persistencia de ProductSpecification y SizeBreakdown en OrderItem.
 */
@SpringBootTest
@Transactional
class OrderItemSpecificationPersistenceTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsProductSpecificationAndSizeBreakdownsAcrossReload() {
        LocalDate today = LocalDate.now();
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

        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta Deportiva",
                20,
                "Hydrotech",
                "Azul",
                Money.of(new BigDecimal("45000")),
                specification,
                List.of(sizeS, sizeM, sizeL, sizeXl)
        );

        Order order = Order.create(
                OrderNumber.of("ORD-SPEC-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(10)),
                "Persistence Tester",
                "Specification persistence",
                List.of(item)
        );

        Order saved = orderRepository.save(order);
        UUID orderId = saved.getId();
        UUID itemId = saved.getItems().getFirst().getId();
        UUID sizeSId = sizeS.getId();
        UUID sizeMId = sizeM.getId();
        UUID sizeLId = sizeL.getId();
        UUID sizeXlId = sizeXl.getId();

        entityManager.flush();
        entityManager.clear();

        Order reloaded = orderRepository.findById(orderId).orElseThrow();
        OrderItem reloadedItem = reloaded.getItems().stream()
                .filter(orderItem -> orderItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow();

        ProductSpecification reloadedSpecification = reloadedItem.getProductSpecification();
        assertEquals("Camiseta", reloadedSpecification.getGarmentType());
        assertEquals("Redondo", reloadedSpecification.getCollarType());
        assertEquals("Corta", reloadedSpecification.getSleeveType());
        assertEquals("Dry-fit", reloadedSpecification.getGarmentVariant());
        assertTrue(reloadedSpecification.isSublimationRequired());
        assertTrue(reloadedSpecification.isDtfRequired());
        assertTrue(reloadedSpecification.isIncludesNames());
        assertTrue(reloadedSpecification.isIncludesNumbers());
        assertEquals("Full print", reloadedSpecification.getDecorationNotes());
        assertEquals("Roster completo", reloadedSpecification.getPersonalizationNotes());
        assertEquals("Prioridad alta", reloadedSpecification.getItemObservations());

        assertEquals(4, reloadedItem.getSizeBreakdowns().size());
        assertEquals(20, reloadedItem.getAssignedSizeQuantity());

        Map<String, SizeBreakdown> sizesByLabel = reloadedItem.getSizeBreakdowns().stream()
                .collect(Collectors.toMap(SizeBreakdown::getSize, size -> size));

        assertEquals(sizeSId, sizesByLabel.get("S").getId());
        assertEquals(3, sizesByLabel.get("S").getQuantity());
        assertEquals(sizeMId, sizesByLabel.get("M").getId());
        assertEquals(7, sizesByLabel.get("M").getQuantity());
        assertEquals(sizeLId, sizesByLabel.get("L").getId());
        assertEquals(6, sizesByLabel.get("L").getQuantity());
        assertEquals(sizeXlId, sizesByLabel.get("XL").getId());
        assertEquals(4, sizesByLabel.get("XL").getQuantity());
    }

    @Test
    void existingOrderItemWithoutSizesRemainsLoadable() {
        LocalDate today = LocalDate.now();

        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta Básica",
                10,
                "Hydrotech",
                "Negro",
                Money.of(new BigDecimal("30000")),
                ProductSpecification.empty(),
                List.of()
        );

        Order order = Order.create(
                OrderNumber.of("ORD-EMPTY-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(5)),
                "Compatibility Tester",
                null,
                List.of(item)
        );

        Order saved = orderRepository.save(order);
        UUID orderId = saved.getId();

        entityManager.flush();
        entityManager.clear();

        Order reloaded = orderRepository.findById(orderId).orElseThrow();
        OrderItem reloadedItem = reloaded.getItems().getFirst();

        assertTrue(reloadedItem.getProductSpecification().isEmpty());
        assertTrue(reloadedItem.getSizeBreakdowns().isEmpty());
        assertEquals(10, reloadedItem.getQuantity());
        assertEquals("Camiseta Básica", reloadedItem.getProductName());
    }
}
